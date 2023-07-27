package com.example.gulimall.seckill.controller;

import com.example.common.utils.R;
import com.example.gulimall.seckill.service.SeckillService;
import com.example.gulimall.seckill.vo.SeckillSkuRelationWithSku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    SeckillService seckillService;
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R currentSeckillSkus() {
        List<SeckillSkuRelationWithSku> data = seckillService.currentSeckillSkus();
        return R.ok().put("data", data);
    }
    @ResponseBody
    @GetMapping("/sku/{skuId}")
    public R getSeckillSku(@PathVariable("skuId") Long skuId) {
        SeckillSkuRelationWithSku data = seckillService.getSeckillSku(skuId);
        return R.ok().put("data", data);
    }
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId, @RequestParam("code") String code, @RequestParam("count") Integer count, Model model) {
        if (count > 0) {
            String orderSn = seckillService.kill(killId, code, count);
            if (orderSn != null) {
                model.addAttribute("orderSn", orderSn);
                return "success.html";
            }
        }
        return "redirect://gulimall.com";
    }
}
