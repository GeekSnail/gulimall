package com.example.gulimall.order.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class OrderSubmit {
    /*无需提交确认页购买项，去购物车再获取一次*/
    @NotNull
    Long addrId;
    int payType;
    @Pattern(regexp = "^\\w+$", message = "token错误")
    String token; //防重令牌
    @NotNull
    BigDecimal payPrice; //应付价格
    String note; //备注
    //优惠 发票
}
