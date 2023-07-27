package com.example.gulimall.ware.dao;

import com.example.gulimall.ware.entity.WareInfoEntity;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gulimall.ware.vo.UpdateStockLockedVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:33:02
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    @MapKey("sku_id")
    Map<Long, Map<String, Number>> getStockBySkuIds(@Param("skuIds") List<Long> skuIds);

//    int tryLockStock(@Param("skuId") Number skuId, @Param("count") Number count);
    int tryLockStock(UpdateStockLockedVo vo);

    int unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum, @Param("wareId") Long wareId);

}
