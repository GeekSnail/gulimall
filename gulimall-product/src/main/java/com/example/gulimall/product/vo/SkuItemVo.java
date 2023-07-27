package com.example.gulimall.product.vo;

import com.example.gulimall.product.entity.SkuImagesEntity;
import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //sku基本信息 pms_sku_info
    SkuInfoEntity info;
    Boolean hasStock = true;
    //sku图片 pms_sku_images
    List<SkuImagesEntity> images;
    //spu介绍
    SpuInfoDescEntity desc;
    //spu销售属性
    List<SkuSaleAttrVo> saleAttrs;
    //spu规格参数
    List<SpuAttrGroupVo> groupAttrs;
    //秒杀优惠信息
    SeckillSkuRelation seckillSku;

    @Data
    public static class SkuSaleAttrVo {
        Long attrId;
        String attrName;
        List<AttrValueSkuIds> attrValues; //聚合
    }
    @Data
    public static class AttrValueSkuIds {
        String attrValue;
        String skuIds; //1,2
    }
    @Data
    public static class SpuAttrGroupVo {
        String groupName;
        List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo {
        String attrName;
        String attrValue;
    }
}
