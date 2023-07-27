package com.example.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVo {
    private List<SaleAttrValueVo> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<SkuImageVo> images;
    private List<String> descar;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPriceVo> memberPrice;
    @Data
    static class MemberPriceVo {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
