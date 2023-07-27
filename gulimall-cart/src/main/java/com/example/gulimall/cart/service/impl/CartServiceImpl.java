package com.example.gulimall.cart.service.impl;

import com.example.common.utils.R;
import com.example.gulimall.cart.feign.ProductFeignService;
import com.example.gulimall.cart.feign.WareFeignService;
import com.example.gulimall.cart.interceptor.CartInterceptor;
import com.example.gulimall.cart.service.CartService;
import com.example.gulimall.cart.vo.Cart;
import com.example.gulimall.cart.vo.CartItem;
import com.example.gulimall.cart.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private final String CART_PREFIX = "gulimall:cart:";
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareFeignService wareFeignService;

    @Override
    public CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = (CartItem) cartOps.get(skuId.toString());
        if (cartItem == null) {
            CartItem item = new CartItem();
            CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                R r = productFeignService.skuInfo(skuId);
                if (r.getCode() == 0) {
                    Map<String, Object> data = (Map) r.get("skuInfo");
                    item.setSkuId(skuId);
                    item.setCount(count);
                    item.setTitle((String) data.get("skuTitle"));
                    item.setImage((String) data.get("skuDefaultImg"));
                    item.setPrice(BigDecimal.valueOf((Double) data.get("price")));
                    item.setChecked(true);
                }
            }, executor);
            CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
                List<String> strings = productFeignService.saleAttrAsStringList(skuId);
                item.setSkuAttr(strings);
            }, executor);
            CompletableFuture.allOf(skuFuture, skuAttrFuture).get();
            cartOps.put(skuId.toString(), item);
            return item;
        } else {
            cartItem.setCount(cartItem.getCount() + count);
            cartOps.put(skuId.toString(), cartItem);
            return cartItem;
        }
    }

    @Override
    public CartItem getItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem item = (CartItem) cartOps.get(skuId.toString());
        return item;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfo userInfo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        String cartKey = CART_PREFIX + userInfo.getUserKey();
        List<CartItem> cartItems = listItems(cartKey);
        if (userInfo.getUserId() != null) { //登录
            if (cartItems != null) { //合并购物车
                for (CartItem item: cartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                clearCart(cartKey); //清除临时购物车
            }
            cartKey = CART_PREFIX + userInfo.getUserId();
            cartItems = listItems(cartKey);
        }
        cart.setItems(cartItems);
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        CartItem item = getItem(skuId);
        item.setChecked(checked == 1);
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.put(skuId.toString(), item);
    }

    @Override
    public void countItem(Long skuId, Integer count) {
        CartItem item = getItem(skuId);
        item.setCount(count);
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.put(skuId.toString(), item);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.delete(skuId.toString());
    }

    @Override
    public List<CartItem> currentUserCartItems() throws ExecutionException, InterruptedException {
        UserInfo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() != null) {
            String cartKey = CART_PREFIX + userInfo.getUserId();
            List<CartItem> cartItems = listItems(cartKey);
            if (cartItems != null && cartItems.size() > 0) {
                List<CartItem> checkedItems = cartItems.stream().filter(it -> it.getChecked()).collect(Collectors.toList());
                List<Long> ids = checkedItems.stream().map(it -> it.getSkuId()).collect(Collectors.toList());
                CompletableFuture<Map<Long, BigDecimal>> priceFuture = CompletableFuture.supplyAsync(() -> {
                     return productFeignService.getPriceByIds(ids);
                }, executor);
                CompletableFuture<Map<String, Boolean>> hasstockFuture = CompletableFuture.supplyAsync(() -> {
                    R r = wareFeignService.hasStockBySkuIds(ids);
                    if (r.getCode() == 0)
                        return (Map<String, Boolean>) r.get("data");
                    return null;
                }, executor);
                CompletableFuture.allOf(priceFuture, hasstockFuture).get();
                Map<Long, BigDecimal> priceMap = priceFuture.get();
                Map<String, Boolean> hasstockMap = hasstockFuture.get();
                if (priceMap != null && hasstockMap != null) {
                    return checkedItems.stream().map(it -> {
                        if (priceMap.containsKey(it.getSkuId()))
                            it.setPrice(priceMap.get(it.getSkuId()));
                        if (hasstockMap.containsKey(it.getSkuId().toString()))
                            it.setHasStock(hasstockMap.get(it.getSkuId().toString()));
                        return it;
                    }).collect(Collectors.toList());
                }
            }
        }
        return null;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfo userInfo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        return hashOps;
    }
    private List<CartItem> listItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItems = values.stream().map(value -> (CartItem) value).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }
}
