package com.example.gulimall.member.service.impl;

import com.example.gulimall.member.vo.SocialUser;
import com.example.gulimall.member.component.GithubApi;
import com.example.gulimall.member.dao.MemberSocialDao;
import com.example.gulimall.member.entity.MemberSocialEntity;
import com.example.gulimall.member.exception.MobileExistedException;
import com.example.gulimall.member.exception.UsernameExistedException;
import com.example.gulimall.member.vo.LoginVo;
import com.example.gulimall.member.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.member.dao.MemberDao;
import com.example.gulimall.member.entity.MemberEntity;
import com.example.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberSocialDao memberSocialDao;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(RegisterVo vo) {
        MemberEntity entity = new MemberEntity();
        checkMobileUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());
        entity.setUsername(vo.getUsername());
        entity.setMobile(vo.getPhone());
        //密码加密
        String encode = new BCryptPasswordEncoder().encode(vo.getPassword());
        entity.setPassword(encode);
        entity.setLevelId(1L);
        baseMapper.insert(entity);
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistedException {
        Long n = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (n > 0) {
            throw new UsernameExistedException();
        }
    }

    @Override
    public void checkMobileUnique(String mobile) throws MobileExistedException {
        Long n = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        if (n > 0) {
            throw new MobileExistedException();
        }
    }

    @Override
    public MemberEntity login(LoginVo vo) {
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", vo.getAccount()).or().eq("mobile", vo.getAccount()));
        if (entity != null) {
            boolean b = new BCryptPasswordEncoder().matches(vo.getPassword(), entity.getPassword());
            if (b) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity socialLogin(String token, String type) throws Exception {
        SocialUser user = null;
        switch (type) {
            case "github": user = new GithubApi(token).user(); break;
        }
        if (user == null)
            return null;
        MemberSocialEntity memberSocial = memberSocialDao.selectOne(new QueryWrapper<MemberSocialEntity>()
                .eq("social_id", user.getId()).eq("social_type", user.getType()));
        MemberEntity entity = new MemberEntity();
        if (memberSocial == null) {
            String uuid = UUID.randomUUID().toString().substring(0, 4);
            entity.setUsername(user.getUsername() +"_"+uuid);
            entity.setEmail(user.getEmail());
            entity.setLevelId(1L);
            baseMapper.insert(entity);
            memberSocial = new MemberSocialEntity();
            memberSocial.setMemberId(entity.getId());
            memberSocial.setSocialId(user.getId());
            memberSocial.setSocialType(user.getType());
            memberSocial.setSocialUsername(user.getUsername());
            memberSocial.setAccessToken(user.getAccessToken());
            memberSocialDao.insert(memberSocial);
        } else {
            memberSocial.setSocialUsername(user.getUsername());
            memberSocial.setAccessToken(user.getAccessToken());
            MemberSocialEntity finalMemberSocial = memberSocial;
            CompletableFuture<Integer> updateFuture = CompletableFuture.supplyAsync(() -> {
                return memberSocialDao.updateById(finalMemberSocial);
            }, threadPoolExecutor);
            CompletableFuture<MemberEntity> selectFuture = CompletableFuture.supplyAsync(() -> {
                return baseMapper.selectById(finalMemberSocial.getMemberId());
            }, threadPoolExecutor);
            CompletableFuture.allOf(updateFuture, selectFuture).get();
            entity = selectFuture.get();
        }
        return entity;
    }

}