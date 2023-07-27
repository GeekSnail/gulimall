package com.example.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("ums_member_social")
public class MemberSocialEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long memberId;
    private Long socialId;
    private String socialType;
    private String socialUsername;
    private String accessToken;
}
