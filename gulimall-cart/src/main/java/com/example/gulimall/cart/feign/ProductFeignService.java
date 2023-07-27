package com.example.gulimall.cart.feign;

import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);
    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> saleAttrAsStringList(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/price")
    Map<Long,BigDecimal> getPriceByIds(@RequestParam("skuIds") List<Long> skuIds);
}
