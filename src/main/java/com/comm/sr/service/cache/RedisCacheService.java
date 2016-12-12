package com.comm.sr.service.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.service.SearchServiceFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

/**
 * Created by jasstion on 29/10/2016.
 */
public class RedisCacheService extends AbstractComponent implements CacheService<String, String> {
  private final Jedis jc;

  public RedisCacheService(Properties settings) {
    super(settings);
    Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
    String redisHost = settings.getProperty("redis.ip");
    String redisPort = settings.getProperty("redis.port");
    String passwd = settings.getProperty("redis.passwd");

    jc = new Jedis(redisHost, Integer.parseInt(redisPort));

  }

  public static void main(String[] args) {
    Properties settings = new Properties();
    try {
      settings
          .load(SearchServiceFactory.class.getClassLoader().getResourceAsStream("sr.properties"));
    } catch (IOException e) {
      throw new RuntimeException(
          "error to load sr.properties, exception:" + ExceptionUtils.getMessage(e.getCause()) + "");

    }
    CacheService<String, String> cacheService = new RedisCacheService(settings);
    cacheService.set("test_key", "test");
    System.out.print(cacheService.get("test_key"));

  }

  @Override
  public String get(String key) {

    return jc.get(key);
  }

  @Override
  public void set(String key, String value) {
    jc.set(key, value);

  }

  @Override
  public void set(String key, String value, int timeValues, TimeUnit timeUnit) {
    jc.setex(key, timeValues, value);

  }
}
