package com.example.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:33:02
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

