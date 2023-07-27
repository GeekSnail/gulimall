package com.example.gulimall.search.service;

import com.example.common.to.es.SkuModel;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    boolean spuUp(List<SkuModel> skuModels) throws IOException;
    void createIndex() throws IOException;
}
