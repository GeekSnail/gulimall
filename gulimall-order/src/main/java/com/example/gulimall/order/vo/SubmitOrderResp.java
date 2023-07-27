package com.example.gulimall.order.vo;

import com.example.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResp {
    OrderEntity order;
    int code = 0; //0成功 错误状态码
}
