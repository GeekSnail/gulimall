package com.example.gulimall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateStockLockedVo {
    Long skuId;
    Integer count;
    long wareId;
}
