package com.example.gulimall.order.feign;

import com.example.common.to.WareSkuLock;
import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @GetMapping("/ware/wareinfo/fare")
    R fare(@RequestParam("addrId") Long addrId, @RequestParam("skuIds") List<Long> skuIds);
    @PostMapping("/ware/waresku/lockstock")
    R lockStock(@RequestBody WareSkuLock wareSkuLock);
}
