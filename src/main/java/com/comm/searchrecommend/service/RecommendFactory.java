package com.comm.searchrecommend.service;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by jasstion on 15/6/30.
 */
public class RecommendFactory {

    public final static Map<String, IRecommend> recommendServiceMap = Maps.newHashMap();

    static {


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
