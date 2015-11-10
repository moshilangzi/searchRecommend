/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.service.impl;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.EncounterRecommendAppRule;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.RecommendAppRule;
import com.yufei.searchrecommend.service.IRecommend;
import com.yufei.searchrecommend.utils.Instances;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jasstion
 */
public class EncouterUserRecommender extends AbstractRecommender implements IRecommend<String, Object> {

    /**
     *
     */
    public final static String appKey = "dd93e5436bf1e3f27442fc7adad40ed4";

    /**
     *
     */
    public final static String ENCOUTEREDUSERID_PRE_KEY = "recommendedUserIds:";

    /**
     *
     */
    public EncouterUserRecommender() {
        super();
    }

    /**
     *
     * @param appKey
     * @return
     */
    @Override
    protected RecommendAppRule getAppRule(String appKey) {
        EncounterRecommendAppRule recommendAppRule = (EncounterRecommendAppRule) appRulesCache.get(appKey);
        if (recommendAppRule != null) {
            return recommendAppRule;
        }
        LOGGER.info("reload appcache from redis!");

        try {
            String jsonStr = redisTemplate_String.opsForValue().get(appKey);
            if (jsonStr == null) {
                throw new RuntimeException("请配置RecommendAppRule!");
            }
            recommendAppRule = Instances.gson.fromJson(jsonStr, EncounterRecommendAppRule.class);
        } catch (Exception e) {
            LOGGER.warn("从redis中获取AppRule失败， 异常：" + e.getMessage() + "");
        }

        return recommendAppRule;
    }

