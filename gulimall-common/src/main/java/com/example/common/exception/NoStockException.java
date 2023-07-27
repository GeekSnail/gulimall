package com.example.common.exception;

public class NoStockException extends RuntimeException {
    private long skuId;
    public NoStockException(String msg) {
        super(msg);
    }
    public NoStockException(Long skuId) {
        super("Sku:"+skuId+" haven't enough stock!");
        this.skuId = skuId;
    }
    public long getSkuId() {
        return skuId;
    }
}
