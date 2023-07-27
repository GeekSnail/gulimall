package com.example.gulimall.auth.feign;

import com.example.common.utils.R;
import com.example.gulimall.auth.vo.LoginVo;
import com.example.gulimall.auth.vo.RegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody RegisterVo vo);
    @PostMapping("/member/member/login")
    R login(@RequestBody LoginVo vo);
    @PostMapping("/member/member/sociallogin")
    R socialLogin(@RequestParam("token") String token, @RequestParam("type") String type);
}
