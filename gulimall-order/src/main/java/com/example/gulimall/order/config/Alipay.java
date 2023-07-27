package com.example.gulimall.order.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class Alipay {
    String gatewayHost;
    String appId;
    String merchantPrivateKey;
    String alipayPublicKey;
    String notifyUrl;
    String test;
    @Bean
    public Config alipayConfig() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = gatewayHost;
        config.signType = "RSA2";
        config.appId = appId;
        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantPrivateKey = merchantPrivateKey;
        config.alipayPublicKey = alipayPublicKey;
        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = notifyUrl;
        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
//        config.encryptKey = "<-- 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA== -->";
        System.out.println(test);
        Factory.setOptions(config);
        return config;
    }
}
