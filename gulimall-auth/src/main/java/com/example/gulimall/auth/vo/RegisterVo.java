package com.example.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class RegisterVo {
//    @NotEmpty(message = "用户名不能为空")
//    @Length(min = 6, max = 16, message = "用户名必须是6-16位字符")
    @Pattern(regexp = "^\\w{6,16}$", message = "用户名格式错误") //用户名长度6-16位，可包含数字、字母、下划线
    String username;
//    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码格式错误") //密码必须是6-16位字符
    String password;
//    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式错误")
    String phone;
    @NotEmpty(message = "验证码不能为空")
    String code;
}
