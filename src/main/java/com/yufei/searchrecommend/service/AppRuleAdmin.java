/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.service;

import com.yufei.searchrecommend.entity.EncounterRecommendAppRule;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.RecommendAppRule;
import com.yufei.searchrecommend.entity.SearchAppRule;
import com.yufei.searchrecommend.entity.SortItem;
import com.yufei.searchrecommend.service.impl.EncouterUserRecommender;
import com.yufei.searchrecommend.service.impl.InterestedUsersRecommender;
import com.yufei.searchrecommend.service.impl.SimilarUsersRecommender;
import com.yufei.searchrecommend.utils.Constants;
import com.yufei.searchrecommend.utils.SpringUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author jasstion
 */
public class AppRuleAdmin {

    private final static GsonBuilder g = new GsonBuilder();

    private final static Gson gson = g.create();

    protected static final RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) SpringUtils.getBeanFromBeanContainer("redisTemplate");

    public static void createSearchAppRule(SearchAppRule searchAppRule) {
        redisTemplate.opsForValue().set(searchAppRule.getAppId(), gson.toJson(searchAppRule));

    }

    public static void createRecommendAppRule(RecommendAppRule searchAppRule) {
        redisTemplate.opsForValue().set(searchAppRule.getAppId(), gson.toJson(searchAppRule));

    }

    public static SearchAppRule getSearchAppRule(String appKey) {
        if (appKey == null || appKey.trim().length() < 1) {
            return null;
        }
        SearchAppRule searchAppRule = null;
        String sarStr = redisTemplate.opsForValue().get(appKey);
        if (sarStr != null) {
            searchAppRule = gson.fromJson(sarStr, SearchAppRule.class);
        }
        return searchAppRule;

    }

    @Deprecated
    public static void createRulesForRecommend(String[] args) {
//        - 帐号状态正常 (accountStatus=1)
//　　- 排除已婚帐号 (marriage=[1 TO 3])
//　　- 有头像帐号 (hasMainPhoto=1)
        //用户最后登录时间 逆向排序

        List<QueryItem> queryItems = Lists.newArrayList();
        QueryItem queryItem1 = new QueryItem("accountStatus", Lists.newArrayList("1"));
        queryItem1.setIsFilterType(true);
        QueryItem queryItem2 = new QueryItem("marriage", Lists.newArrayList("1#TO#3"));
        QueryItem queryItem3 = new QueryItem("hasMainPhoto", Lists.newArrayList("1"));
        queryItem2.setIsFilterType(true);
        queryItem3.setIsFilterType(true);
        queryItems.add(queryItem1);
        queryItems.add(queryItem2);
        queryItems.add(queryItem3);

        List<SortItem> sortItems = Lists.newArrayList();
        SortItem si = new SortItem();
        si.setFieldName("lastLoginTime");
        si.setSort("desc");
        sortItems.add(si);

        String filterStr = gson.toJson(queryItems);
        String sortStr = gson.toJson(sortItems);
        SearchAppRule sar = new SearchAppRule(Constants.app_prefix + SimilarUsersRecommender.appKey, "SimilarUsersRecommender");
        sar.setFilter(filterStr);
        sar.setSort(sortStr);
        SearchAppRule sar1 = new SearchAppRule(Constants.app_prefix + InterestedUsersRecommender.appKey, "InterestedUsersRecommender");
        sar1.setFilter(filterStr);
        sar1.setSort(sortStr);
        AppRuleAdmin.createSearchAppRule(sar1);
        AppRuleAdmin.createSearchAppRule(sar);

    }

    public static void createRecommendAppRules() {
//        - 帐号状态正常 (accountStatus=1)
//　　- 排除已婚帐号 (marriage=[1 TO 3])
//　　- 有头像帐号 (hasMainPhoto=1)
        //用户最后登录时间 逆向排序

        List<QueryItem> queryItems = Lists.newArrayList();
        QueryItem queryItem1 = new QueryItem("accountStatus", Lists.newArrayList("1"));
        queryItem1.setIsFilterType(true);
        QueryItem queryItem2 = new QueryItem("marriage", Lists.newArrayList("1#TO#3"));
        QueryItem queryItem3 = new QueryItem("hasMainPhoto", Lists.newArrayList("1"));
        queryItem2.setIsFilterType(true);
        queryItem3.setIsFilterType(true);
        QueryItem queryItem4 = new QueryItem("userID", Lists.newArrayList("1#TO#*"));
        queryItem4.setIsFilterType(true);
        queryItems.add(queryItem1);
        queryItems.add(queryItem2);
        queryItems.add(queryItem3);
        queryItems.add(queryItem4);

        List<SortItem> sortItems = Lists.newArrayList();
        SortItem si = new SortItem();
        si.setFieldName("lastLoginTime");
        si.setSort("desc");
        sortItems.add(si);

        String filterStr = gson.toJson(queryItems);
        String sortStr = gson.toJson(sortItems);
        RecommendAppRule sar = new RecommendAppRule(SimilarUsersRecommender.appKey, "SimilarUsersRecommender", filterStr, sortStr, null, false, false);
        RecommendAppRule sar1 = new RecommendAppRule(InterestedUsersRecommender.appKey, "InterestedUsersRecommender", filterStr, sortStr, null, false, false);
        String filterStr_encouter = getEncouterFilterStr();

        EncounterRecommendAppRule encouterRecommendAppRule = new EncounterRecommendAppRule(EncouterUserRecommender.appKey, "EncouterUsersRecommender", filterStr_encouter, null, null, false, false);
        encouterRecommendAppRule.setIdentityWeight(2000f);
        encouterRecommendAppRule.setMarriageStatusWeight(6000);

        encouterRecommendAppRule.setAgeWeight(400f);
        encouterRecommendAppRule.setHeightWeight(400f);
        encouterRecommendAppRule.setLocationWeight(300f);
        encouterRecommendAppRule.setLastLoginTimeWeight(100f);
        encouterRecommendAppRule.setDistrictWeight(200f);
        encouterRecommendAppRule.setCityWeight(100f);
        encouterRecommendAppRule.setDupDays(10);
        sar.setCacheStrategy("100");
        sar1.setCacheStrategy("100");
        AppRuleAdmin.createRecommendAppRule(sar1);
        AppRuleAdmin.createRecommendAppRule(sar);
        AppRuleAdmin.createRecommendAppRule(encouterRecommendAppRule);

    }

    public static String getEncouterFilterStr() {
        //邂逅推荐
        List<QueryItem> queryItems = Lists.newArrayList();
        QueryItem queryItem1 = new QueryItem("accountStatus", Lists.newArrayList("1"));
        queryItem1.setIsFilterType(true);
        QueryItem queryItem2 = new QueryItem("marriage", Lists.newArrayList("1#TO#3"));
        QueryItem queryItem3 = new QueryItem("hasPhoto", Lists.newArrayList("1"));
        queryItem2.setIsFilterType(true);
        queryItem3.setIsFilterType(true);
        QueryItem queryItem4 = new QueryItem("userID", Lists.newArrayList("1#TO#*"));
        queryItem4.setIsFilterType(true);
        queryItems.add(queryItem1);
        queryItems.add(queryItem2);
        queryItems.add(queryItem3);
        queryItems.add(queryItem4);

        String filterStr = gson.toJson(queryItems);
        return filterStr;
    }

    public static void main(String[] args) {
        createRecommendAppRules();
    }

}
