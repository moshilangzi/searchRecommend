package com.comm.sr.service;

import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryService;
import com.comm.sr.service.cache.CacheService;
import com.comm.sr.service.cache.RedisCacheService;
import com.comm.sr.service.ruleAdmin.RuleAdminService;
import com.comm.sr.service.topic.KafkaTopicService;
import com.comm.sr.service.topic.TopicService;
import com.comm.sr.service.vcg.*;
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

  protected static CacheService<String,String> cacheService=null;
  public static CacheService<String,String> getCacheService(){
    if(cacheService==null){
      cacheService=new RedisCacheService(settings);
    }
    return cacheService;

  }
  public static TopicService getTopicService(){
    TopicService topicService=new KafkaTopicService(settings);
    return topicService;
  }
  protected static TopicService byteTopicService=null;
  public static TopicService getByteTopicService(){
    Properties properties=(Properties)settings.clone();
    properties.remove("serializer.class");
    if(byteTopicService==null){

      byteTopicService=new KafkaTopicService(properties);
    }

    return byteTopicService;
  }
  public static RuleAdminService getRuleAdminService(){
    RuleAdminService ruleAdminService=new RuleAdminService(settings,ServiceUtils.getCacheService());
    return ruleAdminService;
  }
  static AbstractQueryService searchService=null;
  public static AbstractQueryService getQueryService(){
    if(searchService==null){
      searchService=new EsQueryService(settings,null);
    }


    return searchService;


  }
  public static KeywordService getKeywordService(){
    KeywordService keywordService=new KeywordService(settings);
    return keywordService;


  }
  public static VcgSearchService getVcgSearchService(){
    VcgSearchService vcgSearchService=new VcgSearchService(settings,getQueryService(),getKeywordService());
    return vcgSearchService;


  }
  public static VcgBasedSearchService getVcgOnlineMockServicee(){
    VcgOnlineMockService vcgSearchService=new VcgOnlineMockService(settings,getKeywordService(),getQueryService());
    return vcgSearchService;


  }
  protected static VcgImageSearchService vcgSearchService=null;
  public static VcgImageSearchService getVcgImageSearchService(){
    if(vcgSearchService==null){
      vcgSearchService=new VcgImageSearchService(settings,getQueryService(),getCacheService(),getByteTopicService());

    }

    return vcgSearchService;


  }
}
