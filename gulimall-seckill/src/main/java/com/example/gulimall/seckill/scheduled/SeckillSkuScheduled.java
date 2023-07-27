package com.example.gulimall.seckill.scheduled;

import com.example.gulimall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品定时上架
 *  每天晚上 上架将来最近3天需秒杀的商品
 *  今天00:00:00-后天23:59:59
 */
@EnableAsync
@EnableScheduling
@Component
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;
    private final String UPLOAD_LOCK = "seckill:upload:lock";
    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest() {
        System.out.println("scheduling");
        //分布式锁
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest(3);
        } finally {
            lock.unlock();
        }
    }
}
