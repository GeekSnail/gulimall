package com.example.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrder {
    String orderSn;
    Long promotionSessionId;
    Long skuId;
    BigDecimal seckillPrice;
    Integer count;
    Long memberId;
}
