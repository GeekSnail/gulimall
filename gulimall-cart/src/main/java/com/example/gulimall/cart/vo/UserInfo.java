package com.example.gulimall.cart.vo;

import lombok.Data;

@Data
public class UserInfo {
    Long userId;
    String userKey;
    boolean cookieHad = false;
}
