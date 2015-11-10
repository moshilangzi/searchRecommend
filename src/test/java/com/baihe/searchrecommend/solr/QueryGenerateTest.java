/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import static com.yufei.searchrecommend.solr.QueryGenerate.generateInteredtedUsersQuery;
import static com.yufei.searchrecommend.solr.QueryGenerate.generateSimilarUsersQuery;
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
public class QueryGenerateTest {
            int[] cities = {864413, 861101};

    
    public QueryGenerateTest() {
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
     * Test of generateSimilarUsersQuery method, of class QueryGenerate.
     */
    @Test
    public void testGenerateSimilarUsersQuery() {
         
        System.out.print(generateSimilarUsersQuery(1988, 864413, 185));
    }

    /**
     * Test of generateInteredtedUsersQuery method, of class QueryGenerate.
     */
    @Test
    public void testGenerateInteredtedUsersQuery() {
        System.out.print(generateInteredtedUsersQuery(2000, 1977, 199, 170, cities) + "\n");
    }

    /**
     * Test of main method, of class QueryGenerate.
     */
    @Test
    public void testMain() {
    }
    
}
