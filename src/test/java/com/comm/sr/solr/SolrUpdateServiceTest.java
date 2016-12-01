/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.solr;

import com.comm.sr.common.core.UpdateService;
import com.comm.sr.common.solr.SolrUpdateService;
import com.google.common.collect.Maps;
import org.junit.*;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jasstion
 */
public class SolrUpdateServiceTest {

    public SolrUpdateServiceTest() {
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
     * Test of update method, of class SolrUpdateService.
     */
    @Test
    public void testUpdate() {
        //45.15,-93.85
        try {
            //location_0_coordinate
            //location_1_coordinate
            UpdateService updateService = new SolrUpdateService();
            Map<String, String> updatedMap = Maps.newHashMap();
            String userID = "8911";
            updatedMap.put("id", userID);
            updatedMap.put("age", "1988");
            updatedMap.put("height", "1999");
            updatedMap.put("nickname", "jasstion4143");
            updatedMap.put("userID", userID);
            updatedMap.put("collectionName", "commSearchMain_woman");
            updatedMap.put("location", "45.15,-93.857");
            updateService.update(updatedMap);

            updatedMap = Maps.newHashMap();
            userID = "511";
            updatedMap.put("id", userID);
            updatedMap.put("age", "1988");
            updatedMap.put("height", "1999");
            updatedMap.put("nickname", "jasstion21514");
            updatedMap.put("userID", userID);
            updatedMap.put("collectionName", "commSearchMain_woman");
            updatedMap.put("location", "45.17614,-13.87341");
            updateService.update(updatedMap);

            updatedMap = Maps.newHashMap();
            userID = "61";
            updatedMap.put("id", userID);
            updatedMap.put("age", "1988");
            updatedMap.put("height", "1999");
            updatedMap.put("nickname", "jasstion316134");
            updatedMap.put("userID", userID);
            updatedMap.put("collectionName", "commSearchMain_woman");
            updatedMap.put("location", "2.17614,-13.87341");
            updateService.update(updatedMap);

        } catch (MalformedURLException ex) {
            Logger.getLogger(SolrUpdateServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Test of add method, of class SolrUpdateService.
     */
    @Test
    public void testAdd() {
    }

    /**
     * Test of delete method, of class SolrUpdateService.
     */
    @Test
    public void testDelete() {
        try {
            UpdateService updateService = new SolrUpdateService();
            Map<String, String> updatedMap = Maps.newHashMap();
            String userID = "51";
            updatedMap.put("id", userID);

            updatedMap.put("collectionName", "commSearchMain_woman");
            updateService.delete(updatedMap);
            updatedMap = Maps.newHashMap();
            userID = "2";
            updatedMap.put("id", userID);

            updatedMap.put("collectionName", "commSearchMain_woman");
            updateService.delete(updatedMap);
            updatedMap = Maps.newHashMap();
            userID = "3";
            updatedMap.put("id", userID);

            updatedMap.put("collectionName", "commSearchMain_woman");
            updateService.delete(updatedMap);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SolrUpdateServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
