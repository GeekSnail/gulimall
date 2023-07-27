package com.example.gulimall.member.exception;

public class MobileExistedException extends RuntimeException {
    public MobileExistedException() {
        super("手机号已存在");
    }
}
