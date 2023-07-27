package com.example.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockLocked {
    long taskId;
    long taskDetailId;
    long orderId;
//    WareOrderTaskDetail taskDetail;
}
