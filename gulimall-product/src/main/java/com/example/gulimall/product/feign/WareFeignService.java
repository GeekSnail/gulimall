package com.example.gulimall.product.feign;

import com.example.common.to.SkuHasStock;
import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    public R hasStockBySkuIds(@RequestBody List<Long> skuIds);
}
