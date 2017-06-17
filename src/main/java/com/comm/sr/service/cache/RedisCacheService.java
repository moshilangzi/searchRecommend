package com.comm.sr.service.cache;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.service.SearchServiceFactory;
import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.exception.ExceptionUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jasstion on 29/10/2016.
 */
public class RedisCacheService extends AbstractComponent implements CacheService<String,String> {
    private JedisPool jedisPool=null;
        public RedisCacheService(Properties settings) {
        super(settings);
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        String redisHost=settings.getProperty("redis.ip");
        String redisPort=settings.getProperty("redis.port");
        String passwd=settings.getProperty("redis.passwd");
       // jc = new Jedis(redisHost,Integer.parseInt(redisPort));


        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(500);
         jedisPool = new JedisPool(
                poolConfig,
                redisHost,
                Integer.parseInt(redisPort),
                3000,
                null
        );


    }


    @Override
    public String get(String key) {
        Stopwatch stopwatch=Stopwatch.createStarted();
        String value=null;
        Jedis jedis=null;
        try {
             jedis= jedisPool.getResource();
            value = jedis.get(key);
        }catch (Exception e){
            jedisPool.returnBrokenResource(jedis);

        }finally {

            jedisPool.returnResource(jedis);
        }
        stopwatch.stop();
        long timeSeconds=stopwatch.elapsed(TimeUnit.MILLISECONDS)/1000;
        logger.info("spent "+timeSeconds+" s to get value from redis by key:"+key+"");


        return value;
    }

    @Override
    public void set(String key, String value) {
        Jedis jedis=null;
        try {
            jedis= jedisPool.getResource();
            jedis.set(key,value);
        }catch (Exception e){
            jedisPool.returnBrokenResource(jedis);

        }finally {

            jedisPool.returnResource(jedis);
        }


    }

    @Override
    public void set(String key, String value, int timeValues, TimeUnit timeUnit) {
        jedisPool.getResource().setex(key,timeValues,value);

    }
    public static void main(String[] args){
        Properties settings=new Properties();
        try {
            settings.load(SearchServiceFactory.class.getClassLoader().getResourceAsStream("sr.properties"));
        } catch (IOException e) {
            throw new RuntimeException("error to load sr.properties, exception:"+ ExceptionUtils.getMessage(e.getCause())+"");


        }
        CacheService<String,String> cacheService=new RedisCacheService(settings);
        cacheService.set("test_key","test");
        System.out.print(cacheService.get("test_key"));


    }
}
