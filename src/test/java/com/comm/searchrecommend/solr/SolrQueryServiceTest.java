/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.searchrecommend.solr;

import com.comm.basedSearch.entity.CommonQuery;
import com.comm.basedSearch.entity.QueryItem;
import com.comm.basedSearch.entity.SortItem;
import com.comm.basedSearch.solr.SolrQueryService;
import com.google.common.collect.Lists;
import org.junit.*;

import java.util.List;
import java.util.Map;


/**
 *
 * @author jasstion
 */
public class SolrQueryServiceTest {

    SolrQueryService service = new SolrQueryService();
    CommonQuery commonQuery = new CommonQuery();

    public SolrQueryServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        QueryItem queryItem = new QueryItem("height", Lists.newArrayList("158TO159", "178#TO# 179"));
        QueryItem queryItem1 = new QueryItem("age", Lists.newArrayList("1988", "1999"));
        QueryItem queryItem2 = new QueryItem("gender", Lists.newArrayList("0"));
        QueryItem queryItem_date = new QueryItem("registeDate", Lists.newArrayList("2014-02-15T18:59:51ZTO2015-02-15T18:59:51Z", "2009-02-15T18:59:51Z#TO#2011-02-15T18:59:51Z"));
        queryItem_date.setIsFilterType(true);
        queryItem2.setIsFilterType(true);
        List<QueryItem> items = Lists.newArrayList(queryItem, queryItem1, queryItem2, queryItem_date);
        commonQuery.setQueryItems(items);
        commonQuery.setFls(Lists.newArrayList("userID", "height","dynValue","score","nickname"));
        List<SortItem> sortItems = Lists.newArrayList();
        SortItem sortItem = new SortItem();
        sortItem.setFieldName("age");
        sortItem.setSort("desc");
        SortItem sortItem1 = new SortItem();
        sortItem1.setFieldName("height");
        sortItem1.setSort("desc");
        SortItem sortItem3 = new SortItem();
        sortItem3.setFieldName("registeDate");
        sortItem3.setSort("desc");
        sortItems = Lists.newArrayList(sortItem, sortItem1, sortItem3);//
       // commonQuery.setSortItems(sortItems);
        commonQuery.setGender(0);
        commonQuery.setCacheStrategy("100");
        List<String> functionQueryList=Lists.newArrayList();
        functionQueryList.add("product(dynValue,100)");
        commonQuery.setFunctionQuerysList(functionQueryList);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of query method, of class SolrQueryService.
     */
    @Test
    public void testQuery() throws Exception {
        List<Map<String, Object>> results = service.query(commonQuery);
        for (Map<String, Object> result : results) {
            System.out.print(result.get("userID") + "\n"+result.get("nickname"));
        }

    }

    /**
     * Test of processQuery method, of class SolrQueryService.
     */
    @Test
    public void testProcessQuery() throws Exception {
        CommonQuery bq=new CommonQuery();
        List<Map<String, Object>> results = service.processQuery(commonQuery);
        for (Map<String, Object> result : results) {
            System.out.print(result.get("userID") + "\n");
             System.out.print(result.get("dynValue") + "\n");
              System.out.print(result.get("score") + "\n");
        }

    }
    public static void main(String[] args) {

    }

}
