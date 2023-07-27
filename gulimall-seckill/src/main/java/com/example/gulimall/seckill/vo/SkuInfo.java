package com.example.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuInfo {
    private Long skuId;
    private Long spuId;
    private String skuName;
    private String skuDesc;
    private Long catalogId;
    private Long brandId;
    private String skuDefaultImg;
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;
    private Long saleCount;
}
