package com.example.gulimall.order.feign;

import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/getBySkuIds")
    R getBySkuIds(@RequestParam("skuIds") List<Long> skuIds);
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuinfo(@PathVariable("skuId") Long skuId);
}
