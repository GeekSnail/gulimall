package com.example.gulimall.seckill.service;

import com.example.gulimall.seckill.vo.SeckillSkuRelationWithSku;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest(int days);

    List<SeckillSkuRelationWithSku> currentSeckillSkus();

    SeckillSkuRelationWithSku getSeckillSku(Long skuId);

    String kill(String killId, String code, Integer count);
}
