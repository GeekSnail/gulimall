package com.example.gulimall.product.service.impl;

import com.example.common.utils.R;
import com.example.gulimall.product.entity.SkuImagesEntity;
import com.example.gulimall.product.entity.SpuInfoDescEntity;
import com.example.gulimall.product.entity.SpuInfoEntity;
import com.example.gulimall.product.feign.SeckillFeignService;
import com.example.gulimall.product.service.*;
import com.example.gulimall.product.vo.SeckillSkuRelation;
import com.example.gulimall.product.vo.SkuItemVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.SkuInfoDao;
import com.example.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    SeckillFeignService seckillFeignService;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        // status=1 and (id=1 or spu_name like 'xxx')
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            wrapper.le("price", max);
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> listBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity desc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(desc);
        }, threadPoolExecutor);
        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SpuAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getCatalogId(), res.getSpuId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SkuSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrVos);
        }, threadPoolExecutor);
        CompletableFuture imgFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.listBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);
        //sku秒杀优惠
        CompletableFuture seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSeckillSku(skuId);
            if (r.getCode() == 0) {
                SeckillSkuRelation seckillSku = objectMapper.convertValue(r.get("data"), SeckillSkuRelation.class);
                skuItemVo.setSeckillSku(seckillSku);
            }
        }, threadPoolExecutor);
        CompletableFuture.allOf(descFuture, attrGroupFuture, saleAttrFuture, imgFuture, seckillFuture).get();
        return skuItemVo;
    }

}