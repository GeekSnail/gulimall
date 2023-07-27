package com.example.common.constant;

public class ProductConstant {
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");
        private int value;
        private String desc;
        AttrEnum(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
        public int getValue() { return value; }
        public String getDesc() { return desc; }
    }
    public enum StatusEnum {
        SPU_NEW(0, "新建"),
        SPU_UP(1, "上架"),
        SPU_DOWN(2, "下架");
        private int value;
        private String desc;
        StatusEnum(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
        public int getValue() { return value; }
        public String getDesc() { return desc; }
    }
}
