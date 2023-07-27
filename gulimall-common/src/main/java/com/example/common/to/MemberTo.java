package com.example.common.to;

import java.util.Date;

public class MemberTo {
    private Long id;
    /**
     * 会员等级id
     */
    private Long levelId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    /**
     * 头像
     */
    private String header;
    private Integer gender;
    private Date birth;
    private String city;
    private String job;
    /**
     * 个性签名
     */
    private String sign;
    /**
     * 用户来源
     */
    private Integer sourceType;
    /**
     * 积分
     */
    private Integer integration;
    /**
     * 成长值
     */
    private Integer growth;
    /**
     * 启用状态
     */
    private Integer status;
    private Date createTime;
}
