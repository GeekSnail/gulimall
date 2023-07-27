package com.example.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonConfig {
    @Bean(destroyMethod="shutdown")
    RedissonClient redissonClient() throws IOException {
        Config config = new Config();
//        config.useClusterServers()
//                .addNodeAddress("redis://127.0.0.1:7004", "redis://127.0.0.1:7001");
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        return Redisson.create(config);
    }
}
