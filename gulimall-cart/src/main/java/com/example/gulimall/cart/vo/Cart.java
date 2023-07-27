package com.example.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    List<CartItem> items;
    Integer countType; //商品类型数量
    Integer countNum;  //商品数量
    BigDecimal totalAmount; //商品总价
    BigDecimal reduce = BigDecimal.valueOf(0); //减免价格
    public List<CartItem> getItems() {
        return items;
    }
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    public Integer getCountType() {
        return items==null?0:items.size();
    }
    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item: items) {
                count += item.getCount();
            }
        }
        return count;
    }
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal(0);
        if (items != null && items.size() > 0) {
            for (CartItem item: items) {
                if (item.getChecked()) {
                    amount = amount.add(item.getTotalPrice());
                }
            }
            amount = amount.subtract(getReduce());
        }
        return amount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public BigDecimal getReduce() {
        return reduce;
    }
    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
