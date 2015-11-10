/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author jasstion
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({com.yufei.searchrecommend.solr.SolrQueryServiceTest.class, com.yufei.searchrecommend.solr.QueryGenerateTest.class, com.yufei.searchrecommend.solr.SolrQueryGeneratorTest.class, com.yufei.searchrecommend.solr.CloudSolrTest.class, com.yufei.searchrecommend.solr.AdvancedSolrQueryServiceTest.class})
public class SolrSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
