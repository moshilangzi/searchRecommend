package com.comm.sr.service;

import com.comm.sr.service.cache.CacheService;
import com.comm.sr.service.cache.RedisCacheService;
import com.comm.sr.service.ruleAdmin.RuleAdminService;
import com.comm.sr.service.topic.KafkaTopicService;
import com.comm.sr.service.topic.TopicService;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by jasstion on 15/11/2016.
 */
public class ServiceUtils {

  protected final static Properties settings=new Properties();
  static {
    try {
      settings.load(SearchServiceFactory.class.getClassLoader().getResourceAsStream("sr.properties"));
    } catch (IOException e) {
      throw new RuntimeException("error to load sr.properties, exception:"+ ExceptionUtils
          .getMessage(e.getCause())+"");


    }
  }

  public static CacheService<String,String> getCacheService(){
    CacheService<String,String> cacheService=new RedisCacheService(settings);
    return cacheService;

  }
  public static TopicService getTopicService(){
    TopicService topicService=new KafkaTopicService(settings);
    return topicService;
  }
  public static RuleAdminService getRuleAdminService(){
    RuleAdminService ruleAdminService=new RuleAdminService(settings,ServiceUtils.getCacheService());
    return ruleAdminService;
  }

}
