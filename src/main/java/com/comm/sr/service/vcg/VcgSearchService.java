package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.DateTimeUtil;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.ServiceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.IKMatchOperation;
import org.wltea.analyzer.dic.MatchOperation;
import org.wltea.analyzer.dic.WordsLoader;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by jasstion on 07/12/2016.
 */
public class VcgSearchService extends AbstractComponent{
  static{
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    WordsLoader wordsLoader = new KeywordLoader();
    MatchOperation matchOperation = new IKMatchOperation();

    Dictionary dictionary = new Dictionary(wordsLoader, matchOperation);
    Dictionary.setDictionary(dictionary);
  }
  protected AbstractQueryService queryService=null;
  protected KeywordService keywordService=null;
  public VcgSearchService(Properties settings,AbstractQueryService queryService,KeywordService keywordService) {

    super(settings);
    this.queryService=queryService;
    this.keywordService=keywordService;
  }
  public List<Map<String,String>> search(String searchParamsStr) throws Exception{
    logger.info(searchParamsStr);
    List<Map<String,String>> results=Lists.newArrayList();
    SearchParams searchParams=(SearchParams)GsonHelper.jsonToObj(searchParamsStr,SearchParams.class);
    List<KeywordService.KwInfo> kwInfos=keywordService.parseInputText(searchParams.getQueryText());


    String indexName="vcg_creative";
    String typeName="vcgcsdn";
    String scoreScript=searchParams.getScoreScript();
    SubQuery finalQuery=new SubQuery();
    finalQuery.setLogic("AND");
    SubQuery filterQuery=new SubQuery();

    QueryItem qi=new QueryItem("onlineState", Lists.newArrayList("1"));qi.setIsFilterType(true);filterQuery.setQueryItem(qi);
    SubQuery preKeyQuery=new SubQuery();
    preKeyQuery.setLogic("AND");
    Set<String> kwIds= Sets.newHashSet();
    kwInfos.forEach(new Consumer<KeywordService.KwInfo>() {
      @Override public void accept(KeywordService.KwInfo kwInfo) {
        if(kwInfo.getKwIds()!=null){
          for(String s:kwInfo.getKwIds()){
            kwIds.add(s);
          }

        }

      }
    });
    List<SubQuery> preQuerys=Lists.newArrayList();
    for(KeywordService.KwInfo kwInfo: kwInfos){
      SubQuery subQuery=new SubQuery();
      subQuery.setQueryItem(new QueryItem("prekey", Lists.newArrayList(kwInfo.getKwIds().iterator()), true));

      preQuerys.add(subQuery);


    }
    preKeyQuery.setSubQuerys(preQuerys);


    finalQuery.setSubQuerys(Lists.newArrayList(filterQuery,preKeyQuery));
    EsCommonQuery query = new EsCommonQuery(1, searchParams.getFetchSize(), null, Lists.newArrayList("prekey","id"), indexName, typeName);
    query.setScoreScript(scoreScript);
    query.setSubQuery(finalQuery);
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);
    logger.info(esQueryWrapper.getSearchSourceBuilder().toString());


    final  String imageDomain="http://goss1.asiacn.vcg.com/";
    List<Map<String, String>> results_= queryService.query(query);
    List<String> imageIds=Lists.newArrayList();
    Map<String,Integer> idIndexMap=Maps.newHashMap();


    for(int i=0;i<results_.size();i++){
      imageIds.add((String) results_.get(i).get("id"));


    }
    List<Map<String, Object>> imagesMapList=getImageInfo(imageIds);
    for(int i=0;i<imagesMapList.size();i++ ){
      idIndexMap.put(String.valueOf(imagesMapList.get(i).get("id")),i);
    }

    for (Map<String,String> user:results_) {
      String id=String.valueOf(user.get("id"));
      Map<String,Object> m=imagesMapList.get(idIndexMap.get(id));



      Date createTime = (Date)m.get("create_time");
      int hours = DateTimeUtil.getIntCompareToCurrDateHour(createTime);
      if (hours > 3) {
        user.put("new", "true");
      }
      else{
        user.put("new","false");
      }
      String imageUrl = imageDomain + m.get("oss_id2");
      user.put("url",imageUrl);

      user.put("resId",(String)m.get("res_id"));

      user.put("keywords",(String)m.get("keywords"));

      results.add(user);






    }
    logger.debug(results.toString());



      return results;

  }




public static class SearchParams{
  String queryText=null;
  String scoreScript=null;
  int withHours=0;
  int fetchSize=100;
  boolean ifUseSecondSortBasedDate=false;

  public SearchParams() {
    super();

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


public static void main(String[] args) throws Exception {

  String scoreScript="long uploadTime=doc['uploadTime'].value;def now = new Date() ;long comparedTime=now.getTime()-uploadTime; int hours=comparedTime/1000/60/60/24/1000000;if (hours>3) _score; else _score+100.0";


  String inputText="蓝天";
  boolean ifUseSecondSortBasedDate=false;
  int withinHours=24*3;

  SearchParams searchParams=new SearchParams();
  searchParams.setIfUseSecondSortBasedDate(ifUseSecondSortBasedDate);
  searchParams.setQueryText(inputText);
  searchParams.setScoreScript(scoreScript);
  searchParams.setWithHours(24*3);
  searchParams.setFetchSize(12);

  VcgSearchService vcgSearchService= ServiceUtils.getVcgSearchService();
  String queryStr=GsonHelper.objToJson(searchParams);
  String encodeStr=URLEncoder.encode(queryStr, "UTF-8");
  System.out.print(encodeStr+"\n");
  System.out.print(queryStr.toString()+"\n");
  System.out.print(URLDecoder.decode(encodeStr,"UTF-8"));

  List<Map<String,String>> results =vcgSearchService.search(queryStr);
  System.out.print(results.toString());







}


  public  List<Map<String,Object>> getImageInfo(List<String> imageIds) throws Exception{
    List<Map<String,Object>> results=Lists.newArrayList();
    String mysqlUrl=settings.getProperty("mysqlUrl");
    String mysqlUserName=settings.getProperty("mysqlUserName");
    String mysqlPasswd=settings.getProperty("mysqlPasswd");



    final Sql2o sql2o =
        new Sql2o(mysqlUrl,mysqlUserName,mysqlPasswd);
    Connection con = sql2o.open();
    String tableName = "resource";
    StringBuffer clause=new StringBuffer();
    clause.append("(").append(org.apache.commons.lang3.StringUtils.join(imageIds,",")).append(")");

    String sql="select * from resource where id in "+clause.toString();
    results=con.createQuery(sql).executeAndFetchTable().asList();




    return results;

  }
}