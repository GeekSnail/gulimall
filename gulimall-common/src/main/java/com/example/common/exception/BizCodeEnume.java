package com.example.common.exception;

/**
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景，最后3位表示错误码。如: 10001, 10:通用 001:系统未知异常
 * 3. 维护错误码后需维护错误描述，将它们定义为枚举形式
 * 错误码列表：
 * 10 通用
 *    001 参数格式校验
 *    002 短信验证码频率太高
 * 11 商品
 * 12 订单
 * 13 购物车
 * 14 库存
 * 15 用户
 */
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "获取验证码频率太高，请稍后再试"),
    TOO_MANY_REQUESTS(10003, "请求频率过高"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXISTED_EXCEPTION(15001, "用户名已存在"),
    MOBILE_EXISTED_EXCEPTION(15002, "手机号已存在"),
    ACCOUNT_PASSWORD_INVALID_EXCEPTION(15003, "账号或密码错误"),
    NO_STOCK_EXCEPTION(14000, "商品库存不足");
    private int code;
    private String msg;
    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
