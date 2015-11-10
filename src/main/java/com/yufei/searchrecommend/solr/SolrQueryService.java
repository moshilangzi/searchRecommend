/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.service.AbstractQueryService;
import com.yufei.searchrecommend.service.QueryGenerator;
import com.yufei.searchrecommend.service.QueryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.StopWatch;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jasstion
 */
public class SolrQueryService extends AbstractQueryService<BaiheQuery> implements QueryService<BaiheQuery> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(SolrQueryService.class);

    @Override
    public List<Map<String, Object>> query(BaiheQuery yufeiQuery) throws Exception {
        List<Map<String, Object>> results = Lists.newArrayList();

        int gender = yufeiQuery.getGender();
        if (yufeiQuery.getGender() < 0) {
            throw new RuntimeException("请正确的初始化参数，指定用户性别。");
        }
        CloudSolrServer cloudSolrServer = null;
        if (gender == 0) {
            cloudSolrServer = CloudSolr.getWomanInstance();
        }
        if (gender == 1) {
            cloudSolrServer = CloudSolr.getManInstance();
        }
        QueryGenerator<SolrQuery,BaiheQuery> queryGenerator = new SolrQueryGenerator();
        SolrQuery solrQuery = queryGenerator.generateFinalQuery(yufeiQuery);
        LOGGER.debug("generted solr query:"+solrQuery.toString()+"");
        
        QueryResponse solrRespons = cloudSolrServer.query(solrQuery);
    //    int totalCount=Integer.parseInt((String)solrRespons.getResponse().get("numFound"));
        SolrDocumentList solrResult = solrRespons.getResults();
        for (SolrDocument solrDocument : solrResult) {
            Map<String, Object> resultMap = Maps.newHashMap();
            List<String> flList = yufeiQuery.getFls();
            for (String fl : flList) {
                resultMap.put(fl, solrDocument.get(fl));
            }
            results.add(resultMap);
        }

        return results;

    }

    @Override
    public List<Map<String, Object>> processQuery(BaiheQuery yufeiQuery) throws Exception {

        long timeTaken = 0;
        boolean cacheHit = false;
        List<Map<String, Object>> queryResult = null;
        StopWatch stopWatch = new StopWatch();

        // Start the watch, do some task and stop the watch.
        stopWatch.start();

        queryResult = this.queryCache(yufeiQuery);
        if (queryResult != null && queryResult.size() > 1) {
            cacheHit = true;
            AbstractQueryService.QueryStatistics queryStatistics = new QueryStatistics(timeTaken, cacheHit);
            stopWatch.stop();
            timeTaken = stopWatch.getTime();
            this.recordQueryLog(queryStatistics);
            return queryResult;
        }
        cacheHit = false;
        queryResult = this.query(yufeiQuery);
        stopWatch.stop();
        timeTaken = stopWatch.getTime();

        AbstractQueryService.QueryStatistics queryStatistics = new QueryStatistics(timeTaken, cacheHit);
        this.recordQueryLog(queryStatistics);
        this.cacheQueryResult(yufeiQuery, queryResult);

        return queryResult;

    }

}
