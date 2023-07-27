package com.example.gulimall.order;

import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.entity.OrderReturnReasonEntity;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    AmqpAdmin amqpAdmin;
    @Test
    public void testAmqpAdmin() {
        DirectExchange exchange = new DirectExchange("hello-exchange", true, false);
        amqpAdmin.declareExchange(exchange);
        Queue queue = new Queue("hello-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        Binding binding = new Binding("hello-queue", Binding.DestinationType.QUEUE, "hello-exchange", "hello", null);
        amqpAdmin.declareBinding(binding);
    }
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void sendOrderReturnMessage() {
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setName("Jack");
        rabbitTemplate.convertAndSend("hello-exchange", "hello", entity);
    }
}
