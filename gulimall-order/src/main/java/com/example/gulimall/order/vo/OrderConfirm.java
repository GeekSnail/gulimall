package com.example.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirm {
    //收货地址 ums_member_receive_address
    List<MemberAddress> address;
    List<OrderItem> items; //选中的购物车项
    //发票

    //优惠券

    Integer integration; //积分
    BigDecimal total; //订单总额
    BigDecimal payPrice; //应付价格
    String token; //防重令牌
    public Integer getCount() {
        Integer c = 0;
        if (items != null) {
            for (OrderItem item: items) {
                c += item.getCount();
            }
        }
        return c;
    }
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if (items != null) {
            for (OrderItem item: items) {
                BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(itemTotal);
            }
        }
        return sum;
    }
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
