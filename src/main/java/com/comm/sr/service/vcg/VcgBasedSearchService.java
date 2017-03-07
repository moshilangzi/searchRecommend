package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;

import java.util.Map;
import java.util.Properties;

/**
 * Created by jasstion on 16/02/2017.
 */
public abstract  class VcgBasedSearchService extends AbstractComponent {
  public VcgBasedSearchService(Properties settings) {
    super(settings);
  }
  public abstract Map<String,Object> search(String searchParamsStr)throws Exception;

  public static class SearchParams{
    String queryText=null;
    String serviceName=null;
    String scoreScript=null;
    int withHours=0;
    int fetchSize=100;
    boolean ifUseSecondSortBasedDate=false;
    int newImageNumPerPage=10;
    int pageNum=1;
    //1：简单的每页一半新图放在好图后面，2：新图和好图随机打乱， 3：新图和好图间隔固定数量交叉出现
    int newImageMeragePolicy=1;
    double dateWeight=2.0;

    public SearchParams() {
      super();

    }

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public int getNewImageNumPerPage() {
      return newImageNumPerPage;
    }

    public void setNewImageNumPerPage(int newImageNumPerPage) {
      this.newImageNumPerPage = newImageNumPerPage;
    }

    public int getPageNum() {
      return pageNum;
    }

    public void setPageNum(int pageNum) {
      this.pageNum = pageNum;
    }

    public double getDateWeight() {
      return dateWeight;
    }

    public void setDateWeight(double dateWeight) {
      this.dateWeight = dateWeight;
    }

    public int getNewImageMeragePolicy() {
      return newImageMeragePolicy;
    }

    public void setNewImageMeragePolicy(int newImageMeragePolicy) {
      this.newImageMeragePolicy = newImageMeragePolicy;
    }

    public int getFetchSize() {
      return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
      this.fetchSize = fetchSize;
    }

    public SearchParams(boolean ifUseSecondSortBasedDate, String queryText, String scoreScript,
        int withHours) {
      this.ifUseSecondSortBasedDate = ifUseSecondSortBasedDate;
      this.queryText = queryText;
      this.scoreScript = scoreScript;
      this.withHours = withHours;
    }

    public boolean isIfUseSecondSortBasedDate() {
      return ifUseSecondSortBasedDate;
    }

    public void setIfUseSecondSortBasedDate(boolean ifUseSecondSortBasedDate) {
      this.ifUseSecondSortBasedDate = ifUseSecondSortBasedDate;
    }

    public String getQueryText() {
      return queryText;
    }

    public void setQueryText(String queryText) {
      this.queryText = queryText;
    }

    public String getScoreScript() {
      return scoreScript;
    }

    public void setScoreScript(String scoreScript) {
      this.scoreScript = scoreScript;
    }

    public int getWithHours() {
      return withHours;
    }

    public void setWithHours(int withHours) {
      this.withHours = withHours;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SearchParams)) return false;

      SearchParams that = (SearchParams) o;

      if (withHours != that.withHours) return false;
      if (ifUseSecondSortBasedDate != that.ifUseSecondSortBasedDate) return false;
      if (!queryText.equals(that.queryText)) return false;
      return scoreScript.equals(that.scoreScript);

    }

    @Override public int hashCode() {
      int result = queryText.hashCode();
      result = 31 * result + scoreScript.hashCode();
      result = 31 * result + withHours;
      result = 31 * result + (ifUseSecondSortBasedDate ? 1 : 0);
      return result;
    }
  }
}
