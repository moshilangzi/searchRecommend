/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.utils;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.SearchAppRule;
import com.yufei.searchrecommend.entity.SortItem;
import com.yufei.searchrecommend.service.AbstractQueryService;
import com.yufei.searchrecommend.service.AppRuleAdmin;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * used to convert input query into BaiheQuery object
 *
 * @author jasstion
 */
public class BaiheQueryHelper {

    protected static final RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) SpringUtils.getBeanFromBeanContainer("redisTemplate");


    public static BaiheQuery makeBaiheQuery(String queryJson, String appKey) {
        BaiheQuery query = Instances.gson.fromJson(queryJson, BaiheQuery.class);
        //get from config database.
        SearchAppRule searchAppRule = AppRuleAdmin.getSearchAppRule(appKey);
        String cacheStr = null;
        String sortStr = null;
        if (searchAppRule != null) {
            cacheStr = searchAppRule.getCacheStrategy();
            sortStr = searchAppRule.getSort();
        }

        // String filterStr = null;
        if (cacheStr != null && cacheStr.trim().length() > 0) {
            query.setCacheStrategy(cacheStr);
        }
        if (sortStr != null && sortStr.trim().length() > 0) {
            List<SortItem> sortItems_ = Instances.gson.fromJson(sortStr, new TypeToken<ArrayList<SortItem>>() {
            }.getType());
            query.setSortItems(sortItems_);
        }
        final List<QueryItem> queryItems = query.getQueryItems();
        if (queryItems != null) {
            for (QueryItem queryItem : queryItems) {
                if (AbstractQueryService.filterFieldNmae.contains(queryItem.getFieldName())) {
                    queryItem.setIsFilterType(true);
                }
            }

        }

        return query;
    }
    @Deprecated
    public static BaiheQuery makeBaiheQuery(BaiheQuery yufeiQuery, String appKey) {
        SearchAppRule searchAppRule = AppRuleAdmin.getSearchAppRule(appKey);
        if (searchAppRule != null) {
            String cacheStr = searchAppRule.getCacheStrategy();
            String sortStr = searchAppRule.getSort();
            String filterStr = searchAppRule.getFilter();
            if (cacheStr != null && cacheStr.trim().length() > 0) {
                yufeiQuery.setCacheStrategy(cacheStr);
            }
            if (sortStr != null && sortStr.trim().length() > 0) {
                List<SortItem> sortItems_ = Instances.gson.fromJson(sortStr, new TypeToken<ArrayList<SortItem>>() {
                }.getType());
                yufeiQuery.setSortItems(sortItems_);
            }

            if (filterStr != null && filterStr.trim().length() > 0) {
                List<QueryItem> filterItems = Instances.gson.fromJson(filterStr, new TypeToken<ArrayList<QueryItem>>() {
                }.getType());
                yufeiQuery.getQueryItems().addAll(filterItems);
            }

        }

        return yufeiQuery;
    }

}
