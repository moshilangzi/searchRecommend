/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.searchrecommend.service;

import com.comm.searchrecommend.entity.RecommendServiceRule;
import com.comm.searchrecommend.entity.SearchServiceRule;
import com.comm.searchrecommend.utils.SpringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author jasstion
 */
public class AppRuleAdmin {

    private final static GsonBuilder g = new GsonBuilder();

    private final static Gson gson = g.create();

    protected static final RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) SpringUtils.getBeanFromBeanContainer("redisTemplate");

    public static void createSearchAppRule(SearchServiceRule searchAppRule) {
        redisTemplate.opsForValue().set(searchAppRule.getServiceId(), gson.toJson(searchAppRule));

    }

    public static void createRecommendAppRule(RecommendServiceRule searchAppRule) {
        redisTemplate.opsForValue().set(searchAppRule.getServiceId(), gson.toJson(searchAppRule));

    }

    public static SearchServiceRule getSearchAppRule(String appKey) {
        if (appKey == null || appKey.trim().length() < 1) {
            return null;
        }
        SearchServiceRule searchAppRule = null;
        String sarStr = redisTemplate.opsForValue().get(appKey);
        if (sarStr != null) {
            searchAppRule = gson.fromJson(sarStr, SearchServiceRule.class);
        }
        return searchAppRule;

    }



    public static void createRecommendAppRules() {


    }







}
