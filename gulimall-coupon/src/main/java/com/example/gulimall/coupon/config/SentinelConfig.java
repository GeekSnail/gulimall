package com.example.gulimall.coupon.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.example.common.exception.BizCodeEnume;
import com.example.common.utils.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.PrintWriter;

@Slf4j
@Configuration
public class SentinelConfig {
    @Autowired
    ObjectMapper om;
    @Bean
    public BlockExceptionHandler sentinelBlockExceptionHandler() {
        return (request, response, e) -> {
//            response.setStatus(429);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            R error = R.error(BizCodeEnume.TOO_MANY_REQUESTS.getCode(), BizCodeEnume.TOO_MANY_REQUESTS.getMsg());
            om.writeValue(out, error);
            log.warn("Oops, blocked by Sentinel: " + e.getClass().getSimpleName());
            out.flush();
            out.close();
        };
    }
}
