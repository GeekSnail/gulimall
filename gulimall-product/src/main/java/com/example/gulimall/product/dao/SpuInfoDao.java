package com.example.gulimall.product.dao;

import com.example.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-27 23:37:24
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateStatus(@Param("id") Long id, @Param("value") int value);

    @MapKey("sku_id")
    Map<Long,Map<String,Object>> getBySkuIds(@Param("skuIds") List<Long> skuIds);
}
