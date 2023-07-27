package com.example.gulimall.product.feign;

import com.example.common.to.es.SkuModel;
import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/product/save")
    R spuUp(@RequestBody List<SkuModel> skuModels);
}
