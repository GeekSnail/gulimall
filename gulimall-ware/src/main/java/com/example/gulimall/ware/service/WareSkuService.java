package com.example.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.to.WareSkuLock;
import com.example.common.to.mq.StockLocked;
import com.example.common.utils.PageUtils;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.example.gulimall.ware.vo.AddrPairFare;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 商品库存
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:33:02
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    Map<Long, Boolean> hasStockBySkuIds(List<Long> skuIds);

    boolean lockStock(WareSkuLock wareSkuLock);

    void unlockStock(StockLocked stockLocked);

    void unlockStock(Long orderId);
}

