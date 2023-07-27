package com.example.gulimall.ware.service.impl;

import com.example.common.exception.NoStockException;
import com.example.common.to.WareSkuLock;
import com.example.common.to.mq.StockLocked;
import com.example.common.to.mq.WareOrderTaskDetail;
import com.example.common.utils.R;
import com.example.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.example.gulimall.ware.entity.WareOrderTaskEntity;
import com.example.gulimall.ware.feign.OrderFeignService;
import com.example.gulimall.ware.feign.ProductFeignService;
import com.example.gulimall.ware.service.WareOrderTaskDetailService;
import com.example.gulimall.ware.service.WareOrderTaskService;
import com.example.gulimall.ware.vo.UpdateStockLockedVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.ware.dao.WareSkuDao;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.example.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params), wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> entities = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setWareId(wareId);
            skuEntity.setStock(skuNum);
            skuEntity.setStockLocked(0);
            //远程查询sku名字，若失败(try-catch) 整个事务无需回滚
            try {
                R r = productFeignService.info(skuId);
                if (r.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) r.get("skuInfo");
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            this.save(skuEntity); //insert
        } else {
            baseMapper.addStock(skuId, wareId, skuNum); //update
        }
    }

    @Override
    public Map<Long, Boolean> hasStockBySkuIds(List<Long> skuIds) {
        Map<Long, Map<String, Number>> map = baseMapper.getStockBySkuIds(skuIds);
        Map<Long, Boolean> rmap = new HashMap<>();
        skuIds.forEach(id -> {
            boolean b = map.get(id) != null && ((Number) (map.get(id).get("net_stock"))).intValue() > 0;
            rmap.put(id, b);
        });
        return rmap;
    }

    @Override
    @Transactional
    public boolean lockStock(WareSkuLock wareSkuLock) {
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderId(wareSkuLock.getOrderId());
        taskEntity.setOrderSn(wareSkuLock.getOrderSn());
        wareOrderTaskService.save(taskEntity); //
        for (WareSkuLock.SkuCount it: wareSkuLock.getLocks()) {
            UpdateStockLockedVo updateVo = new UpdateStockLockedVo(it.getSkuId(), it.getCount(), 0);
            baseMapper.tryLockStock(updateVo);
            if (updateVo.getWareId() == 0) {
                throw new NoStockException(it.getSkuId());
            }
            WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, it.getSkuId(), null, it.getCount(), taskEntity.getId(), updateVo.getWareId(), 1);
            wareOrderTaskDetailService.save(taskDetailEntity); //
            StockLocked stockLocked = new StockLocked(taskEntity.getId(), taskDetailEntity.getId(), wareSkuLock.getOrderId());
//            WareOrderTaskDetail taskDetail = new WareOrderTaskDetail();
//            BeanUtils.copyProperties(taskDetailEntity, taskDetail);
//            stockLocked.setTaskDetail(taskDetail);
            rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLocked);
        }
        return true;
    }

    // taskId, taskDetailId, orderId
    @Override
    @Transactional
    public void unlockStock(StockLocked stockLocked) {
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(stockLocked.getTaskDetailId());
        //若查询不到原工作单详情 说明库存锁定失败且锁定事务已回滚 此时无需解锁
        if (detailEntity != null && detailEntity.getLockStatus() == 1) {
            //查订单 若无则说明订单提交事务已回滚 需解锁，若有且状态已取消 需解锁
            R r = orderFeignService.info(stockLocked.getOrderId());
            if (r.getCode() == 0) {
                Map<String,Object> order = (Map<String, Object>) r.get("order");
                if (order == null || (int)order.get("status") == 4) {
                    unlockStock(detailEntity);
                }
            } else {
                throw new RuntimeException(r.getMsg());
            }
        }
    }

    private void unlockStock(WareOrderTaskDetailEntity entity) {
        baseMapper.unlockStock(entity.getSkuId(), entity.getSkuNum(), entity.getWareId());
        entity.setLockStatus(2); //更新状态为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }

    @Override
    @Transactional
    public void unlockStock(Long orderId) {
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.listByOrderId(orderId);
        for (WareOrderTaskDetailEntity entity: detailEntities) {
            if (entity.getLockStatus() == 1) {
                unlockStock(entity);
            }
        }
    }


}