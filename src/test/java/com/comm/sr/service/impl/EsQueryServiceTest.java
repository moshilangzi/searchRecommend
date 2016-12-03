/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.service.impl;


import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.elasticsearch.EsQueryService;
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
   // public static final NodeTestUtils nodeTestUtils=new NodeTestUtils();


    String indexName="com";
    String typeName="user";
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
        queryItem.setIsPayload(true);
       // items.add(queryItem);
        SubQuery subQuery=new SubQuery();
        subQuery.setLogic("AND");
        List<SubQuery> subQueries=Lists.newArrayList(new SubQuery("AND", new QueryItem("des", Lists.newArrayList("basket", "football"))));
        QueryItem queryItem1=new QueryItem("des", Lists.newArrayList("boss"));
        queryItem1.setIsPayload(false);
        SubQuery subQuery1=new SubQuery("NOT",queryItem1 );
        subQuery1.setSubQuerys(Lists.newArrayList(new SubQuery("AND", new QueryItem("age", Lists.newArrayList("1220TO1230")))));
        subQueries.add(subQuery1);
        SubQuery subQuery2=new SubQuery();
        subQuery2.setLogic("OR");
        SubQuery subQuery3=new SubQuery();
        subQuery3.setQueryItem(new QueryItem("des",Lists.newArrayList("boss","box")));

        subQuery2.setSubQuerys(Lists.newArrayList(subQuery3));
        subQuery1.getSubQuerys().add(subQuery2);
        subQuery.setSubQuerys(subQueries);


        final List<String> fls = Lists.newArrayList("userId","des","name","age");

        List<SortItem> sortItems = Lists.newArrayList();
        //logstash-2015.12.10 log4j
        //EsCommonQuery baiheQuery = new EsCommonQuery(items, 1, 18, sortItems, fls, "baihe_user", "user");
        EsCommonQuery baiheQuery = new EsCommonQuery(items, 1, 5, sortItems, fls, indexName, typeName);
        baiheQuery.setSubQuery(subQuery);
        baiheQuery.setScoreScript("100*_score");




        EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(baiheQuery);

        System.out.print(esQueryWrapper.getSearchSourceBuilder().toString());
        Properties settings=new Properties();
        settings.put("elasticSearchHosts","127.0.0.1:9308");
        settings.put("redis.ip","localhost");
        settings.put("redis.port","6379");

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
