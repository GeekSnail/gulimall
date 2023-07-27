package com.example.gulimall.auth.controller;

import com.example.common.constant.AuthConstant;
import com.example.common.exception.BizCodeEnume;
import com.example.common.utils.R;
import com.example.gulimall.auth.feign.MemberFeignService;
import com.example.gulimall.auth.feign.ThirdpartyFeignService;
import com.example.gulimall.auth.vo.LoginVo;
import com.example.gulimall.auth.vo.RegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    ThirdpartyFeignService thirdpartyFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam(value="redirect_url",required=false) String redirectUrl, HttpSession session) {
        Object loginUser = session.getAttribute(AuthConstant.LOGIN_USER);
        if (loginUser == null) {
            return "login";
        }
        if (redirectUrl != null) {
            return "redirect:" + redirectUrl;
        }
        return "redirect://gulimall.com";
    }
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        String codeWithTime = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_PREFIX + phone);
        if (!StringUtils.isEmpty(codeWithTime)) {
            long l = Long.parseLong(codeWithTime.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                //同一phone在60s内不要重复发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        //redis缓存验证码 key:prefix+phone, value:code_timestamp
        thirdpartyFeignService.send(phone, "code:"+code);
        code += "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_PREFIX+phone, code, 5, TimeUnit.MINUTES);
        return R.ok();
    }
    @PostMapping("/register")
    public String register(@Valid RegisterVo vo, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);
            return "redirect://auth.gulimall.com/reg.html";
        }
        //register
        String code = vo.getCode();
        String key = AuthConstant.SMS_CODE_PREFIX + vo.getPhone();
        String val = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(val)) {
            if (code.equals(val.split("_")[0])) {
                redisTemplate.delete(key);
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    return "redirect://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = Map.of("msg", r.getMsg());
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect://auth.gulimall.com/reg.html";
                }
            }
        }
        Map<String, String> errors = Map.of("code", "验证码错误");
        attributes.addFlashAttribute("errors", errors);
        return "redirect://auth.gulimall.com/reg.html";
    }
    @PostMapping("/login")
    public String login(LoginVo vo, RedirectAttributes attributes, HttpSession session, HttpServletRequest request) {
        R r = memberFeignService.login(vo);
        if (r.getCode() != 0) {
            Map<String, String> errors = Map.of("msg", r.getMsg());
            attributes.addFlashAttribute("errors", errors);
            return "redirect://auth.gulimall.com/login.html";
        }
        Object data = r.get("data");
        System.out.println(data);
        session.setAttribute(AuthConstant.LOGIN_USER, data);
        String referer = request.getHeader("referer");
        if (referer.contains("redirect_url=")) {
            String url = referer.split("redirect_url=")[1];
            if (url != null)
                return "redirect:" + url;
        }
        return "redirect://gulimall.com";
    }
}
