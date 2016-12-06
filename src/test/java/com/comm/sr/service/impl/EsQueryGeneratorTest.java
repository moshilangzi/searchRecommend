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
import com.comm.sr.common.utils.GsonHelper;
import com.google.common.collect.Lists;
import org.junit.*;

import java.util.List;

/**
 *
 * @author jasstion
 */
public class EsQueryGeneratorTest {

    public EsQueryGeneratorTest() {
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
     * Test of generateFinalQuery method, of class EsQueryGenerator.
     */
    @Test
    public void testGenerateFinalQuery() throws Exception {
        String indexName="com";
        String typeName="user";
        List<QueryItem> items = Lists.newArrayList();
//        QueryItem queryItem=new QueryItem("des",Lists.newArrayList("box"));
//        queryItem.setIsPayload(true);
        // items.add(queryItem);
        SubQuery subQuery=new SubQuery();
        subQuery.setLogic("OR");
        List<SubQuery> subQueries=Lists.newArrayList(new SubQuery("AND", new QueryItem("des", Lists.newArrayList("basket", "football"))));
        QueryItem queryItem1=new QueryItem("des", Lists.newArrayList("boss"));
        queryItem1.setIsPayload(true);
        SubQuery subQuery1=new SubQuery("NOT",queryItem1 );
        subQuery1.setSubQuerys(Lists.newArrayList(new SubQuery("AND", new QueryItem("age", Lists.newArrayList("1220TO1230"))),new SubQuery("AND", new QueryItem("age", Lists.newArrayList("12200TO12300")))));
        subQueries.add(subQuery1);
        subQuery.setSubQuerys(subQueries);


        final List<String> fls = Lists.newArrayList("userId","des","name","age");

        List<SortItem> sortItems = Lists.newArrayList();
        //logstash-2015.12.10 log4j
        //EsCommonQuery baiheQuery = new EsCommonQuery(items, 1, 18, sortItems, fls, "baihe_user", "user");
        EsCommonQuery baiheQuery = new EsCommonQuery(1, 5, sortItems, fls, indexName, typeName);
        baiheQuery.setSubQuery(subQuery);
        baiheQuery.setScoreScript("100*_score");
        System.out.print(GsonHelper.objToJson(baiheQuery)+"\n");






        indexName="vcg_creative";
        typeName="vcgcsdn";
        SubQuery subQuery2=new SubQuery();
        subQuery2.setLogic("AND");
        SubQuery item1=new SubQuery();
        QueryItem qi=new QueryItem("onlineState",Lists.newArrayList("1"),false);

        qi.setIsFilterType(true);
        item1.setQueryItem(qi);
        SubQuery item2=new SubQuery();
        item2.setQueryItem(new QueryItem("prekey3",Lists.newArrayList("4165"),true));
        subQuery2.setSubQuerys(Lists.newArrayList(item1,item2));
        EsCommonQuery query = new EsCommonQuery(1, 5, null, Lists.newArrayList("_score","prekey3"), indexName, typeName);
       query.setScoreScript("1.0*_score");
        query.setSubQuery(subQuery2);
        System.out.print(GsonHelper.objToJson(query)+"\n");
        EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

        System.out.print(esQueryWrapper.getSearchSourceBuilder().toString());






    }

}
