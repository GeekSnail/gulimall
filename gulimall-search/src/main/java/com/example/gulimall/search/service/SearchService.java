package com.example.gulimall.search.service;

import com.example.gulimall.search.vo.SearchParam;
import com.example.gulimall.search.vo.SearchResult;

public interface SearchService {
    SearchResult search(SearchParam searchParam);
}
