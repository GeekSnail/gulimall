package com.example.gulimall.product.feign;

import com.example.common.exception.BizCodeEnume;
import com.example.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getSeckillSku(Long skuId) {
        log.info("SeckillFeignServiceFallback.getSeckillSku(skuId=" + skuId + ")");
        return R.error(BizCodeEnume.TOO_MANY_REQUESTS.getCode(), BizCodeEnume.TOO_MANY_REQUESTS.getMsg());
    }
}
