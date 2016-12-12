/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.comm.sr.common.entity;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author jasstion
 */
public class AbstractQuery implements Serializable {

  protected int pageNum = -1;
  protected int pageSize = 18;
  protected List<SortItem> sortItems = Lists.newArrayList();
  protected List<String> fls = Lists.newArrayList();
  protected String cacheStrategy = null;

  public AbstractQuery(int pageNum, int pageSize) {
    this.pageNum = pageNum;
    this.pageSize = pageSize;
  }

  public AbstractQuery() {
    super();
  }

  public AbstractQuery(int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls) {
    this.pageNum = pageNum;
    this.pageSize = pageSize;
    this.sortItems = sortItems;
    this.fls = fls;

  }

  public String getCacheStrategy() {
    return cacheStrategy;
  }

  public void setCacheStrategy(String cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }

  public List<String> getFls() {
    return fls;
  }

  public void setFls(List<String> fls) {
    this.fls = fls;
  }

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public List<SortItem> getSortItems() {
    return sortItems;
  }

  public void setSortItems(List<SortItem> sortItems) {
    this.sortItems = sortItems;
  }

}
