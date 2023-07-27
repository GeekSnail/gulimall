package com.example.common.constant;

public class OrderConstant {
    public static final String USER_ORDER_TOKEN_PREFIX = "order:token:";
    public enum OrderStatusEnum {
        CREATED(0, "待付款"), PAYED(1, "已付款"),
        SENDED(2, "已发货"), RECEIVED(3, "已完成"),
        CANCELED(4, "已取消"), SERVICING(5, "售后中"),
        SERVICED(6, "售后结束");
        private int code;
        private String msg;
        OrderStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public int getCode() { return code; }
        public String getMsg() { return msg; }
    }
}
