package com.comm.sr.service.cache;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Properties;

/**
 * Created by jasstion on 03/11/2016.
 */
public class RedisCacheServiceTest extends TestCase {

    CacheService cacheService=null;

    public void testGet() throws Exception {
        String key="testKey";
        String value="this is a test hello value";
        cacheService.set(key,value);
        String expectedValue= (String) cacheService.get(key);
        Assert.assertEquals(value,expectedValue);

    }

    public void testSet() throws Exception {

    }



    public void testSet1() throws Exception {

    }

    public void setUp() throws Exception {
        super.setUp();
        Properties settings=null;
        settings= new Properties();
        settings.put("redis.ip","localhost");
        settings.put("redis.port","6379");
        cacheService=new RedisCacheService(settings);


    }
    public void tearDown() throws Exception {
        super.tearDown();



    }





}