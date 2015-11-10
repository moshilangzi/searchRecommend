/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.SortItem;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jasstion
 */
public class SolrQueryGeneratorTest {

    public SolrQueryGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of generateFinalQuery method, of class SolrQueryGenerator.
     */
    @Test
    public void testGenerateFinalQuery() throws SolrServerException {

        QueryItem queryItem = new QueryItem("height", Lists.newArrayList("158#TO #159", "178#TO# 179"));
        QueryItem queryItem1 = new QueryItem("age", Lists.newArrayList("1988", "1999"));
        QueryItem queryItem2 = new QueryItem("gender", Lists.newArrayList("0"));
        QueryItem queryItem_date = new QueryItem("registeDate", Lists.newArrayList("2014-02-15T18:59:51Z#TO #2015-02-15T18:59:51Z", "2009-02-15T18:59:51Z#TO #2011-02-15T18:59:51Z"));
        queryItem_date.setIsFilterType(true);
        queryItem2.setIsFilterType(true);
        List<QueryItem> items = Lists.newArrayList(queryItem, queryItem1, queryItem2, queryItem_date);
        SortItem sortItem = new SortItem();
        sortItem.setFieldName("age");
        sortItem.setSort("desc");
        SortItem sortItem1 = new SortItem();
        sortItem1.setFieldName("height");
        sortItem1.setSort("desc");
        SortItem sortItem3 = new SortItem();
        sortItem3.setFieldName("registeDate");
        sortItem3.setSort("desc");
        final List<String> fls = Lists.newArrayList("userID", "score", "age", "height", "registeDate");

        List<SortItem> sortItems = Lists.newArrayList(sortItem, sortItem1, sortItem3);
        BaiheQuery yufeiQuery = new BaiheQuery(items, 1, 18, sortItems, fls);
        yufeiQuery.setPageNum(1);
        yufeiQuery.setPageSize(10);
        SolrQuery finalQuery = new SolrQueryGenerator().generateFinalQuery(yufeiQuery);
        final String queryParams = JSON.toJSONString(yufeiQuery, SerializerFeature.WriteClassName);
        System.out.print(queryParams + "\n");

        BaiheQuery yufeiQuery1 = JSON.parseObject(queryParams, BaiheQuery.class);
        final String sortString = JSON.toJSONString(sortItems, SerializerFeature.WriteClassName);
        List<QueryItem> filterItems = Lists.newArrayList();
        QueryItem filterItem1 = new QueryItem();
        filterItem1.setFieldName("accountStatus");
        filterItem1.setMatchedValues(Lists.newArrayList(String.valueOf(1)));
        filterItem1.setIsFilterType(true);
        QueryItem filterItem2 = new QueryItem();
        filterItem2.setFieldName("hasMainPhoto");
        filterItem2.setMatchedValues(Lists.newArrayList(String.valueOf(1)));
        filterItem2.setIsFilterType(true);

        //marriage 123
        QueryItem filterItem3 = new QueryItem();
        filterItem3.setFieldName("marriage");
        filterItem3.setMatchedValues(Lists.newArrayList(String.valueOf(1), String.valueOf(2), String.valueOf(3)));
        filterItem3.setIsFilterType(true);

        QueryItem filterItem4 = new QueryItem();
        filterItem4.setFieldName("loveState");
        filterItem4.setMatchedValues(Lists.newArrayList(String.valueOf(1)));
        filterItem4.setIsFilterType(true);
        filterItems = Lists.newArrayList(filterItem1, filterItem2, filterItem3, filterItem4);
        final String filterString = JSON.toJSONString(filterItems, SerializerFeature.WriteClassName);
        System.out.println("filter config is:" + filterString + ""+"\n");

        finalQuery = new SolrQueryGenerator().generateFinalQuery(yufeiQuery1);
        System.out.print(finalQuery+"\n");

        String zkHostString = "172.16.11.43:2181,172.16.11.44:2181,172.16.11.45:2181/yufei/solrcloud";
        //SolrClient solr = new CloudSolrClient(zkHostString);
        // CloudSolrServer solrServer = CloudSolr.getManInstance();
        //solrServer = CloudSolr.getWomanInstance();

        //   QueryResponse solrRespons = solrServer.query(finalQuery);
        //  SolrDocumentList sdl = solrRespons.getResults();
//        for (SolrDocument sdl1 : sdl) {
//            System.out.println(sdl1.get("registeDate") + "," + sdl1.get("age") + "," + sdl1.get("height") + "\n");
//
//        }
//
//        QueryService queryService = new SolrQueryService();
//        yufeiQuery.setGender(0);
//        List<Map<String, Object>> queryResults = queryService.query(yufeiQuery);
//        for (Map<String, Object> queryResult : queryResults) {
//            System.out.println(queryResult.get("registeDate") + "," + queryResult.get("age") + "," + queryResult.get("height") + "\n");
//
//        }
    }

}
