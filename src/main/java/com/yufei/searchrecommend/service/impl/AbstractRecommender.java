/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.RecommendAppRule;
import com.yufei.searchrecommend.entity.SortItem;
import com.yufei.searchrecommend.service.AbstractQueryService;
import com.yufei.searchrecommend.solr.SolrQueryService;
import com.yufei.searchrecommend.utils.HttpUtils;
import com.yufei.searchrecommend.utils.Instances;
import com.yufei.searchrecommend.utils.ReadProperties;
import com.yufei.searchrecommend.utils.SpringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author jasstion
 */
public class AbstractRecommender {

    /**
     *
     */
    protected static final RedisTemplate<String, Set> redisTemplate = (RedisTemplate<String, Set>) SpringUtils.getBeanFromBeanContainer("redisTemplate");

    /**
     *
     */
    protected static final RedisTemplate<String, String> redisTemplate_String = (RedisTemplate<String, String>) SpringUtils.getBeanFromBeanContainer("redisTemplate");

    /**
     *
     */
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractRecommender.class);

    /**
     *
     */
    protected AbstractQueryService<BaiheQuery> queryService = new SolrQueryService();

    /**
     *
     */
    protected String userRelationSign = null;

    /**
     *
     */
    public AbstractRecommender() {
        super();
    }

    /**
     *
     * @param userId
     * @return
     * @throws RuntimeException
     */
    protected Map<String, String> getUserInfoByUserId(String userId) throws RuntimeException {
        String usergeturl = ReadProperties.appBundle.getString("usergeturl");
        if (usergeturl == null || usergeturl.length() < 1) {
            throw new RuntimeException("please configure api to get user info.");
        }
        Map<String, String> user = null;
        if (userId != null && userId.trim().length() > 0) {
            Map<String, String> params = new HashMap<String, String>();
            // params.put("traceID", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            params.put("systemID", "sr");
            params.put("userID", String.valueOf(userId));
            params.put("propertys", "birthday,age,gender,city,country,province,district,height,matchMarriage,matchCountry,matchProvince,matchDistrict,matchCity,matchMaxHeight,matchMinHeight,matchMaxAge,matchMinAge");
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            jsonMap.put("params", JSONObject.toJSONString(params));
            String result1 = HttpUtils.executeWithHttp(usergeturl, jsonMap);
            com.alibaba.fastjson.JSONObject result = com.alibaba.fastjson.JSONObject.parseObject(result1);
            user = JSON.parseObject(result.getString("data"), new TypeReference<Map<String, String>>() {
            });

        }
        return user;
    }

    /**
     *
     */
    protected static final Map<String, RecommendAppRule> appRulesCache = Maps.newHashMap();

    /**
     *
     * @param appKey
     * @return
     */
    protected RecommendAppRule getAppRule(String appKey) {

        RecommendAppRule recommendAppRule = appRulesCache.get(appKey);
        if (recommendAppRule != null) {
            return recommendAppRule;
        }
        LOGGER.info("reload appcache from redis!");
        try {
            String jsonStr = redisTemplate_String.opsForValue().get(appKey);
            if (jsonStr == null) {
                throw new RuntimeException("请配置RecommendAppRule!");
            }
            recommendAppRule = Instances.gson.fromJson(jsonStr, RecommendAppRule.class);
        } catch (Exception e) {
            LOGGER.warn("从redis中获取AppRule失败， 异常：" + e.getMessage() + "");
        }

        return recommendAppRule;

    }

    /**
     *
     * @param appKey
     */
    public void reloadAppRuleCache(String appKey) {
        appRulesCache.remove(appKey);
        LOGGER.info("reload apprule from reids for service:" + appKey + "");

    }

    /**
     *
     * @param yufeiQuery
     * @param searchAppRule
     * @return
     */
    protected BaiheQuery makeBaiheQuery(BaiheQuery yufeiQuery, RecommendAppRule searchAppRule) {
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

    /**
     *
     * @param appRule
     * @param visitedUserId
     * @param recommenUserIdsList
     */
    public void processRecommendUsersRelations(RecommendAppRule appRule, String visitedUserId, List<String> recommenUserIdsList) {
        //判断当前的查询结果是否是从缓存里面取出的，如果是就不应该再重复插入用户推荐关系表中
        if (AbstractQueryService.ifUseCache.get() != null) {
            return;
        }
        if (!appRule.isIfWriteIntoUserRelations()) {
            return;
        }
        if (this.userRelationSign == null || this.userRelationSign.length() < 1) {
            LOGGER.info("推荐应用：" + appRule.getAppName() + " not set 用户关系标人识，将忽略用户关系的更改");
        }
        //write to userrelation
        String userrelationupdateUrl = ReadProperties.appBundle.getString("userrelationupdateUrl");
        //params={"relationList":[{"userID":"1","relationSign":"myview","targetUserID":"200"},{"userID":"200","relationSign":"viewme","targetUserID":"1"}]}
        List<Map<String, String>> maps = Lists.newArrayList();
        for (String userId : recommenUserIdsList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("userID", visitedUserId);
            map.put("targetUserID", userId);

            map.put("relationSign", userRelationSign);

            maps.add(map);

        }
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("relationList", maps);
        Map<String, Object> paramsMap = Maps.newHashMap();
        paramsMap.put("params", JSONObject.toJSONString(jsonMap));
        LOGGER.debug(JSONObject.toJSONString(paramsMap));
        HttpUtils.executeWithHttp(userrelationupdateUrl, paramsMap);

    }

    /**
     *
     * @param appRule
     * @param recommenUserIdsList
     * @param visitedUserId
     */
    protected void duplicateRecommendUsersFromUserRelations(RecommendAppRule appRule, List<String> recommenUserIdsList, String visitedUserId) {
        if (!appRule.isIfDuplicateFromUserRelations()) {
            return;
        }
        String userrelationgeturl = ReadProperties.appBundle.getString("userrelationgeturl");

        //get usrrelations then duplicate 
        List<String> recommendRelationList = Lists.newArrayList();
        Map<String, String> parasMap = Maps.newHashMap();
        // {"relationSign":"recommend_interested_user","targetUserID":"108134156","userID":"56027289"}
        parasMap.put("userID", visitedUserId);
        parasMap.put("relationSign", this.userRelationSign);
        Map<String, Object> paramsMap_ = Maps.newHashMap();
        paramsMap_.put("params", JSONObject.toJSONString(parasMap));
        LOGGER.debug(JSONObject.toJSONString(paramsMap_));
        String lists = HttpUtils.executeWithHttp(userrelationgeturl, paramsMap_);
        JSONObject resultObj = JSONObject.parseObject(lists);
        String listsStr = ((JSONObject) resultObj.get("data")).getString("relationList");
        List<Map<String, String>> resultMap = JSON.parseObject(listsStr, new TypeReference<ArrayList<Map<String, String>>>() {
        });
        if (resultMap == null) {
            return;
        }
        for (Map<String, String> userMap : resultMap) {
            recommendRelationList.add(userMap.get("targetUserID"));
        }
        for (String relationUserID : recommendRelationList) {
            recommenUserIdsList.remove(relationUserID);
        }

    }

    public QueryItem generateBirthdayQueryRange(String visitedUserGender, String visitedUserBirthday, String maxAgeStr, String minAgeStr, boolean extendAge) {
        QueryItem queryItem = null;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentDateInt = year * 10000 + month * 100 + day;
        int visitUserAgeInt = -1;
        int birthdayInt = -1;

        if (visitedUserBirthday != null && visitedUserBirthday.trim().length() > 0) {
            birthdayInt = Integer.parseInt(visitedUserBirthday);
            visitUserAgeInt = (currentDateInt - birthdayInt) / 10000;

        }
        int hage = -1, lage = -1;

        if (maxAgeStr != null && maxAgeStr.trim().length() > 0) {
            hage = Integer.parseInt(maxAgeStr);
            hage = birthdayInt - (hage - visitUserAgeInt) * 10000;
        }
        if (minAgeStr != null && minAgeStr.trim().length() > 0) {
            lage = Integer.parseInt(minAgeStr);
            lage = birthdayInt - (lage - visitUserAgeInt) * 10000;
        }
        if (extendAge) {
            if (hage > 0) {
                if (visitedUserGender.equals("1")) {
                    //男性
                    hage = hage - 30000;

                } else {
                    //女性
                    hage = hage - 80000;

                }
            }
            if (lage > 0) {
                if (visitedUserGender.equals("1")) {
                    //男性
                    lage = lage + 80000;

                } else {
                    //女性
                    lage = lage + 30000;

                }
            }
        }

        if (hage == -1) {
            hage = 0;
        }
        String ageRange = hage + "#TO#" + lage;
        if (lage == -1) {
            ageRange = hage + "#TO#*";
        }
        queryItem = new QueryItem("birthday", Lists.newArrayList(ageRange));
        return queryItem;

    }

}
