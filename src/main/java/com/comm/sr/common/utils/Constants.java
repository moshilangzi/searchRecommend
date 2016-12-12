/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.comm.sr.common.utils;

import com.comm.sr.common.entity.ThreadShardEntity;

/**
 * @author jasstion
 */
public class Constants {
  public static final String app_prefix = "searchRecommend_";
  public final static ThreadLocal<ThreadShardEntity> threadShardEntity =
      new ThreadLocal<ThreadShardEntity>();

}
