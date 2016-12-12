/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.service.impl;


import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.elasticsearch.EsQueryService;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.entity.SubQuery;
import com.google.common.collect.Lists;
import org.junit.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author jasstion
 */
public class EsQueryServiceTest {



    public EsQueryServiceTest() {
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
     * Test of processQuery method, of class EsQueryService.
     */
    @Test
    public void testProcessQuery() throws Exception {
    }

    /**
     * Test of query method, of class EsQueryService.
     */
    @Test
    public void testQuery() throws Exception {







        List<QueryItem> items = Lists.newArrayList();
        QueryItem queryItem=new QueryItem("des",Lists.newArrayList("box"));
        //queryItem.setIsPayload(true);
       // items.add(queryItem);
        SubQuery subQuery=new SubQuery();
        subQuery.setLogic("AND");
      List<SubQuery> subQueries=Lists.newArrayList();
      SubQuery payloadQuery=new SubQuery();
      QueryItem queryItem1=new QueryItem();
      queryItem1.setFieldName("des");
      queryItem1.setIsPayload(true);
      queryItem1.setMatchedValues(Lists.newArrayList("basket","football"));

      payloadQuery.setQueryItem(queryItem1);
      subQueries.add(payloadQuery);

        subQuery.setSubQuerys(subQueries);


        final List<String> fls = Lists.newArrayList("userId","des","name","age");
      String indexName="com";
      String typeName="user";
        List<SortItem> sortItems = Lists.newArrayList();
        //logstash-2015.12.10 log4j
        //EsCommonQuery baiheQuery = new EsCommonQuery(items, 1, 18, sortItems, fls, "baihe_user", "user");
        EsCommonQuery baiheQuery = new EsCommonQuery(1, 5, sortItems, fls, indexName, typeName);
        baiheQuery.setSubQuery(subQuery);
        baiheQuery.setClusterIdentity("test");
        //baiheQuery.setScoreScript("100*_score");




        EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(baiheQuery);

        System.out.print(esQueryWrapper.getSearchSourceBuilder().toString());
        Properties settings=new Properties();
      settings.put("elastic.test.hosts","127.0.0.1:9308");
      settings.put("elastic.test.clusterName","elasticsearch");


        //CacheService<String,String> cacheService=new RedisCacheService(settings);
        EsQueryService esQueryService =new EsQueryService(settings,null);
        List<Map<String, Object>> results = esQueryService.query(baiheQuery);
        System.out.print(results.size()+"\n");
        for (Map<String,Object> user:results) {
            Object content =  user.get("userId");
            //String c1=new String(content.getBytes(),"utf8");
            System.out.print(content+"\n");
            System.out.print(user.get("score")+"\n");
        }






    }

}
