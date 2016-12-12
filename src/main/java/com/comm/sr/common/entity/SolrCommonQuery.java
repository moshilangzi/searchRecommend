package com.comm.sr.common.entity;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by jasstion on 12/11/2016.
 */
public class SolrCommonQuery extends CommonQuery {
  private String collectionName = null;
  private List<String> functionQuerysList = Lists.newArrayList();

  public SolrCommonQuery(String collectionName) {
    super();
    this.collectionName = collectionName;
  }

  public SolrCommonQuery(int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls,
      String collectionName) {
    super(pageNum, pageSize, sortItems, fls);
    this.collectionName = collectionName;
  }

  public List<String> getFunctionQuerysList() {
    return functionQuerysList;
  }

  public void setFunctionQuerysList(List<String> functionQuerysList) {
    this.functionQuerysList = functionQuerysList;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  @Override
  public String toString() {
    return "SolrCommonQuery{" + "collectionName='" + collectionName + '\'' + "} "
        + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SolrCommonQuery)) return false;

    SolrCommonQuery that = (SolrCommonQuery) o;

    return !(getCollectionName() != null ? !getCollectionName().equals(that.getCollectionName())
        : that.getCollectionName() != null);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getCollectionName() != null ? getCollectionName().hashCode() : 0);
    return result;
  }
}
