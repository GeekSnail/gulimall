package com.example.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SeckillSkuRelationWithSku {
    private Long id;
    private Long promotionId; //活动id
    private Long promotionSessionId; //活动场次id
    private Long skuId;
    private BigDecimal seckillPrice;
    private long seckillCount; //秒杀总量
    private int seckillLimit; //每人限购数量
    private int seckillSort;
    private SkuInfo skuInfo;
    private Long startTime;
    private Long endTime;
    private String randomCode;
}
