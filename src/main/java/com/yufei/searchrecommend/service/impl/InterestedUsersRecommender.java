package com.yufei.searchrecommend.service.impl;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.RecommendAppRule;
import com.yufei.searchrecommend.service.IRecommend;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Calendar;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * Created by jasstion on 15/6/30.
 */
public class InterestedUsersRecommender extends AbstractRecommender implements IRecommend<String, Object> {

    /**
     *
     */
    public final static String appKey = "59c937b30d1db924cc6d4d29d1ae607e";

    private final static String REDIS_PREFIX = "bhdp-cf:";

    /**
     *
     */
    public InterestedUsersRecommender() {
        super();
        this.userRelationSign = "recommend_interested_user";

    }

    /**
     *
     * @param paras
     * @return
     */
    @Override
    public Map<String, Object> recommend(Map<String, String> paras) {
        RecommendAppRule appRule = this.getAppRule(appKey);
        Map<String, Object> resultMap = Maps.newHashMap();
        String profileUserId = paras.get("profileUserId");
        String visitUserId = paras.get("visitUserId");
        int count = Integer.parseInt(paras.get("count"));
        Map<String, String> visitedUserInfo = getUserInfoByUserId(visitUserId);
        Map<String, String> profileUserInfo = getUserInfoByUserId(profileUserId);

        int hage = -1, lage = -1, hheight = -1, lheight = -1;
        int matchCity = -1, matchProvince = -1, matchCountry = -1, matchDistrict = -1;
        String matchCityStr = visitedUserInfo.get("matchCity");
        //matchCountry,matchProvince,matchDistrict,matchCity
        String matchCountryStr = visitedUserInfo.get("matchCountry");
        String matchDistrictStr = visitedUserInfo.get("matchDistrict");
        String matchProvinceStr = visitedUserInfo.get("matchProvince");

        String hageStr = visitedUserInfo.get("matchMaxAge");
        String lageStr = visitedUserInfo.get("matchMinAge");
        String hheightStr = visitedUserInfo.get("matchMaxHeight");
        String lheightStr = visitedUserInfo.get("matchMinHeight");
        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (hageStr != null && hageStr.trim().length() > 0) {
            hage = Integer.parseInt(hageStr);
            hage = year - hage;
        }
        if (lageStr != null && lageStr.trim().length() > 0) {
            lage = Integer.parseInt(lageStr);
            lage = year - lage;
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

        String genderStr = profileUserInfo.get("gender");
        if (genderStr == null || genderStr.trim().length() < 1) {
            throw new RuntimeException("userId:" + profileUserId + " has no gender value!");
        }
        int gender = Integer.parseInt(genderStr);
        String k = REDIS_PREFIX + visitUserId;
        List<String> userIdsList = Lists.newArrayList();

        try {
            Set<ZSetOperations.TypedTuple<Set>> result = redisTemplate.opsForZSet().reverseRangeWithScores(k, 0, -1);

            for (ZSetOperations.TypedTuple tu : result) {
                userIdsList.add((String) tu.getValue());

            }
            if (userIdsList.contains(profileUserId)) {
                userIdsList.remove(profileUserId);

            }
        } catch (Exception e) {
            LOGGER.warn("访问 Redis 服务器出现异常，" + e.getMessage() + "");
        }

        int left = count - userIdsList.size();
        List<String> userIds_solr = Lists.newArrayList();
        try {

            BaiheQuery query = new BaiheQuery();

            List<QueryItem> queryItems = Lists.newArrayList();

            query.setQueryItems(queryItems);
            query.setPageNum(1);
            query.setPageSize(count + 2);
            query.setFls(Lists.newArrayList("userID"));
            query.setGender(gender);

            if (matchDistrict > 0) {

                QueryItem queryItem = new QueryItem("district", Lists.newArrayList(String.valueOf(matchDistrict)));
                queryItems.add(queryItem);

            } else {
                if (matchCity > 0) {

                    QueryItem queryItem = new QueryItem("city", Lists.newArrayList(String.valueOf(matchCity)));
                    queryItems.add(queryItem);

                } else {
                    if (matchProvince > 0) {

                        QueryItem queryItem = new QueryItem("province", Lists.newArrayList(String.valueOf(matchProvince)));
                        queryItems.add(queryItem);

                    }
                }
            }

            if (hage == -1) {
                hage = 0;
            }
            if (lheight == -1) {
                lheight = 0;
            }

            String ageRange = hage + "#TO#" + lage;
            String heightRange = lheight + "#TO#" + hheight;
            if (lage == -1) {
                ageRange = hage + "#TO#*";
            }
            if (hheight == -1) {
                heightRange = lheight + "#TO#*";
            }
            QueryItem ageQueryItem = new QueryItem("age", Lists.newArrayList(ageRange));
            QueryItem heightQueryItem = new QueryItem("height", Lists.newArrayList(heightRange));
            queryItems.add(ageQueryItem);
            queryItems.add(heightQueryItem);
            //  query = BaiheQueryHelper.makeBaiheQuery(query, appKey);

            query = this.makeBaiheQuery(query, appRule);
            LOGGER.debug(query.toString());
            List<Map<String, Object>> finalQueryResults = queryService.processQuery(query);
            for (Map<String, Object> map : finalQueryResults) {
                String userID = String.valueOf(map.get("userID"));
                if (userID == null) {
                    continue;
                }
                userID = userID.replace(".", "").replaceAll("E[\\d]{0,}", "");

                userIds_solr.add(userID);
            }
        } catch (Exception e) {
            LOGGER.warn("query solr errors,  异常信息是：" + e.getMessage() + "");

        }

        if (left < 0) {
            userIdsList = userIdsList.subList(0, count - 1);
        }

        //remove duplicate
        for (String userId : userIds_solr) {
            if (!userIdsList.contains(userId) && !userId.equals(profileUserId) && !userId.equals(visitUserId)) {
                userIdsList.add(userId);
            }
            if (userIdsList.size() == count) {
                break;

            }
        }
        this.processRecommendUsersRelations(appRule, visitUserId, userIdsList);
        this.duplicateRecommendUsersFromUserRelations(appRule, userIdsList, visitUserId);
        resultMap.put("interestedUserIds", userIdsList);

        return resultMap;
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
