package com.example.gulimall.member.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    Integer coreSize;
    Integer maxSize;
    Integer keepLiveTime; //seconds
}
