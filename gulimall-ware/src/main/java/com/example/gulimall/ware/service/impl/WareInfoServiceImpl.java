package com.example.gulimall.ware.service.impl;

import com.example.common.utils.R;
import com.example.gulimall.ware.feign.MemberFeignService;
import com.example.gulimall.ware.vo.AddrPairFare;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.ware.dao.WareInfoDao;
import com.example.gulimall.ware.entity.WareInfoEntity;
import com.example.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public AddrPairFare getAddrPairAndFare(Long addrId, List<Long> skuIds) throws ExecutionException, InterruptedException {
        CompletableFuture<Map> recvAddrFuture = CompletableFuture.supplyAsync(() -> {
            R r = memberFeignService.recvAddrInfo(addrId);
            if (r.getCode() == 0)
                return (Map) r.get("memberReceiveAddress");
            return null;
        }, executor);
        CompletableFuture<List<WareInfoEntity>> wareFuture = CompletableFuture.supplyAsync(() -> {
            return baseMapper.getWareBySkuIds(skuIds);
        }, executor);
        CompletableFuture.allOf(recvAddrFuture, wareFuture).get();
        Map map = recvAddrFuture.get();
        List<WareInfoEntity> entities = wareFuture.get();
        if (map != null && entities != null && entities.size() > 0) {
            WareInfoEntity ware = entities.get(0);
            AddrPairFare vo = new AddrPairFare();
            vo.setWareId(ware.getId());
            vo.setWareAddr(ware.getAddress());
            vo.setRecvAddrId(addrId);
            vo.setRecvProvince((String) map.get("province"));
            vo.setRecvCity((String) map.get("city"));
            vo.setRecvRegion((String) map.get("region"));
            vo.setRecvDetailAddress((String) map.get("detail_address"));
            vo.setRecvName((String) map.get("name"));
            String phone = (String) map.get("phone");
            vo.setRecvPhone(phone);
            vo.setFare(Integer.parseInt(phone.substring(phone.length() - 1)));
            return vo;
        }
        return null;
    }

}