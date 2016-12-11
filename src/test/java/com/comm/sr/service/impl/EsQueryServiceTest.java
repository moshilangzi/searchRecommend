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
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.GsonHelper;
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







        indexName="vcg_creative";
        typeName="vcgcsdn";
        SubQuery subQuery2=new SubQuery();
        subQuery2.setLogic("AND");
        SubQuery item1=new SubQuery();
        QueryItem qi=new QueryItem("onlineState",Lists.newArrayList("1"),false);

        qi.setIsFilterType(true);
        item1.setQueryItem(qi);
        SubQuery item2=new SubQuery();
        item2.setQueryItem(new QueryItem("prekey3", Lists.newArrayList("4165"), true));
        subQuery2.setSubQuerys(Lists.newArrayList(item1,item2));
        EsCommonQuery query = new EsCommonQuery(1, 5, null, Lists.newArrayList("_score","prekey3","resId","_id"), indexName, typeName);
        query.setScoreScript("1.0*_score");
        query.setSubQuery(subQuery2);
        System.out.print(GsonHelper.objToJson(query) + "\n");
        EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);








        System.out.print(esQueryWrapper.getSearchSourceBuilder().toString());
        Properties settings=new Properties();
        settings.put("elasticSearchHosts",":9300");
        settings.put("redis.ip","localhost");
        settings.put("redis.port","6379");

        //CacheService<String,String> cacheService=new RedisCacheService(settings);
        EsQueryService esQueryService =new EsQueryService(settings,null);
        List<Map<String, Object>> results = esQueryService.query(query);
        System.out.print(results.size()+"\n");
        for (Map<String,Object> user:results) {
            Object content =  user.get("resId");
            //String c1=new String(content.getBytes(),"utf8");
            System.out.print(content+"\n");
            System.out.print(user.get("_score")+"\n");
        }






    }

}
