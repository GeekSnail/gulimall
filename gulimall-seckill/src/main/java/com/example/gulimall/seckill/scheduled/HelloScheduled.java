package com.example.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableAsync
@EnableScheduling
@Slf4j
//@Component
public class HelloScheduled {
    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello() throws InterruptedException {
        log.info("hello."+Thread.currentThread().getId());
        Thread.sleep(3000);
    }

}
