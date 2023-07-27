package com.example.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.gulimall.member.entity.MemberEntity;
import com.example.gulimall.member.exception.MobileExistedException;
import com.example.gulimall.member.exception.UsernameExistedException;
import com.example.gulimall.member.vo.LoginVo;
import com.example.gulimall.member.vo.RegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:27:14
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(RegisterVo vo);
//    void checkEmailUnique(String email);
    void checkUsernameUnique(String username) throws UsernameExistedException;
    void checkMobileUnique(String mobile) throws MobileExistedException;

    MemberEntity login(LoginVo vo);

    MemberEntity socialLogin(String token, String type) throws Exception;
}

