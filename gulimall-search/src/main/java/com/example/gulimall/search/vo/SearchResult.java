package com.example.gulimall.search.vo;

import com.example.common.to.es.SkuModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    List<SkuModel> products;

    Integer pageNum;
    Long total;         //总记录数
    Integer totalPages; //总页码

    List<Long> attrIds;

    List<BrandVo> brands;
    List<CatalogVo> catalogs;
    List<AttrVo> attrs;
    @Data
    public static class BrandVo {
        Long brandId;
        String brandName;
        String brandImg;
    }
    @Data
    public static class CatalogVo {
        Long catalogId;
        String catalogName;
    }
    @Data
    public static class AttrVo {
        Long attrId;
        String attrName;
        List<String> attrValue;
    }
}
