package com.example.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.example.common.exception.BizCodeEnume;
import com.example.gulimall.member.exception.MobileExistedException;
import com.example.gulimall.member.exception.UsernameExistedException;
import com.example.gulimall.member.feign.CouponFeignService;
import com.example.gulimall.member.vo.LoginVo;
import com.example.gulimall.member.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.member.entity.MemberEntity;
import com.example.gulimall.member.service.MemberService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * 会员
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:27:14
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;
    //测试Feign
    @RequestMapping("/coupons")
    public R test() {
        MemberEntity member = new MemberEntity();
        member.setNickname("张三");
        //调用远程服务
        R r = couponFeignService.memberCoupons();
        return R.ok().put("member", member).put("coupons", r.get("coupons"));
    }

    @PostMapping("/register")
    public R register(@RequestBody RegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (MobileExistedException e) {
            System.err.println(e);
            return R.error(BizCodeEnume.MOBILE_EXISTED_EXCEPTION.getCode(), BizCodeEnume.MOBILE_EXISTED_EXCEPTION.getMsg());
        } catch (UsernameExistedException e) {
            System.err.println(e);
            return R.error(BizCodeEnume.USER_EXISTED_EXCEPTION.getCode(), BizCodeEnume.USER_EXISTED_EXCEPTION.getMsg());
        }
        return R.ok();
    }
    @PostMapping("/login")
    public R login(@RequestBody LoginVo vo) {
        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            return R.ok().put("data", entity);
        }
        return R.error(BizCodeEnume.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),
                BizCodeEnume.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
    }
    @PostMapping("/sociallogin")
    public R socialLogin(@RequestParam("token") String token, @RequestParam("type") String type) throws Exception {
        MemberEntity entity = memberService.socialLogin(token, type);
        if (entity != null) {
            return R.ok().put("data", entity);
        }
        return R.error();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
