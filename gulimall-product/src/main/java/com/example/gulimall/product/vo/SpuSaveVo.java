package com.example.gulimall.product.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuSaveVo {
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<AttrValueVo> baseAttrs;
    private List<SkuInfoVo> skus;
    @Data
    public class Bounds {
        private BigDecimal buyBounds;
        private BigDecimal growBounds;
    }
}
