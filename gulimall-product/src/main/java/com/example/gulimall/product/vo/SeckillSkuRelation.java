package com.example.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRelation {
    private Long id;
    private Long promotionId; //活动id
    private Long promotionSessionId; //活动场次id
    private Long skuId;
    private BigDecimal seckillPrice;
    private long seckillCount; //秒杀总量
    private int seckillLimit; //每人限购数量
    private int seckillSort;
    private Long startTime;
    private Long endTime;
    private String randomCode;
}
