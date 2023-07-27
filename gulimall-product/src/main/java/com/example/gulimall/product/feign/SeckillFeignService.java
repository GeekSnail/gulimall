package com.example.gulimall.product.feign;

import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value="gulimall-seckill", fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {
    @GetMapping("/sku/{skuId}")
    R getSeckillSku(@PathVariable("skuId") Long skuId);
}
