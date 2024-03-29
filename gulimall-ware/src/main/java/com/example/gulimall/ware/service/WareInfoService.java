package com.example.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.gulimall.ware.entity.WareInfoEntity;
import com.example.gulimall.ware.vo.AddrPairFare;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 仓库信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:33:02
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    AddrPairFare getAddrPairAndFare(Long addrId, List<Long> skuIds) throws ExecutionException, InterruptedException;
}

