package com.example.gulimall.order.listener;

import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.entity.OrderReturnReasonEntity;
import com.example.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@RabbitListener(queues={"order.release.queue"})
@Component
public class OrderReleaseListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void handleOrderRelease(Channel channel, Message message, Long orderId) throws IOException {
        System.out.println(new Date() + "\n" + message.getMessageProperties() + "\norderId=" + orderId);
        try {
            orderService.closeOrder(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException | RuntimeException e) {
            System.err.println(e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
