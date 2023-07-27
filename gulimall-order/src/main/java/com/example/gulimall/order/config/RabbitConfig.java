package com.example.gulimall.order.config;

import com.example.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    //测试延时&死信队列
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }
    @Bean
    public Queue orderCreateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange");
        args.put("x-dead-letter-routing-key", "order.release");
        args.put("x-message-ttl", TimeUnit.MINUTES.toMillis(1));
        return new Queue("order.delay.queue", true, false, false, args);
    }
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }
    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.#", null);
    }
    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release", null);
    }
    @Bean
    public Binding orderReleaseStockBinding() {
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.stock.#", null);
    }
    @Bean
    public Queue orderSeckillQueue() {
        return new Queue("order.seckill.queue", true, false, false);
    }
    @Bean
    public Binding orderSeckillBinding() {
        return new Binding("order.seckill.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.seckill", null);
    }

//    @RabbitListener(queues="order.release.queue")
//    public void listener(Channel channel, Message message, OrderEntity entity) throws IOException {
//        System.out.println("received expired order, preparing to close order:" + entity.getOrderSn());
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }
}
