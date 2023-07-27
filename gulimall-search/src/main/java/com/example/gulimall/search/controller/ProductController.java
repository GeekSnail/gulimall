package com.example.gulimall.search.controller;

import com.example.common.exception.BizCodeEnume;
import com.example.common.to.es.SkuModel;
import com.example.common.utils.R;
import com.example.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/product")
@RestController
public class ProductController {
    @Autowired
    ProductService productService;
    //商品上架
    @PostMapping("/save")
    public R spuUp(@RequestBody List<SkuModel> skuModels) {
        boolean b = false;
        R r = R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        try {
            b = productService.spuUp(skuModels);
        } catch (IOException e) {
            log.error(e.toString());
            return r;
        }
        return b ? R.ok() : r;
    }
}
