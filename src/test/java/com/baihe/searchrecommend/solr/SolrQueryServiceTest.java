/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.SortItem;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasstion
 */
public class SolrQueryServiceTest {

    SolrQueryService service = new SolrQueryService();
    BaiheQuery yufeiQuery = new BaiheQuery();

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
        QueryItem queryItem = new QueryItem("height", Lists.newArrayList("158#TO #159", "178#TO# 179"));
        QueryItem queryItem1 = new QueryItem("age", Lists.newArrayList("1988", "1999"));
        QueryItem queryItem2 = new QueryItem("gender", Lists.newArrayList("0"));
        QueryItem queryItem_date = new QueryItem("registeDate", Lists.newArrayList("2014-02-15T18:59:51Z#TO #2015-02-15T18:59:51Z", "2009-02-15T18:59:51Z#TO #2011-02-15T18:59:51Z"));
        queryItem_date.setIsFilterType(true);
        queryItem2.setIsFilterType(true);
        List<QueryItem> items = Lists.newArrayList(queryItem, queryItem1, queryItem2, queryItem_date);
        yufeiQuery.setQueryItems(items);
        yufeiQuery.setFls(Lists.newArrayList("userID", "height","dynValue","score"));
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
       // yufeiQuery.setSortItems(sortItems);
        yufeiQuery.setGender(0);
        yufeiQuery.setCacheStrategy("100");
        List<String> functionQueryList=Lists.newArrayList();
        functionQueryList.add("product(dynValue,100)");
        yufeiQuery.setFunctionQuerysList(functionQueryList);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of query method, of class SolrQueryService.
     */
    @Test
    public void testQuery() throws Exception {
        List<Map<String, Object>> results = service.query(yufeiQuery);
        for (Map<String, Object> result : results) {
            //   System.out.print(result.get("userID") + "\n");
        }

    }

    /**
     * Test of processQuery method, of class SolrQueryService.
     */
    @Test
    public void testProcessQuery() throws Exception {
        List<Map<String, Object>> results = service.processQuery(yufeiQuery);
        for (Map<String, Object> result : results) {
            System.out.print(result.get("userID") + "\n");
             System.out.print(result.get("dynValue") + "\n");
              System.out.print(result.get("score") + "\n");
        }

    }

}
