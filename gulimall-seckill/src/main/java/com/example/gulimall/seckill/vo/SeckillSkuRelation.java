package com.example.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRelation {
    private Long id;
    private Long promotionId; //活动id
    private Long promotionSessionId; //活动场次id
    private Long skuId;
    private BigDecimal seckillPrice;
    private int seckillCount; //秒杀总量
    private int seckillLimit; //每人限购数量
    private int seckillSort;
}
