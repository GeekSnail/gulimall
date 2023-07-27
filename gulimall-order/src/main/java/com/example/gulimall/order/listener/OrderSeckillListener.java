package com.example.gulimall.order.listener;

import com.example.common.to.mq.SeckillOrder;
import com.example.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@RabbitListener(queues={"order.seckill.queue"})
@Component
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void handleOrderRelease(Channel channel, Message message, SeckillOrder seckillOrder) throws IOException {
        log.info(new Date() + "\n" + message.getMessageProperties() + "\nseckillOrder=" + seckillOrder);
        try {
            orderService.createOrder(seckillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException | RuntimeException e) {
            System.err.println(e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
