package com.example.gulimall.coupon.dao;

import com.example.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:10:55
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
