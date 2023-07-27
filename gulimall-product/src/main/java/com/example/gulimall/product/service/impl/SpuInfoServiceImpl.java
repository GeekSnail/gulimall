package com.example.gulimall.product.service.impl;

import com.example.common.constant.ProductConstant;
import com.example.common.to.SkuReductionTo;
import com.example.common.to.SpuBoundTo;
import com.example.common.to.es.SkuModel;
import com.example.common.utils.R;
import com.example.gulimall.product.entity.*;
import com.example.gulimall.product.feign.CouponFeignService;
import com.example.gulimall.product.feign.SearchFeignService;
import com.example.gulimall.product.feign.WareFeignService;
import com.example.gulimall.product.service.*;
import com.example.gulimall.product.vo.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

//    @GlobalTransactional
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfo);
        spuInfo.setCreateTime(new Date());
        spuInfo.setUpdateTime(new Date());
        this.save(spuInfo);
        //保存spu描述 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        if (decript.size() > 0) {
            SpuInfoDescEntity spuInfoDesc = new SpuInfoDescEntity();
            spuInfoDesc.setSpuId(spuInfo.getId());
            spuInfoDesc.setDecript(String.join(",", decript));
            spuInfoDescService.save(spuInfoDesc);
        }
        //保存spu图片集 pms_spu_images
        List<String> images = vo.getImages();
        if (images.size() > 0) {
            List<SpuImagesEntity> imagesEntities = images.stream().map(img -> {
                SpuImagesEntity imagesEntity = new SpuImagesEntity();
                imagesEntity.setSpuId(spuInfo.getId());
                imagesEntity.setImgUrl(img);
                return imagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(imagesEntities);
        }
        //保存spu规格参数 pms_product_attr_value
        List<AttrValueVo> baseAttrs = vo.getBaseAttrs();
        List<Long> ids = baseAttrs.stream().map(attrValueVo -> attrValueVo.getAttrId()).collect(Collectors.toList());
        if (ids.size() > 0) {
            //Function.identity()返回一个输出跟输入一样的Lambda表达式 t -> t
            Map<Long, AttrEntity> map = attrService.listByIds(ids).stream().collect(Collectors.toMap(AttrEntity::getAttrId, t -> t));
            List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(attrValueVo -> {
                ProductAttrValueEntity attrValue = new ProductAttrValueEntity();
                attrValue.setAttrId(attrValueVo.getAttrId());
                attrValue.setAttrValue(attrValueVo.getAttrValues());
                attrValue.setQuickShow(attrValueVo.getShowDesc());
                attrValue.setAttrName(map.get(attrValueVo.getAttrId()).getAttrName());
                attrValue.setSpuId(spuInfo.getId());
                return attrValue;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(attrValueEntities);
        }
        //保存spu积分信息 sms_spu_bounds
        SpuSaveVo.Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfo.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败!");
        }
        //保存spu对应的sku信息：
        // sku基本信息 pms_sku_info
        List<SkuInfoVo> skus = vo.getSkus();
        if (skus.size() > 0) {
            List<SkuImagesEntity> skuImagesEntities = new ArrayList<>();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = new ArrayList<>();
            List<SkuInfoEntity> skuInfoEntities = skus.stream().map(sku -> {
                String defaultImg = "";
                for (SkuImageVo img : sku.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImg = img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfo = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfo);
                skuInfo.setBrandId(spuInfo.getBrandId());
                skuInfo.setCatalogId(spuInfo.getCatalogId());
                skuInfo.setSpuId(spuInfo.getId());
                skuInfo.setSaleCount(0L);
                skuInfo.setSkuDefaultImg(defaultImg);
                Long skuId = skuInfo.getSkuId();
                sku.getImages().forEach(img -> {
                    if (!StringUtils.isEmpty(img.getImgUrl())) {
                        SkuImagesEntity skuImages = new SkuImagesEntity();
                        BeanUtils.copyProperties(img, skuImages);
                        skuImages.setSkuId(skuId);
                        skuImagesEntities.add(skuImages);
                    }
                });
                sku.getAttr().forEach(attr -> {
                    SkuSaleAttrValueEntity saleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, saleAttrValue);
                    saleAttrValue.setSkuId(skuId);
                    skuSaleAttrValueEntities.add(saleAttrValue);
                });
                // sku优惠|满减信息 sms_sku_ladder, sms_sku_full_reduction, sms_member_price
                //过滤掉满0件打0折的默认数据
                if (sku.getFullCount() > 0 || sku.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(sku, skuReductionTo);
                    skuReductionTo.setSkuId(skuId);

                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败!");
                    }
                }
                return skuInfo;
            }).collect(Collectors.toList());
            skuInfoService.saveBatch(skuInfoEntities);
            // sku图片信息 pms_sku_images
            if (skuImagesEntities.size() > 0) {
                skuImagesService.saveBatch(skuImagesEntities);
            }
            // sku销售属性信息 pms_sku_sale_attr_value
            if (skuSaleAttrValueEntities.size() > 0) {
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
            }
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        // status=1 and (id=1 or spu_name like 'xxx')
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public boolean up(Long id) {
        List<SkuInfoEntity> skus = skuInfoService.listBySpuId(id);
        //查询公共数据
        BrandEntity brand = brandService.getById(skus.get(0).getBrandId());
        CategoryEntity category = categoryService.getById(skus.get(0).getCatalogId());
        //attrs
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(id);
        List<Long> attrIds = baseAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        //过滤出search_type=1的attr_id
        List<Long> searchAttrIds = attrService.getSearchAttrIds(attrIds);
        //过滤出search_type=1的attr并组装
        List<SkuModel.Attr> attrs = baseAttrs.stream().filter(attr -> searchAttrIds.contains(attr.getAttrId()))
                .map(entity -> {
                    SkuModel.Attr attr = new SkuModel.Attr();
                    BeanUtils.copyProperties(entity, attr);
                    return attr;
                }).collect(Collectors.toList());
        //ware
        List<Long> skuIds = skus.stream().map(s -> s.getSkuId()).collect(Collectors.toList());
        R r = wareFeignService.hasStockBySkuIds(skuIds);
        Map<String, Boolean> stockMap = null;
        if (r.getCode() == 0) {
            stockMap = (Map<String, Boolean>) r.get("data");
        } else {
            log.error("远程调用wareFeignService.hasStockBySkuIds失败！");
        }
        Map<String, Boolean> finalStockMap = stockMap;
        List<SkuModel> skuModels = skus.stream().map(sku -> {
            SkuModel skuModel = new SkuModel();
            BeanUtils.copyProperties(sku, skuModel);
            //skuPrice:price,skuImg:skuDefaultImg
            skuModel.setSkuPrice(sku.getPrice());
            skuModel.setSkuImg(sku.getSkuDefaultImg());
            //brandName,brandImg,catalogName
            skuModel.setBrandName(brand.getName());
            skuModel.setBrandImg(brand.getLogo());
            skuModel.setCatalogName(category.getName());
            //attrs
            skuModel.setAttrs(attrs);
            //hasStock:,hotScore:
            skuModel.setHotScore(0L);
            if (finalStockMap != null) {
                skuModel.setHasStock(finalStockMap.get(sku.getSkuId()+""));
            }
            return skuModel;
        }).collect(Collectors.toList());
        //search
        R r1 = searchFeignService.spuUp(skuModels);
        if (r1.getCode() == 0) {
//            this.update(new UpdateWrapper<SpuInfoEntity>().eq("id", id)
//              .set("publish_status", ProductConstant.StatusEnum.SPU_UP.getValue())
//              .set("update_time", new Date()))
            baseMapper.updateStatus(id, ProductConstant.StatusEnum.SPU_UP.getValue());
            return true;
        } else {
            log.error('['+r1.getCode()+']' + r1.getMsg());
        }
        return false;
    }

    @Override
    public Map<Long,Map<String,Object>> getBySkuIds(List<Long> skuIds) {
        return baseMapper.getBySkuIds(skuIds);
    }
}