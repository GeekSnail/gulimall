package com.example.gulimall.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
//    @Bean
//    public Exchange orderEventExchange() {
//        return new TopicExchange("order-event-exchange", true, false);
//    }
//    @Bean
//    public Queue orderCreateQueue() {
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-dead-letter-exchange", "order-event-exchange");
//        args.put("x-dead-letter-routing-key", "order.release");
//        args.put("x-message-ttl", TimeUnit.MINUTES.toMillis(1));
//        return new Queue("order.delay.queue", true, false, false, args);
//    }
//    @Bean
//    public Queue orderReleaseQueue() {
//        return new Queue("order.release.queue", true, false, false);
//    }
//    @Bean
//    public Binding orderCreateBinding() {
//        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.#", null);
//    }
//    @Bean
//    public Binding orderReleaseBinding() {
//        return new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release", null);
//    }
//    @Bean
//    public Binding orderReleaseStockBinding() {
//        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.stock.#", null);
//    }
}
