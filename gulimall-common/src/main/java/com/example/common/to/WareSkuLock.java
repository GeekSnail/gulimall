package com.example.common.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WareSkuLock {
    Long orderId;
    String orderSn;
    List<SkuCount> locks;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuCount {
        Long skuId;
        Integer count;
    }
}
