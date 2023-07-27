package com.example.gulimall.ware.listener;

import com.example.common.to.mq.StockLocked;
import com.example.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@RabbitListener(queues = {"stock.release.queue"})
@Component
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;
    @RabbitHandler
    public void handleStockLockedRelease(Channel channel, Message message, StockLocked stockLocked) throws IOException {
        System.out.println(new Date() + "\n" + message.getMessageProperties() + "\n" + stockLocked);
        try {
            wareSkuService.unlockStock(stockLocked);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException | RuntimeException e) {
            System.err.println(e);
            //数据库|远程|channel请求异常 拒绝消息并重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
    @RabbitHandler
    public void handleOrderRelease(Channel channel, Message message, Long orderId) throws IOException {
        System.out.println(new Date() + "\n" + message.getMessageProperties() + "\norderId=" + orderId);
        try {
            wareSkuService.unlockStock(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException | RuntimeException e) {
            System.err.println(e);
            //数据库|channel请求异常 拒绝消息并重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
