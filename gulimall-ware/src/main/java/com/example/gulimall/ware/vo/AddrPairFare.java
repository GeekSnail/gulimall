package com.example.gulimall.ware.vo;

import lombok.Data;

@Data
public class AddrPairFare {
    long wareId;
    String wareAddr;
    long recvAddrId;
    String recvProvince;
    String recvCity;
    String recvRegion;
    String recvDetailAddress;
    String recvName;
    String recvPhone;
    int fare;
}
