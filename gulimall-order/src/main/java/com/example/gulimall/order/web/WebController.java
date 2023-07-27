package com.example.gulimall.order.web;

import com.alipay.easysdk.factory.Factory;
import com.example.common.constant.OrderConstant;
import com.example.common.exception.NoStockException;
import com.example.common.utils.PageUtils;
import com.example.gulimall.order.entity.OrderEntity;
import com.example.gulimall.order.service.OrderService;
import com.example.gulimall.order.vo.OrderConfirm;
import com.example.gulimall.order.vo.OrderSubmit;
import com.example.gulimall.order.vo.SubmitOrderResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
public class WebController {
    @Autowired
    OrderService orderService;
    @GetMapping("/confirm")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirm orderConfirm = orderService.confirmOrder();
        model.addAttribute("orderConfirm", orderConfirm);
        return "confirm";
    }
    @PostMapping("/submitOrder")
    public String submitOrder(@Valid OrderSubmit vo, Model model, RedirectAttributes ra) {
        try {
            SubmitOrderResp resp = orderService.submitOrder(vo); //下单
//            System.out.println(resp);
            if (resp.getCode() == 0) {
                model.addAttribute("order", resp.getOrder());
                return "pay";
            }
            String msg = "下单失败！";
            switch (resp.getCode()) {
                case 1: msg += "订单信息过期 请重新提交";break;
                case 2: msg += "订单商品存在金额变动 请确认后再次提交";break;
                case 3: msg += "订单商品存在库存不足";break; //no
            }
            ra.addFlashAttribute("msg", msg);
            return "redirect://order.gulimall.com/confirm";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect://order.gulimall.com/confirm";
        }
    }

    @ResponseBody
    @GetMapping(value = "/pay", produces = "text/html")
    public String pay(@RequestParam("orderSn") String orderSn) {
        try {
            String html = orderService.getOrderPay(orderSn);
            return html;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/list")
    public String list(@RequestParam(value="pageNum",defaultValue = "1") String pageNum, Model model) {
        PageUtils page = orderService.pageWithItems(new HashMap<>(Map.of("page", pageNum)));
        model.addAttribute("page", page);
        Map<Integer, String> statusMap = Arrays.stream(OrderConstant.OrderStatusEnum.values())
                .collect(Collectors.toMap(e -> e.getCode(), e -> e.getMsg()));
        model.addAttribute("statusMap", statusMap);
        return "list";
    }
}
