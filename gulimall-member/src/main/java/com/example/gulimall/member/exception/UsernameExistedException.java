package com.example.gulimall.member.exception;

public class UsernameExistedException extends RuntimeException {
    public UsernameExistedException() {
        super("用户名已存在");
    }
}
