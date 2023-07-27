package com.example.gulimall.auth.feign;

import com.example.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-thirdparty")
public interface ThirdpartyFeignService {
    @GetMapping("/sms/send")
    public R send(@RequestParam("phone") String phone, @RequestParam("content") String content);
}
