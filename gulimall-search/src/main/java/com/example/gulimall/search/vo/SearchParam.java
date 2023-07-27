package com.example.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    String keyword;
    Long catalog3Id;
    // sort=saleCount_asc|desc
    // sort=skuPrice_asc|desc
    // sort=hotScore_asc|desc
    String sort;
    Integer hasStock;
    String skuPrice;    //1_500|_500|500_
    List<Long> brandId; //可多选
    List<String> attrs; // {attrId}_{attrValue1}:{attrValue2}
    Integer pageNum = 1;
}
