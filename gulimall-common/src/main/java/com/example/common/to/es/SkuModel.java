package com.example.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuModel {
    Long skuId;
    Long spuId;
    String skuTitle;
    BigDecimal skuPrice;
    String skuImg;
    Long saleCount;
    Boolean hasStock;
    Long hotScore;
    Long brandId;
    Long catalogId;
    String brandName;
    String brandImg;
    String catalogName;
    List<Attr> attrs;
    @Data
    public static class Attr {
       Long attrId;
       String attrName;
       String attrValue;
    }
}
