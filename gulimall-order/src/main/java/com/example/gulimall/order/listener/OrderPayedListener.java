package com.example.gulimall.order.listener;

import com.alipay.easysdk.factory.Factory;
import com.example.gulimall.order.service.OrderService;
import com.example.gulimall.order.service.PaymentInfoService;
import com.example.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderPayedListener {
    @Autowired
    OrderService orderService;
    @PostMapping("/payed/notify")
    public String payedNotify(PayAsyncVo vo, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        for (String key: request.getParameterMap().keySet()) {
            map.put(key, request.getParameter(key));
        }
        System.out.println(vo+"\n"+map);
        if (vo.getSign() != null && vo.getSign_type() != null) {
            try {
                Boolean b = Factory.Payment.Common().verifyNotify(map);
                if (b) {
                    orderService.handlePayed(vo);
                    return "success";
                }
            } catch (Exception e) {
                System.err.println(e);
//                throw new RuntimeException(e);
            }
        }
        return "error";
    }
}
