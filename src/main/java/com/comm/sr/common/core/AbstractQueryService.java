/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.core;


import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.entity.AbstractQuery;
import com.comm.sr.service.cache.CacheService;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * use to do some common task such as : save query log, process query result
 * cache... this implemention is thread safe
 *
 * @author jasstion
 */
public abstract class AbstractQueryService<Q extends AbstractQuery> extends AbstractComponent{

    public final static ThreadLocal<Boolean> ifUseCache = new ThreadLocal<Boolean>();

    final static GsonBuilder g = new GsonBuilder();

    final static Gson gson = g.create();
    public final static String PRE_KEY ="querycachekey_";

   protected Properties settings;
    protected CacheService<String,String> cacheService;

    public static Set<String> filterFieldNmae = Sets.newHashSet("shape", "residencepProvince", "province", "education", "corporationNature", "industry", "income", "loveType", "matchIncome", "lastLoginDate");

    public AbstractQueryService(CacheService<String,String> cacheService,Properties settings) {
        super(settings);
        this.cacheService=cacheService;
        this.settings=settings;


    }

    public List<Map<String, Object>> processQuery(Q baiheQuery) throws Exception {

        long timeTaken = 0;
        boolean cacheHit = false;
        List<Map<String, Object>> queryResult = null;
        StopWatch stopWatch = new StopWatch();

        // Start the watch, do some task and stop the watch.
        stopWatch.start();

        queryResult = this.queryCache(baiheQuery);
        if (queryResult != null && queryResult.size() > 1) {
            cacheHit = true;
            AbstractQueryService.QueryStatistics queryStatistics = new QueryStatistics(timeTaken, cacheHit);
            stopWatch.stop();
            timeTaken = stopWatch.getTime();
            this.recordQueryLog(queryStatistics);
            return queryResult;
        }
        cacheHit = false;
        queryResult = this.query(baiheQuery);
        stopWatch.stop();
        timeTaken = stopWatch.getTime();

        AbstractQueryService.QueryStatistics queryStatistics = new QueryStatistics(timeTaken, cacheHit);
        this.recordQueryLog(queryStatistics);
        this.cacheQueryResult(baiheQuery, queryResult);

        return queryResult;

    }

    public abstract List<Map<String, Object>> query(Q baiheQuery) throws Exception;
    public abstract Map<String,Object> queryAll(Q baiheQuery) throws Exception;

    public void cacheQueryResult(Q baiheQuery, List<Map<String, Object>> queryResult) {
        String cacheStr = baiheQuery.getCacheStrategy();
        String key = PRE_KEY + baiheQuery.hashCode();
        if (queryResult == null || queryResult.size() < 1) {
            return;
        }
        ifUseCache.set(Boolean.TRUE);
        String queryResultJson = null;
        if(cacheService==null){
            return;
        }
        if (cacheStr == null || cacheStr.trim().length() < 1) {
            //default not to set cache
            // valueOperations.set(key, queryResultJson);
        } else {
          queryResultJson=gson.toJson(queryResult);
            cacheService.set(key, queryResultJson, Integer.parseInt(cacheStr), TimeUnit.SECONDS);

        }
    }

    public List<Map<String, Object>> queryCache(Q baiheQuery) {
        if(cacheService==null){
            return null;

        }
        List<Map<String, Object>> queryResult = null;
        String key = PRE_KEY + baiheQuery.hashCode();

        String queryResultJson = cacheService.get(key);
        if (queryResultJson == null) {
            return null;
        }
        queryResult = gson.fromJson(queryResultJson, new TypeToken<ArrayList<Map<String, Object>>>() {
        }.getType());
        return queryResult;

    }

    public void recordQueryLog(QueryStatistics queryStatistics) {
        logger.info(queryStatistics.toString());

    }

    public static class QueryStatistics {

        //seconds
        private long queryTakenTime = 0;
        private boolean cacheHit = false;

        @Override
        public String toString() {
            return "QueryStatistics{" + "queryTakenTime=" + queryTakenTime + ", cacheHit=" + cacheHit + '}';
        }

        public QueryStatistics(long queryTakenTim, boolean cacheHit) {
            this.queryTakenTime = queryTakenTim;
            this.cacheHit = cacheHit;
        }

    }

}
