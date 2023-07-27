package com.example.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItem {
    Long skuId;
//    Boolean checked = true;
    String title;
    String image;
    List<String> skuAttr;
    BigDecimal price;
    Integer count;
    BigDecimal totalPrice;
    boolean hasStock;
}
