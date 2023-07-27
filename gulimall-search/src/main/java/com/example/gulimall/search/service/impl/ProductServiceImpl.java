package com.example.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.example.common.to.es.SkuModel;
import com.example.gulimall.search.constant.EsConstant;
import com.example.gulimall.search.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ElasticsearchClient client;
    @Override
    public boolean spuUp(List<SkuModel> skuModels) throws IOException {
        BooleanResponse exists = client.indices().exists(e -> e.index(EsConstant.PRODUCT_INDEX));
        if (!exists.value()) {
            createIndex();
        }
        List<BulkOperation> ios = skuModels.stream().map(sku -> BulkOperation
                .of(op -> op.index(i -> i.index(EsConstant.PRODUCT_INDEX).id(sku.getSkuId()+"").document(sku))))
                .collect(Collectors.toList());
        BulkResponse res = client.bulk(b -> b.operations(ios));
        return !res.errors();
    }

    @Override
    public void createIndex() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/index/product.json");
//        CreateIndexRequest request = CreateIndexRequest.of(b -> b.index(EsConstant.PRODUCT_INDEX).withJson(inputStream));
        CreateIndexResponse res = client.indices().create(b -> b.index(EsConstant.PRODUCT_INDEX).withJson(inputStream));
        System.out.println(res.acknowledged());
    }
}
