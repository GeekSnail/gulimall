package com.example.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.example.common.to.es.SkuModel;
import com.example.gulimall.search.constant.EsConstant;
import com.example.gulimall.search.service.SearchService;
import com.example.gulimall.search.vo.SearchParam;
import com.example.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    ElasticsearchClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            SearchResponse<SkuModel> resp = client.search(searchRequest, SkuModel.class);
            result = buildSearchResult(resp, param);
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchRequest.Builder sbuilder = new SearchRequest.Builder();
        sbuilder.index(EsConstant.PRODUCT_INDEX);
        BoolQuery.Builder bbuilder = new BoolQuery.Builder();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            bbuilder.must(mu -> mu.match(m -> m.field("skuTitle").query(param.getKeyword())));
        }
        if (param.getCatalog3Id() != null) {
            bbuilder.filter(f -> f.term(t -> t.field("catalogId").value(param.getCatalog3Id())));
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<FieldValue> values = param.getBrandId().stream().map(id -> FieldValue.of(id)).collect(Collectors.toList());
            bbuilder.filter(f -> f.terms(t -> t.field("brandId").terms(te -> te.value(values))));
        }
        if (param.getHasStock() != null) {
            bbuilder.filter(f -> f.term(t -> t.field("hasStock").value(param.getHasStock() == 1)));
        }
        //1_500|_500|500_
        String price = param.getSkuPrice();
        if (!StringUtils.isEmpty(price)) {
            String[] s = price.split("_");
            if (s.length == 2) {
                bbuilder.filter(f -> f.range(r -> r.field("skuPrice").gte(JsonData.of(s[0])).lte(JsonData.of(s[1]))));
            } else if (s.length == 1) {
                if (price.startsWith("_")) {
                    bbuilder.filter(f -> f.range(r -> r.field("skuPrice").lte(JsonData.of(s[0]))));
                } else if (price.endsWith("_")) {
                    bbuilder.filter(f -> f.range(r -> r.field("skuPrice").gte(JsonData.of(s[0]))));
                }
            }
        }
        // {attrId}_{attrValue1}:{attrValue2}
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            BoolQuery.Builder bqbuilder = new BoolQuery.Builder();
            for (String attr : param.getAttrs()) {
                String[] s = attr.split("_");
                List<FieldValue> values = Arrays.stream(s[1].split(":")).map(v -> FieldValue.of(v)).collect(Collectors.toList());
                bqbuilder.must(m -> m.term(t -> t.field("attrs.attrId").value(s[0])));
                bqbuilder.must(m -> m.terms(t -> t.field("attrs.attrValue").terms(te -> te.value(values))));
                bbuilder.filter(f -> f.nested(n -> n.path("attrs").query(q -> q.bool(bqbuilder.build()))));
            }
        }
        sbuilder.query(q -> q.bool(bbuilder.build()));
        //sort=hotScore_asc|desc
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] str = sort.split("_");
            SortOrder order = str[1].toLowerCase().equals("asc") ? SortOrder.Asc : (str[1].toLowerCase().equals("desc") ? SortOrder.Desc : null);
            if (order != null) {
                sbuilder.sort(s -> s.field(FieldSort.of(b -> b.field(str[0]).order(order))));
            }
        }
        // from=(pageNum-1)*pageSize
        sbuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sbuilder.size(EsConstant.PRODUCT_PAGESIZE);
        if (!StringUtils.isEmpty(param.getKeyword())) {
            sbuilder.highlight(h -> h.fields("skuTitle", HighlightField.of(b -> b)).preTags("<b style='color:red'>").postTags("</b>"));
        }
        sbuilder.aggregations("brand_agg", b -> b.terms(t -> t.field("brandId").size(10))
                        .aggregations("brand_name_agg", a -> a.terms(t -> t.field("brandName")))
                        .aggregations("brand_img_agg", a -> a.terms(t -> t.field("brandImg"))))
                .aggregations("catalog_agg", b -> b.terms(t -> t.field("catalogId"))
                        .aggregations("catalog_name_agg", a -> a.terms(t -> t.field("catalogName"))))
                .aggregations("attrs_agg", b -> b.nested(n -> n.path("attrs"))
                        .aggregations("attr_agg", a -> a.terms(t -> t.field("attrs.attrId"))
                                .aggregations("attr_name_agg", ag -> ag.terms(t -> t.field("attrs.attrName")))
                                .aggregations("attr_value_agg", ag -> ag.terms(t -> t.field("attrs.attrValue")))
                        )
                );
        return sbuilder.build();
    }

    private SearchResult buildSearchResult(SearchResponse<SkuModel> resp, SearchParam param) {
        SearchResult result = new SearchResult();
        HitsMetadata<SkuModel> hits = resp.hits();
        long total = hits.total().value();
        result.setTotal(total);
        int pages = (int) (total / EsConstant.PRODUCT_PAGESIZE);
        result.setTotalPages(total % EsConstant.PRODUCT_PAGESIZE == 0 ? pages : pages + 1);
        result.setPageNum(param.getPageNum());
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<Long> attrIds = param.getAttrs().stream().map(a -> Long.parseLong(a.split("_")[0])).collect(Collectors.toList());
            result.setAttrIds(attrIds);
        }
        List<SkuModel> skus = hits.hits().stream().map(h -> {
            SkuModel skuModel = h.source();
            if (!StringUtils.isEmpty(param.getKeyword())) {
                skuModel.setSkuTitle(h.highlight().get("skuTitle").get(0));
            }
            return skuModel;
        }).collect(Collectors.toList());
        result.setProducts(skus);
        List<SearchResult.CatalogVo> catalogVos = resp.aggregations().get("catalog_agg").lterms().buckets().array()
                .stream().map(b -> {
                    SearchResult.CatalogVo catalog = new SearchResult.CatalogVo();
                    catalog.setCatalogId(Long.parseLong(b.key()));
                    catalog.setCatalogName(b.aggregations().get("catalog_name_agg").sterms().buckets().array().get(0).key().stringValue());
                    return catalog;
                }).collect(Collectors.toList());
        result.setCatalogs(catalogVos);
        List<SearchResult.BrandVo> brandVos = resp.aggregations().get("brand_agg").lterms().buckets().array()
                .stream().map(b -> {
                    SearchResult.BrandVo brand = new SearchResult.BrandVo();
                    brand.setBrandId(Long.parseLong(b.key()));
                    List<StringTermsBucket> buckets = b.aggregations().get("brand_img_agg").sterms().buckets().array();
                    if (buckets.size() > 0) {
                        brand.setBrandImg(buckets.get(0).key().stringValue());
                    }
                    brand.setBrandName(b.aggregations().get("brand_name_agg").sterms().buckets().array().get(0).key().stringValue());
                    return brand;
                }).collect(Collectors.toList());
        result.setBrands(brandVos);
        List<SearchResult.AttrVo> attrVos = resp.aggregations().get("attrs_agg").nested()
                .aggregations().get("attr_agg").lterms().buckets().array().stream().map(b -> {
                    SearchResult.AttrVo attr = new SearchResult.AttrVo();
                    attr.setAttrId(Long.parseLong(b.key()));
                    attr.setAttrName(b.aggregations().get("attr_name_agg").sterms().buckets().array().get(0).key().stringValue());
                    List<String> attrValues = b.aggregations().get("attr_value_agg").sterms().buckets().array()
                            .stream().map(v -> v.key().stringValue()).collect(Collectors.toList());
                    attr.setAttrValue(attrValues);
                    return attr;
                }).collect(Collectors.toList());
        result.setAttrs(attrVos);
        return result;
    }
}
