package com.example.gulimall.order.controller;

import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitController {
//    @Autowired
//    RabbitTemplate rabbitTemplate;
//    @GetMapping("/sendmq")
//    public String sendMQ(@RequestParam(value="n", defaultValue="10") int n) {
//        for (int i=0; i<10; i++) {
//            if (i%2 == 0) {
//                OrderReturnReasonEntity reason = new OrderReturnReasonEntity();
//                reason.setId(1L);
//                reason.setCreateTime(new Date());
//                reason.setName("test"+i);
//                rabbitTemplate.convertAndSend("hello-exchange", "hello", reason);
//            } else {
//                OrderEntity order = new OrderEntity();
//                order.setOrderSn(UUID.randomUUID().toString());
//                rabbitTemplate.convertAndSend("hello-exchange", "hello", order);
//            }
//        }
//        return "ok";
//    }
//    @GetMapping("/createorder")
//    public String sendOrderCreateMessage() {
//        OrderEntity entity = new OrderEntity();
//        entity.setOrderSn(UUID.randomUUID().toString());
//        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
//        return "ok";
//    }
}
