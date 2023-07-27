package com.example.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {
    private Long purchaseId; //整单id
    private List<Long> items; //采购需求id
}
