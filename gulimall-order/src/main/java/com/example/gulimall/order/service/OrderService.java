package com.example.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.to.mq.SeckillOrder;
import com.example.common.utils.PageUtils;
import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.vo.OrderConfirm;
import com.example.gulimall.order.vo.OrderSubmit;
import com.example.gulimall.order.vo.PayAsyncVo;
import com.example.gulimall.order.vo.SubmitOrderResp;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 02:30:18
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
    PageUtils pageWithItems(Map<String, Object> params);

    OrderConfirm confirmOrder() throws ExecutionException, InterruptedException; //订单确认页数据

    SubmitOrderResp submitOrder(OrderSubmit vo);

    void closeOrder(Long orderId);

    OrderEntity getByOrderSn(String orderSn);

    String getOrderPay(String orderSn) throws Exception;

    void handlePayed(PayAsyncVo vo) throws ParseException;

    void createOrder(SeckillOrder seckillOrder);
}

