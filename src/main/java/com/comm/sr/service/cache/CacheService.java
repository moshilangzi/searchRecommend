package com.comm.sr.service.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Created by jasstion on 29/10/2016.
 */
public interface CacheService<K, V> extends Serializable {

  public V get(K key);

  public void set(K key, V value);

  public void set(K key, V value, int timeValues, TimeUnit timeUnit);
}
