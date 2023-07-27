package com.example.gulimall.ware.dao;

import com.example.gulimall.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库信息
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:33:02
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {

    List<WareInfoEntity> getWareBySkuIds(@Param("skuIds") List<Long> skuIds);
}
