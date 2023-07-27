package com.example.gulimall.thirdparty.controller;

import com.example.common.utils.R;
import com.example.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    SmsComponent smsComponent;
    @GetMapping("/send")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("content") String content) {
        smsComponent.send(phone, content);
        return R.ok();
    }
}
