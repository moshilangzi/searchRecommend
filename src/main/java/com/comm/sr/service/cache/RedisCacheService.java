package com.comm.sr.service.cache;

import com.comm.sr.common.component.AbstractComponent;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jasstion on 29/10/2016.
 */
public class RedisCacheService extends AbstractComponent implements CacheService<String,String> {
    private final JedisCluster jc;
    public RedisCacheService(Properties settings) {
        super(settings);
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        String redisHost=settings.getProperty("redis.ip");
        String redisPort=settings.getProperty("redis.port");

        jedisClusterNodes.add(new HostAndPort(redisHost, Integer.parseInt(redisPort)));
     jc = new JedisCluster(jedisClusterNodes);

    }


    @Override
    public String get(String key) {

        return jc.get(key);
    }

    @Override
    public void set(String key, String value) {
        jc.set(key,value);

    }

    @Override
    public void set(String key, String value, int timeValues, TimeUnit timeUnit) {
        jc.setex(key,timeValues,value);

    }
}
