package com.example.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItem {
    Long skuId;
    Boolean checked = true;
    String title;
    String image;
    List<String> skuAttr;
    BigDecimal price;
    Integer count;
    BigDecimal totalPrice;
    boolean hasStock;
    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(count));
    }
}
