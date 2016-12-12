package com.comm.sr.service;

import java.util.Map;

/**
 * Created by jasstion on 15/6/30.
 */
public interface IRecommend<K, V> {
  public Map<K, V> recommend(Map<String, String> paras);

  public void clearAppRuleCache(String appKey);
}