    /**
     *
     * @param paras
     * @return
     */
    @Override
    public Map<String, Object> recommend(Map<String, String> paras) {
        EncounterRecommendAppRule appRule = (EncounterRecommendAppRule) this.getAppRule(appKey);
        int days = appRule.getDupDays();

        Map<String, Object> resultMap = Maps.newHashMap();
        //要邂逅人的id
        String visitUserId = paras.get("userID");

        int count = Integer.parseInt(paras.get("count"));

        int finalCount = (days + 1) * count;
        Map<String, String> visitedUserInfo = getUserInfoByUserId(visitUserId);

        List<String> finalUserIds = Lists.newArrayList();
        try {

            BaiheQuery query = populateBaiheQuery(false, false, finalCount, appRule, visitedUserInfo);
            LOGGER.debug(query.toString());
            populateRecommendedUserIds(query, finalUserIds);
            if (finalUserIds.size() < finalCount) {
                query = populateBaiheQuery(true, false, finalCount, appRule, visitedUserInfo);
                LOGGER.debug(query.toString());
                populateRecommendedUserIds(query, finalUserIds);
                if (finalUserIds.size() < finalCount) {
                    query = populateBaiheQuery(true, true, finalCount, appRule, visitedUserInfo);
                    LOGGER.debug(query.toString());
                    populateRecommendedUserIds(query, finalUserIds);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("query solr errors,  异常信息是：" + e.getMessage() + "");

        }

        String encouteredUserIdsKey = ENCOUTEREDUSERID_PRE_KEY + visitUserId;
        Set<String> recommendedUserIds = redisTemplate_String.opsForZSet().rangeByScore(encouteredUserIdsKey, 1, System.currentTimeMillis() / 1000);
        if (recommendedUserIds != null) {
            //duplication
            for (String userId : recommendedUserIds) {
                finalUserIds.remove(userId);
            }
        }

        if (finalUserIds.size() >= 20) {
            finalUserIds = finalUserIds.subList(0, count);
            addAndClearRedis(finalUserIds, encouteredUserIdsKey);
            //存入redis
        } else {
            int left = count - finalUserIds.size();
            addAndClearRedis(finalUserIds, encouteredUserIdsKey);

            //补足
            for (String recommendedUserId : recommendedUserIds) {
                finalUserIds.add(recommendedUserId);
                //把补足的先删除在重新加到列表的后面
                 redisTemplate_String.opsForZSet().remove(encouteredUserIdsKey, recommendedUserId);
                 redisTemplate_String.opsForZSet().add(encouteredUserIdsKey, recommendedUserId, System.currentTimeMillis() / 1000);
                
                left--;
                if (left < 0) {
                    break;
                }
            }
            
            

        }

        resultMap.put("encouterUserIds", finalUserIds);

        return resultMap;
    }

    private void populateRecommendedUserIds(BaiheQuery query, List<String> finalUserIds) throws Exception {

        List<Map<String, Object>> finalQueryResults = queryService.processQuery(query);
        for (Map<String, Object> map : finalQueryResults) {
            String userID = String.valueOf(map.get("userID"));
            if (userID == null) {
                continue;
            }

            userID = userID.replace(".", "").replaceAll("E[\\d]{0,}", "");
            if (finalUserIds.contains(userID)) {
                continue;
            }
            finalUserIds.add(userID);
        }
    }


    
    private BaiheQuery populateBaiheQuery(boolean extendAge, boolean extendArea, int finalCount, EncounterRecommendAppRule appRule, Map<String, String> visitedUserInfo) {

        int  hheight = -1, lheight = -1;
        int matchCity = -1, matchProvince = -1, matchCountry = -1, matchDistrict = -1;
        List<Integer> matchMarriageInts = Lists.newArrayList();
        String birthdayStr = visitedUserInfo.get("birthday");
        String matchCityStr = visitedUserInfo.get("matchCity");
        //matchCountry,matchProvince,matchDistrict,matchCity
        String matchCountryStr = visitedUserInfo.get("matchCountry");
        String matchDistrictStr = visitedUserInfo.get("matchDistrict");
        String matchProvinceStr = visitedUserInfo.get("matchProvince");
        String matchMarriage = visitedUserInfo.get("matchMarriage");

        String hageStr = visitedUserInfo.get("matchMaxAge");
        String lageStr = visitedUserInfo.get("matchMinAge");
        String hheightStr = visitedUserInfo.get("matchMaxHeight");
        String lheightStr = visitedUserInfo.get("matchMinHeight");
        if (matchMarriage != null && matchMarriage.trim().length() > 0) {
            String[] mStrings = matchMarriage.split(",");
            for (String mString : mStrings) {
                matchMarriageInts.add(Integer.parseInt(mString));
            }

        }
        if (hheightStr != null && hheightStr.trim().length() > 0) {
            hheight = Integer.parseInt(hheightStr);
        }
        if (lheightStr != null && lheightStr.trim().length() > 0) {
            lheight = Integer.parseInt(lheightStr);
        }
        if (matchCityStr != null && matchCityStr.trim().length() > 0) {
            matchCity = Integer.parseInt(matchCityStr);
        }
        if (matchCountryStr != null && matchCountryStr.trim().length() > 0) {
            matchCountry = Integer.parseInt(matchCountryStr);
        }
        if (matchDistrictStr != null && matchDistrictStr.trim().length() > 0) {
            matchDistrict = Integer.parseInt(matchDistrictStr);
        }
        if (matchProvinceStr != null && matchProvinceStr.trim().length() > 0) {
            matchProvince = Integer.parseInt(matchProvinceStr);
        }

        String visitUserGender = visitedUserInfo.get("gender");
        int targetGender = visitUserGender.equals("0") ? 1 : 0;

        BaiheQuery query = new BaiheQuery();
        List<QueryItem> queryItems = Lists.newArrayList();
        query.setQueryItems(queryItems);
        query.setPageNum(1);
        query.setPageSize(finalCount);
        query.setFls(Lists.newArrayList("userID"));
        query.setGender(targetGender);

        if (lheight == -1) {
            lheight = 0;
        }

        String heightRange = lheight + "#TO#" + hheight;

        if (hheight == -1) {
            heightRange = lheight + "#TO#*";
        }
        //area filter
        if (matchCity > 0 && !extendArea) {

            QueryItem queryItem = new QueryItem("city", Lists.newArrayList(String.valueOf(matchCity)));
            queryItem.setIsFilterType(true);

            queryItems.add(queryItem);

        } else {
            if (matchProvince > 0) {

                QueryItem queryItem = new QueryItem("province", Lists.newArrayList(String.valueOf(matchProvince)));
                queryItem.setIsFilterType(true);
                queryItems.add(queryItem);

            } else if (matchCountry > 0) {
                QueryItem queryItem = new QueryItem("country", Lists.newArrayList(String.valueOf(matchCountry)));
                queryItem.setIsFilterType(true);
                queryItems.add(queryItem);
            }
        }
        //默认匹配国家
        int matchedLocation = 1;
        if (matchDistrict > 0) {
            matchedLocation = 4;

        } else {
            if (matchCity > 0) {
                matchedLocation = 3;
            } else {
                if (matchProvince > 0) {
                    matchedLocation = 2;
                } else {
                    if (matchCountry > 0) {
                        matchedLocation = 1;

                    }
                }
            }
        }
        QueryItem ageQueryItem = generateBirthdayQueryRange(visitUserGender, birthdayStr, hageStr, lageStr, extendAge);// new QueryItem("birthday", Lists.newArrayList(ageRange));
        
        QueryItem heightQueryItem = new QueryItem("height", Lists.newArrayList(heightRange));
        ageQueryItem.setIsFilterType(true);
        heightQueryItem.setIsFilterType(true);
        if (ageQueryItem != null) {
            queryItems.add(ageQueryItem);

        }
        queryItems.add(heightQueryItem);
        //function weight query
        float identityWeight = appRule.getIdentityWeight();
        float ageWeight = appRule.getAgeWeight();
        float heightWeight = appRule.getHeightWeight();
        float locationWeight = appRule.getLocationWeight();
        float lastLoginTimeWeight = appRule.getLastLoginTimeWeight();
        float districtWeight = appRule.getDistrictWeight();
        float cityWeight = appRule.getCityWeight();
        float marriageStatusWeight = appRule.getMarriageStatusWeight();
        //金至尊和至尊
        //  String identityFunction = "if(exists(identityDisplayName),product(strdist('金至尊会员',identityDisplayName,'JW')," + identityWeight + "),0)";
        String identityFunction1 = "if(exists(query({!v=identityDisplayName:'金至尊会员'}))," + identityWeight + ",0)";
        String identityFunction2 = "if(exists(query({!v=identityDisplayName:'至尊会员'}))," + identityWeight / 2 + ",0)";
        //婚宴状态权重，有限未婚
        String marriageStatusFunction1 = "if(exists(query({!v='marriage:1'}))," + marriageStatusWeight + ",0)";
        String marriageStatusFunction2 = "if(exists(query({!v='marriage:2'}))," + (marriageStatusWeight / 2) + ",0)";
        String marriageStatusFunction3 = "if(exists(query({!v='marriage:3'}))," + marriageStatusWeight / 4 + ",0)";
        if (matchMarriageInts.size() == 1) {
            if (matchMarriageInts.get(0) == 2) {
                marriageStatusFunction1 = "if(exists(query({!v='marriage:2'}))," + marriageStatusWeight + ",0)";
                marriageStatusFunction2 = "if(exists(query({!v='marriage:1'}))," + (marriageStatusWeight / 2) + ",0)";
                marriageStatusFunction3 = "if(exists(query({!v='marriage:3'}))," + marriageStatusWeight / 4 + ",0)";
            }
            if (matchMarriageInts.get(0) == 3) {

                marriageStatusFunction1 = "if(exists(query({!v='marriage:3'}))," + marriageStatusWeight + ",0)";
                marriageStatusFunction2 = "if(exists(query({!v='marriage:1'}))," + (marriageStatusWeight / 2) + ",0)";
                marriageStatusFunction3 = "if(exists(query({!v='marriage:2'}))," + marriageStatusWeight / 4 + ",0)";
            }
        }
        if (matchMarriageInts.size() == 2) {
            if (matchMarriageInts.contains(2) && matchMarriageInts.contains(3)) {
                marriageStatusFunction1 = "if(exists(query({!v='marriage:2'}))," + marriageStatusWeight + ",0)";
                marriageStatusFunction2 = "if(exists(query({!v='marriage:3'}))," + (marriageStatusWeight / 2) + ",0)";
                marriageStatusFunction3 = "if(exists(query({!v='marriage:1'}))," + marriageStatusWeight / 4 + ",0)";
            }
            if (matchMarriageInts.contains(1) && matchMarriageInts.contains(3)) {
                marriageStatusFunction1 = "if(exists(query({!v='marriage:1'}))," + marriageStatusWeight + ",0)";
                marriageStatusFunction2 = "if(exists(query({!v='marriage:3'}))," + (marriageStatusWeight / 2) + ",0)";
                marriageStatusFunction3 = "if(exists(query({!v='marriage:2'}))," + marriageStatusWeight / 4 + ",0)";
            }

        }
        
        String ageFunction = "product(div(age,2015)," + ageWeight + ")";
        String heightFunction = "product(div(height,250)," + heightWeight + ")";
        String lastLoginTimeFunctionQuery = "product(recip(ms(NOW,lastLoginTime),3.16e-11,1,1)," + lastLoginTimeWeight + ")";
        String districtFunctionQuery = "if(exists(district),product(strdist('" + matchDistrictStr + "',district,'JW')," + districtWeight + "),0)";
        String cityFunctionQuery = "if(exists(city),product(strdist('" + matchCityStr + "',city,'JW')," + cityWeight + "),0)";

        query.getFunctionQuerysList().add(identityFunction1);
        query.getFunctionQuerysList().add(identityFunction2);
        query.getFunctionQuerysList().add(marriageStatusFunction1);
        query.getFunctionQuerysList().add(marriageStatusFunction2);
        query.getFunctionQuerysList().add(marriageStatusFunction3);

        query.getFunctionQuerysList().add(ageFunction);
        query.getFunctionQuerysList().add(heightFunction);
        query.getFunctionQuerysList().add(lastLoginTimeFunctionQuery);
        //location related weight function query
        if (matchedLocation == 4) {
            query.getFunctionQuerysList().add(districtFunctionQuery);
        }
        if (matchedLocation == 3) {
            query.getFunctionQuerysList().add(cityFunctionQuery);

        }

        query = this.makeBaiheQuery(query, appRule);
        return query;
    }

    /**
     *
     *
     */
    private void addAndClearRedis(List<String> finalUserIdsList, String encouteredKey) {

        for (String userId : finalUserIdsList) {
            redisTemplate_String.opsForZSet().add(encouteredKey, userId, System.currentTimeMillis() / 1000);
        }
        //check if size >10*20,  if  remove earlily added userIds to keep 200 userIds in redis
        Long countInRedis = redisTemplate_String.opsForZSet().count(encouteredKey, 1, System.currentTimeMillis() / 1000);
        if (countInRedis > 200) {
            long deletedCount = (countInRedis - 200);
            Set<String> recommendedUserIds = redisTemplate_String.opsForZSet().rangeByScore(encouteredKey, 1, System.currentTimeMillis() / 1000);
            for (String recommendedUserId : recommendedUserIds) {
                redisTemplate_String.opsForZSet().remove(encouteredKey, recommendedUserId);
                deletedCount--;
                if (deletedCount < -0) {
                    break;
                }
            }
        }

    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        EncouterUserRecommender encouterUserRecommender = new EncouterUserRecommender();
        List<String> testUserIds = Lists.newArrayList("74522756","74522756","74522756","74522756","74522756");
        for (String testUserId : testUserIds) {
            Map<String, String> params = Maps.newHashMap();
            params.put("userID", testUserId);
            params.put("count", "20");
            List<String> userIds = (List<String>) encouterUserRecommender.recommend(params).get("encouterUserIds");
            for (String userId : userIds) {
                System.out.println(userId + "\n");
            }
            encouterUserRecommender.clearAppRuleCache(EncouterUserRecommender.appKey);

        }

    }

    /**
     *
     * @param appKey
     */
    @Override
    public void clearAppRuleCache(String appKey) {
        this.reloadAppRuleCache(appKey);
    }

}
