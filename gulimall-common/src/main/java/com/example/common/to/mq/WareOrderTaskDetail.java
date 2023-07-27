package com.example.common.to.mq;

import lombok.Data;

@Data
public class WareOrderTaskDetail {
    private Long id;
    private Long skuId;
    private String skuName;
    private Integer skuNum;
    private Long taskId;
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;
}
