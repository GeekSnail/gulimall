package com.example.gulimall.auth.controller;

import com.example.common.constant.AuthConstant;
import com.example.common.utils.R;
import com.example.gulimall.auth.component.GithubAuth;
import com.example.gulimall.auth.feign.MemberFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class OAuthController {
    @Autowired
    GithubAuth githubAuth;
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth/github/callback")
    public String github(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, Object> map = githubAuth.oauth(code);
        if (map != null) {
            R r = memberFeignService.socialLogin((String) map.get("access_token"), "github");
            if (r.getCode() == 0) {
                Object data = r.get("data");
                System.out.println(data);
                session.setAttribute(AuthConstant.LOGIN_USER, data);
                return "redirect://gulimall.com";
            }
        }
        return "redirect://auth.gulimall.com/login.html";
    }
}
