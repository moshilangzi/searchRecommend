package com.yufei.searchrecommend.service;

import com.yufei.searchrecommend.service.impl.EncouterUserRecommender;
import com.yufei.searchrecommend.service.impl.InterestedUsersRecommender;
import com.yufei.searchrecommend.service.impl.SimilarUsersRecommender;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * Created by jasstion on 15/6/30.
 */
public class RecommendFactory {

    public final static Map<String, IRecommend> recommendServiceMap = Maps.newHashMap();

    static {
          //yufei_recommend_getsimilaruserIds
        //yufei_recommend_getinteresteduserids
        recommendServiceMap.put(SimilarUsersRecommender.appKey, new SimilarUsersRecommender());
        recommendServiceMap.put(InterestedUsersRecommender.appKey, new InterestedUsersRecommender());
        recommendServiceMap.put(EncouterUserRecommender.appKey, new EncouterUserRecommender());

    }

    public static IRecommend createRecommender(String serviceName) {
        if (serviceName == null || serviceName.trim().length() < 1) {
            throw new IllegalArgumentException("serviceName can not be null or empty!");
        }

        IRecommend recommender = null;
        recommender = recommendServiceMap.get(serviceName);
        if (recommender == null) {
            throw new RuntimeException("" + serviceName + " not existed! ");
        }
        return recommender;

    }
}
