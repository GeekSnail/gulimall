package com.example.gulimall.member.vo;

import lombok.Data;

@Data
public class SocialUser {
    private Long id;
    private String username; //username
    private String email;
    private String accessToken;
    private String type;
//    private String avatar_url;
}