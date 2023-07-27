package com.example.gulimall.cart.service;

import com.example.gulimall.cart.vo.Cart;
import com.example.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException;

    CartItem getItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;
    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer checked);

    void countItem(Long skuId, Integer count);

    void deleteItem(Long skuId);

    List<CartItem> currentUserCartItems() throws ExecutionException, InterruptedException;
}
