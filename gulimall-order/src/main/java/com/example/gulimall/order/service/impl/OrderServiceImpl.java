package com.example.gulimall.order.service.impl;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.common.constant.OrderConstant;
import com.example.common.exception.NoStockException;
import com.example.common.to.WareSkuLock;
import com.example.common.to.mq.SeckillOrder;
import com.example.common.utils.R;
import com.example.gulimall.order.entity.OrderItemEntity;
import com.example.gulimall.order.entity.PaymentInfoEntity;
import com.example.gulimall.order.feign.CartFeignService;
import com.example.gulimall.order.feign.MemberFeignService;
import com.example.gulimall.order.feign.ProductFeignService;
import com.example.gulimall.order.feign.WareFeignService;
import com.example.gulimall.order.interceptor.LoginInterceptor;
import com.example.gulimall.order.service.OrderItemService;
import com.example.gulimall.order.service.PaymentInfoService;
import com.example.gulimall.order.vo.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.order.dao.OrderDao;
import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmit> submitThreadLocal = new ThreadLocal<>();
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${alipay.pagePayReturnUrl}")
    String pagePayReturnUrl;
    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils pageWithItems(Map<String, Object> params) {
        Map<String,Object> map = LoginInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
            new Query<OrderEntity>().getPage(params),
            new QueryWrapper<OrderEntity>().eq("member_id", map.get("id"))
                    .orderByDesc("id")
        );
        List<String> orderSns = page.getRecords().stream().map(order -> order.getOrderSn()).collect(Collectors.toList());
        Map<String, List<OrderItemEntity>> orderItemsMap = new HashMap<>();
        orderItemService.listByOrderSns(orderSns).forEach(item -> {
            String orderSn = item.getOrderSn();
            if (orderItemsMap.containsKey(orderSn))
                orderItemsMap.get(orderSn).add(item);
            else
                orderItemsMap.put(orderSn, List.of(item));
        });
        List<OrderEntity> records = page.getRecords().stream().map(order -> {
            order.setItems(orderItemsMap.get(order.getOrderSn()));
            return order;
        }).collect(Collectors.toList());
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        return getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public String getOrderPay(String orderSn) throws Exception {
        OrderEntity order = getByOrderSn(orderSn);
        String amount = order.getPayAmount().setScale(2, RoundingMode.HALF_UP).toString();
        OrderItemEntity orderItem = orderItemService.getOne(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        Calendar cld = Calendar.getInstance();
        cld.setTime(order.getCreateTime());
        cld.add(Calendar.MINUTE, 1);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cld.getTime());
        AlipayTradePagePayResponse res = Factory.Payment.Page().optional("time_expire", time).pay(orderItem.getSkuName(), orderSn, amount, pagePayReturnUrl);
        return res.getBody();
    }

    @Override
    public void handlePayed(PayAsyncVo vo) throws ParseException {
        //交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        String orderSn = vo.getOut_trade_no();
        infoEntity.setOrderSn(orderSn);
        String tradeStatus = vo.getTrade_status();
        infoEntity.setPaymentStatus(tradeStatus);
        infoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(infoEntity);
        //更新订单状态
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            baseMapper.updateStatus(orderSn, OrderConstant.OrderStatusEnum.PAYED.getCode());
        }
    }

    @Override
    public OrderConfirm confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirm confirm = new OrderConfirm();
        Map<String,Object> userMap = LoginInterceptor.threadLocal.get();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        //远程查询选中的购物项
        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItem> items = cartFeignService.currentUserCartItems();
            confirm.setItems(items);
        }, executor);
        //远程查询收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddress> addresses = memberFeignService.getAddress(Long.parseLong(userMap.get("id").toString()));
            confirm.setAddress(addresses);
        }, executor);
        //用户积分
        confirm.setIntegration((Integer) userMap.get("integration"));
        //其他数据

        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + userMap.get("id");
        redisTemplate.opsForValue().set(key, token, 30, TimeUnit.MINUTES);
        confirm.setToken(token);

        CompletableFuture.allOf(addressFuture, itemsFuture).get();
        return confirm;
    }

    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResp submitOrder(OrderSubmit vo) {
        submitThreadLocal.set(vo);
        SubmitOrderResp resp = new SubmitOrderResp();
        Map<String,Object> userMap = LoginInterceptor.threadLocal.get();
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + userMap.get("id");
        //compareAndDelete
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //0-删除失败 1-删除成功
        Long ret = redisTemplate.execute(RedisScript.of(script, Long.class), List.of(key), vo.getToken());
        if (ret == 0)
            resp.setCode(1); //订单信息过期 请重新提交
        else {
            List<OrderItem> items = cartFeignService.currentUserCartItems();
            if (items != null && items.size() > 0) {
                List<Long> skuIds = items.stream().map(it -> it.getSkuId()).collect(Collectors.toList());
                String orderSn = IdWorker.getTimeId();
                //创建订单
                OrderEntity orderEntity = buildOrder(skuIds, orderSn);
                //创建订单购物项
                List<OrderItemEntity> itemEntities = buildOrderItems(items, orderSn);
                if (orderEntity != null && itemEntities != null && itemEntities.size() > 0) {
                    //计算订单价格与积分抵扣
                    computeAmount(orderEntity, itemEntities);
                    //验价
                    BigDecimal payAmount = orderEntity.getPayAmount();
                    BigDecimal payPrice = vo.getPayPrice();
                    if (payAmount.subtract(payPrice).abs().floatValue() < 0.01) {
                        //保存订单
                        orderEntity.setCreateTime(new Date());
                        baseMapper.insert(orderEntity);
                        orderItemService.saveBatch(itemEntities);
                        //远程锁定库存 有异常则回滚对订单数据的保存
//                        List<Map<String, Long>> maps = itemEntities.stream().map(it -> Map.of("skuId", it.getSkuId(), "count", it.getSkuQuantity().longValue())).collect(Collectors.toList());
                        List<WareSkuLock.SkuCount> locks = itemEntities.stream().map(it -> new WareSkuLock.SkuCount(it.getSkuId(), it.getSkuQuantity())).collect(Collectors.toList());
                        WareSkuLock wareSkuLock = new WareSkuLock(orderEntity.getId(), orderSn, locks);
                        R r = wareFeignService.lockStock(wareSkuLock);
                        if (r.getCode() == 0) {
                            resp.setOrder(orderEntity);
                        } else {
                            resp.setCode(3); //订单商品存在库存不足
                            throw new NoStockException(r.getMsg());
                        }
                        //TODO 远程更新用户积分 模拟异常
//                        throw new RuntimeException("测试抛异常后，分布式事务回滚！");
                        //订单创建成功 发消息给mq
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderEntity.getId());
                    } else {
                        resp.setCode(2); //订单商品存在金额变动 请确认后再次提交
                    }
                }
            }
        }
        return resp;
    }

    @Override
    public void createOrder(SeckillOrder seckillOrder) {
        //TODO 订单
        OrderEntity entity = this.getByOrderSn(seckillOrder.getOrderSn());
        if (entity == null) {
            entity = new OrderEntity();
            entity.setOrderSn(seckillOrder.getOrderSn());
            entity.setMemberId(seckillOrder.getMemberId());
            entity.setStatus(OrderConstant.OrderStatusEnum.CREATED.getCode());
            BigDecimal amount = seckillOrder.getSeckillPrice().multiply(new BigDecimal(seckillOrder.getCount()));
            entity.setPayAmount(amount);
            entity.setCreateTime(new Date());
            this.save(entity);
            //TODO 订单项
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderSn(seckillOrder.getOrderSn());
            orderItem.setSkuId(seckillOrder.getSkuId());
            orderItem.setRealAmount(amount);
            orderItem.setSkuQuantity(seckillOrder.getCount());
            R r = productFeignService.skuinfo(seckillOrder.getSkuId());
            if (r.getCode() == 0) {
                Map<String,Object> skuInfo = (Map<String, Object>) r.get("skuInfo");
                orderItem.setSkuName((String) skuInfo.get("skuName"));
                orderItem.setSkuPic((String) skuInfo.get("sku_default_img"));
                orderItemService.save(orderItem);
            }
        }
    }

    @Override
    public void closeOrder(Long orderId) {
        OrderEntity entity = getById(orderId);
        if (entity.getStatus() == OrderConstant.OrderStatusEnum.CREATED.getCode()) {
            entity.setStatus(OrderConstant.OrderStatusEnum.CANCELED.getCode());
            updateById(entity);
            //取消订单后通知 stock.release.queue
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.stock", orderId);
        }
    }

    //订单项的价格与积分/抵扣汇总
    private void computeAmount(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        int giftGrowth = 0;
        int giftIntegeration = 0;
        for (OrderItemEntity item: itemEntities) {
            total = total.add(item.getRealAmount());
            coupon = coupon.add(item.getCouponAmount());
            promotion = promotion.add(item.getPromotionAmount());
            integration = integration.add(item.getIntegrationAmount());
            giftGrowth += item.getGiftGrowth();
            giftIntegeration += item.getGiftIntegration();
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount())); //应付总额
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegeration);
    }

    private OrderEntity buildOrder(List<Long> skuIds, String orderSn) {
        OrderSubmit submitVo = submitThreadLocal.get();
        R r = wareFeignService.fare(submitVo.getAddrId(), skuIds);
        if (r.getCode() == 0) {
            Map<String,Object> map = (Map<String, Object>) r.get("data");
            Map<String,Object> userMap = LoginInterceptor.threadLocal.get();
            OrderEntity entity = new OrderEntity();
            entity.setOrderSn(orderSn);
            entity.setMemberId(Long.parseLong(userMap.get("id").toString()));
            entity.setMemberUsername((String) userMap.get("username"));
            entity.setFreightAmount(new BigDecimal((int)map.get("fare"))); //运费
            entity.setReceiverProvince((String) map.get("recvProvince"));
            entity.setReceiverCity((String) map.get("recvCity"));
            entity.setReceiverRegion((String) map.get("recvRegion"));
            entity.setReceiverDetailAddress((String) map.get("recvDetailAddress"));
            entity.setReceiverName((String) map.get("recvName"));
            entity.setReceiverPhone((String) map.get("recvPhone"));
            entity.setDeleteStatus(0);
            entity.setStatus(OrderConstant.OrderStatusEnum.CREATED.getCode());
            return entity;
        }
        return null;
    }

    private List<OrderItemEntity> buildOrderItems(List<OrderItem> items, String orderSn) {
        List<Long> skuIds = items.stream().map(it -> it.getSkuId()).collect(Collectors.toList());
        R r = productFeignService.getBySkuIds(skuIds);
        if (r.getCode() == 0) {
            Map<Long, Map<String, Object>> map = (Map<Long, Map<String, Object>>) r.get("data");
            return items.stream().map(it -> {
                OrderItemEntity entity = new OrderItemEntity();
                entity.setOrderSn(orderSn);
                //商品spu信息
                Map<String, Object> spuInfo = map.get(it.getSkuId().toString());
                entity.setSpuId(Long.parseLong(spuInfo.get("id").toString()));
                entity.setSpuName((String) spuInfo.get("spu_name"));
                //TODO brand_id or brand_name?
//                entity.setSpuBrand(spuInfo.get("brand_id").toString());
                entity.setCategoryId(Long.parseLong(spuInfo.get("catalog_id").toString()));
                //商品sku信息
                entity.setSkuId(it.getSkuId());
                entity.setSkuName(it.getTitle());
                entity.setSkuPic(it.getImage());
                entity.setSkuPrice(it.getPrice());
                String skuAttr = StringUtils.collectionToDelimitedString(it.getSkuAttr(), ";");
                entity.setSkuAttrsVals(skuAttr);
                entity.setSkuQuantity(it.getCount());
                //优惠信息
                //积分信息
                Integer n = it.getPrice().intValue()*it.getCount();
                entity.setGiftGrowth(n);
                entity.setGiftIntegration(n);
                //价格
                entity.setPromotionAmount(new BigDecimal(0));
                entity.setCouponAmount(new BigDecimal(0));
                entity.setIntegrationAmount(new BigDecimal(0));
                //实际价格=单品总额-抵扣
                BigDecimal origin = it.getPrice().multiply(new BigDecimal(it.getCount()));
                entity.setRealAmount(origin.subtract(entity.getPromotionAmount())
                        .subtract(entity.getCouponAmount()).subtract(entity.getIntegrationAmount()));
                return entity;
            }).collect(Collectors.toList());
        }
        return null;
    }

}