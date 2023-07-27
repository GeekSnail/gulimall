package com.example.gulimall.cart.controller;

import com.example.gulimall.cart.interceptor.CartInterceptor;
import com.example.gulimall.cart.service.CartService;
import com.example.gulimall.cart.vo.Cart;
import com.example.gulimall.cart.vo.CartItem;
import com.example.gulimall.cart.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;
    @GetMapping("/cart.html")
    public String cartPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cart";
    }
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        CartItem item = cartService.addToCart(skuId, count);
//        model.addAttribute("item", item);
//        return "success";
        ra.addAttribute("skuId", skuId);
        return "redirect://cart.gulimall.com/addToCartSuccess.html";
    }
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CartItem item = cartService.getItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }
    @PostMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect://cart.gulimall.com/cart.html";
    }
    @PostMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        cartService.countItem(skuId, count);
        return "redirect://cart.gulimall.com/cart.html";
    }
    @PostMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect://cart.gulimall.com/cart.html";
    }
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> currentUserCartItems() throws ExecutionException, InterruptedException {
        return cartService.currentUserCartItems();
    }
}
