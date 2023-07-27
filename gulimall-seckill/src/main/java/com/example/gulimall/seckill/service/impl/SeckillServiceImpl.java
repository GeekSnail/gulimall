package com.example.gulimall.seckill.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.common.to.mq.SeckillOrder;
import com.example.common.utils.R;
import com.example.gulimall.seckill.feign.CouponFeignService;
import com.example.gulimall.seckill.feign.ProductFeignService;
import com.example.gulimall.seckill.interceptor.LoginInterceptor;
import com.example.gulimall.seckill.service.SeckillService;
import com.example.gulimall.seckill.vo.SeckillSession;
import com.example.gulimall.seckill.vo.SeckillSkuRelation;
import com.example.gulimall.seckill.vo.SeckillSkuRelationWithSku;
import com.example.gulimall.seckill.vo.SkuInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    ObjectMapper om;
    //    private final ObjectMapper om = new ObjectMapper();
    // seckill:sessions:startTime_endTime -> list(sessionId_skuId)
    private final String SESSSIONS_PREFIX = "seckill:sessions:";
    // seckill:skus -> hash{sessionId_skuId:skuInfo}
    private final String SKUS_PREFIX = "seckill:skus";
    private final String STOCK_PREFIX = "seckill:stock:"; //+randomCode
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest(int days) {
        //扫描需参与秒杀的活动
        R r = couponFeignService.seckillsessionLatest(3);
        if (r.getCode() == 0) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) r.get("data");
            if (data != null) {
                List<SeckillSession> sessions = data.stream().map(m -> om.convertValue(m, SeckillSession.class)).collect(Collectors.toList());
                //缓存到redis 活动 活动关联商品
                cacheSeckillSessions(sessions);
                cacheSeckillSkuRelations(sessions);
            }
        }
    }

    private void cacheSeckillSessions(List<SeckillSession> sessions) {
        sessions.forEach(session -> {
            String key = SESSSIONS_PREFIX + session.getStartTime().getTime() + "_" + session.getEndTime().getTime();
            if (!redisTemplate.hasKey(key)) {
                List<String> sessionIdskuIds = session.getSkuRelations().stream().map(sr -> session.getId()+"_"+sr.getSkuId()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, sessionIdskuIds);
            }
        });
    }

    private void cacheSeckillSkuRelations(List<SeckillSession> sessions) {
        BoundHashOperations hashOps = redisTemplate.boundHashOps(SKUS_PREFIX);
        sessions.forEach(session -> {
            List<Long> skuIds = session.getSkuRelations().stream().map(sr -> sr.getSkuId()).collect(Collectors.toList());
            R r = productFeignService.skuList(skuIds);
            if (r.getCode() == 0) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) r.get("data");
                List<SkuInfo> skuList = data.stream().map(m -> om.convertValue(m, SkuInfo.class)).collect(Collectors.toList());
                List<SeckillSkuRelation> skuRelations = session.getSkuRelations();
                if (skuList != null && skuList.size() == skuRelations.size()) {
                    for (int i = 0; i < skuList.size(); i++) {
                        String sessionIdskuId = session.getId() +"_"+ skuRelations.get(i).getSkuId();
                        if (!hashOps.hasKey(sessionIdskuId)) {
                            SeckillSkuRelationWithSku vo = new SeckillSkuRelationWithSku();
                            vo.setSkuInfo(skuList.get(i));
                            BeanUtils.copyProperties(skuRelations.get(i), vo);
                            vo.setStartTime(session.getStartTime().getTime());
                            vo.setEndTime(session.getEndTime().getTime());
                            //商品链接 随机码
                            String code = UUID.randomUUID().toString().replace("-", "");
                            vo.setRandomCode(code);
                            hashOps.put(sessionIdskuId, vo);
                            //分布式信号量 限流
                            RSemaphore semaphore = redissonClient.getSemaphore(STOCK_PREFIX + code);
                            semaphore.trySetPermits(skuRelations.get(i).getSeckillCount());
                        }
                    }
                }
            }
        });
    }

    @Override
    public List<SeckillSkuRelationWithSku> currentSeckillSkus() {
        //确定当前时间属于哪个秒杀场次
        long now = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSSIONS_PREFIX + "*");
        for (String key: keys) {
            String[] timePair = key.replace(SESSSIONS_PREFIX, "").split("_");
            long start = Long.parseLong(timePair[0]);
            long end = Long.parseLong(timePair[1]);
            if (start <= now && now <= end) {
                //获取该秒杀场次的所有商品信息
                List<String> sessionIdskuIds = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations hashOps = redisTemplate.boundHashOps(SKUS_PREFIX);
                List<SeckillSkuRelationWithSku> vos = hashOps.multiGet(sessionIdskuIds);
                if (vos != null)
                    return vos;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRelationWithSku getSeckillSku(Long skuId) {
        BoundHashOperations hashOps = redisTemplate.boundHashOps(SKUS_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            for (String key: keys) {
                String regex = "\\d+_" + skuId;
                if (Pattern.matches(regex, key)) {
                    SeckillSkuRelationWithSku vo = (SeckillSkuRelationWithSku) hashOps.get(key);
                    long now = new Date().getTime();
                    if (vo.getStartTime() <= now && now <= vo.getEndTime()) {

                    } else {
                        vo.setRandomCode(null); //非当前场次不返回随机码
                    }
                    return vo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer count) {
        long st1 = System.currentTimeMillis();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(SKUS_PREFIX);
        SeckillSkuRelationWithSku seckillSku = (SeckillSkuRelationWithSku) hashOps.get(killId);
        if (seckillSku == null)
            return null;
        Map<String,Object> userMap = LoginInterceptor.threadLocal.get();
        //校验时间,随机码,数量
        long now = new Date().getTime();
        long endTime = seckillSku.getEndTime();
        if (seckillSku.getStartTime() <= now && now <= endTime
                && seckillSku.getRandomCode().equals(code) && count <= seckillSku.getSeckillLimit()) {
            //校验当前用户是否已购买过 setnx
            String uid = userMap.get("id").toString();
            String key = uid + "_" + killId;
            Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, count, endTime - now, TimeUnit.MILLISECONDS);
            if (absent) {
                //占位成功 - 首次抢购
                RSemaphore semaphore = redissonClient.getSemaphore(STOCK_PREFIX + code);
//                    semaphore.acquire(count); //阻塞直到取得
                boolean b = semaphore.tryAcquire(count); //可选等待固定时间
                //快速下单
                if (b) {
                    String orderSn = IdWorker.getTimeId();
                    SeckillOrder order = new SeckillOrder();
                    order.setOrderSn(orderSn);
                    order.setMemberId(Long.parseLong(uid));
                    order.setPromotionSessionId(seckillSku.getPromotionSessionId());
                    order.setSkuId(seckillSku.getSkuId());
                    order.setSeckillPrice(seckillSku.getSeckillPrice());
                    order.setCount(count);
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill", order);
                    long st2 = System.currentTimeMillis();
                    log.info("seckill cost {}", (st2-st1));
                    return orderSn;
                }
            } else {
                //TODO 已有订单是否完成？
            }
        }
        return null;
    }
}
