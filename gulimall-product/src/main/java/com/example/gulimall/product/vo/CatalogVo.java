package com.example.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CatalogVo {
    String id;
    String name;
    String parentCid;
    List<CatalogVo> children;
}
