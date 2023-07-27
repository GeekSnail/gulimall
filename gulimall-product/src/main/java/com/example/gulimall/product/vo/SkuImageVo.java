package com.example.gulimall.product.vo;

import lombok.Data;

@Data
public class SkuImageVo {
    private String imgUrl;
    //默认图[0 - 不是默认图，1 - 是默认图]
    private Integer defaultImg;
}
