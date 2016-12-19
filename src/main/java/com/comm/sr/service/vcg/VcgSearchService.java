package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.DateTimeUtil;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.ServiceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.LocalDateTime;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.IKMatchOperation;
import org.wltea.analyzer.dic.MatchOperation;
import org.wltea.analyzer.dic.WordsLoader;

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

  private static class OffsetAndLimits{
    int goodImageOffset=0;
    int goodImageLimit=0;
    int newImageOffset=0;
    int newImageLimit=0;

    @Override public String toString() {
      return "OffsetAndLimits{" +
          "goodImageLimit=" + goodImageLimit +
          ", goodImageOffset=" + goodImageOffset +
          ", newImageOffset=" + newImageOffset +
          ", newImageLimit=" + newImageLimit +
          '}';
    }

    public int getGoodImageLimit() {
      return goodImageLimit;
    }

    public void setGoodImageLimit(int goodImageLimit) {
      this.goodImageLimit = goodImageLimit;
    }

    public int getGoodImageOffset() {
      return goodImageOffset;
    }

    public void setGoodImageOffset(int goodImageOffset) {
      this.goodImageOffset = goodImageOffset;
    }

    public int getNewImageLimit() {
      return newImageLimit;
    }

    public void setNewImageLimit(int newImageLimit) {
      this.newImageLimit = newImageLimit;
    }

    public int getNewImageOffset() {
      return newImageOffset;
    }

    public void setNewImageOffset(int newImageOffset) {
      this.newImageOffset = newImageOffset;
    }

    public OffsetAndLimits() {
      super();
    }

    public OffsetAndLimits(int goodImageLimit, int goodImageOffset, int newImageLimit,
        int newImageOffset) {
      this.goodImageLimit = goodImageLimit;
      this.goodImageOffset = goodImageOffset;
      this.newImageLimit = newImageLimit;
      this.newImageOffset = newImageOffset;
    }
  }
  public VcgSearchService(Properties settings,AbstractQueryService queryService,KeywordService keywordService) {

    super(settings);
    this.queryService=queryService;
    this.keywordService=keywordService;
  }
  public Map<String,Object> search(String searchParamsStr) throws Exception{
    logger.info(searchParamsStr);
    Map<String,Object> finalResults=Maps.newHashMap();
    List<Map<String,String>> results=Lists.newArrayList();
    SearchParams searchParams=(SearchParams)GsonHelper.jsonToObj(searchParamsStr,SearchParams.class);
    List<KeywordService.KwInfo> kwInfos=keywordService.parseInputText(searchParams.getQueryText());
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
    //过去三天
    int lastFewDays=3;
    int totalNewImage=getNewImageNum(lastFewDays,kwInfos);
    int totalGoodImage=getGoodImageNum(lastFewDays,kwInfos);

    int numPerPage=searchParams.getFetchSize();
    int pageNum=searchParams.getPageNum();
    int goodImageNumPerPage=90;
    int newImageNumPerPage=10;



    int totalImageNum=totalGoodImage+totalNewImage;
    int totalPageNum=totalImageNum/numPerPage+1;

    if(pageNum>totalPageNum){
      pageNum=totalPageNum;
    }



     OffsetAndLimits offsetAndLimits= computeImageOffsetAndLimit(numPerPage, pageNum, goodImageNumPerPage, newImageNumPerPage,
        totalNewImage, totalGoodImage);

    //根据offsetAndLimits各自获取新图和好图



    logger.info("final offsetAndLimits:"+offsetAndLimits.toString()+"");


    //query new images
    List<Map<String, String>> newImageList=getNewImageList(lastFewDays,pageNum, kwInfos, offsetAndLimits);

    List<Map<String, String>> goodImageList=getGoodImageList(lastFewDays,pageNum, kwInfos, offsetAndLimits);
    //merage two list

    List<Map<String, String>> results_=Lists.newArrayList();
    results_=new MeragerInterval().merage(goodImageList,newImageList);

//    results_.addAll(goodImageList);
//    results_.addAll(newImageList);
















    final  String imageDomain="http://goss1.asiacn.vcg.com/";


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

      String imageUrl = imageDomain + m.get("oss_id2");
      user.put("url",imageUrl);
      String dateStr=DateTimeUtil.getFormatDate(createTime,DateTimeUtil.TIME_FORMAT);
      user.put("date",dateStr);

      user.put("resId",(String)m.get("res_id"));

      user.put("keywords",(String)m.get("keywords"));

      results.add(user);






    }
    logger.debug(results.toString());
    finalResults.put("result",results);
    String queryAnalyzerStr="PageNum:"+pageNum+"; totalNewImageNum:"+totalNewImage+"; totalGoodImageNum:"+totalGoodImage+"; totalPageNum:"+totalPageNum+"; newImageOffset: "+offsetAndLimits.getNewImageOffset()+"; newImageLimit:"+offsetAndLimits.getNewImageLimit()+"; goodImageOffset:"+offsetAndLimits.getGoodImageOffset()+"; goodImageLimit:"+offsetAndLimits.getGoodImageLimit()+"";
    finalResults.put("exInfo",queryAnalyzerStr);



      return finalResults;

  }
  public List<Map<String,String>> getNewImageList(int offsetDay,int pageNum,List<KeywordService.KwInfo> kwInfos,OffsetAndLimits offsetAndLimits)
      {

        LocalDateTime from = new LocalDateTime();
        LocalDateTime to = new LocalDateTime();
        from = new LocalDateTime();
        from = from.minusDays(Math.abs(offsetDay));
        from = from.withHourOfDay(0);
        from = from.withMinuteOfHour(0);
        from = from.withSecondOfMinute(0);
        to = new LocalDateTime();
        to = to.minusDays(1);
        to = to.withHourOfDay(23);
        to = to.withMinuteOfHour(59);
        to = to.withSecondOfMinute(59);
        String dateRangeQuery=from.toDate().getTime()/1000+"TO"+to.toDate().getTime()/1000;
    List<Map<String,String>> result=null;
    String indexName="vcg_creative";
    String typeName="vcgcsdn";
    SubQuery finalQuery=new SubQuery();
    finalQuery.setLogic("AND");
    SubQuery filterQuery=new SubQuery();
    QueryItem qi=new QueryItem("onlineState", Lists.newArrayList("1"));
    filterQuery.setQueryItem(qi);
    SubQuery filterQuery1=new SubQuery();
    QueryItem qi1=new QueryItem("uploadTime", Lists.newArrayList(dateRangeQuery));
    // qi1.setIsFilterType(true);
    filterQuery1.setQueryItem(qi1);
    SubQuery preKeyQuery=new SubQuery();
    preKeyQuery.setLogic("AND");

    List<SubQuery> preQuerys=Lists.newArrayList();
    for(KeywordService.KwInfo kwInfo: kwInfos){
      SubQuery subQuery=new SubQuery();
      subQuery.setQueryItem(new QueryItem("prekey", Lists.newArrayList(kwInfo.getKwIds().iterator()), true));

      preQuerys.add(subQuery);


    }
    preQuerys.add(filterQuery1);
    preQuerys.add(filterQuery);
    preKeyQuery.setSubQuerys(preQuerys);


    finalQuery.setSubQuerys(Lists.newArrayList(preKeyQuery));






    EsCommonQuery query = new EsCommonQuery(-1,1, null, Lists.newArrayList("prekey","id"), indexName, typeName);
        query.setScoreScript(null);
        SortItem dateItem=new SortItem();
        dateItem.setFieldName("uploadTime");
        dateItem.setSort("desc");
        query.setSortItems(Lists.newArrayList(dateItem));
    query.setOffset(offsetAndLimits.getNewImageOffset());
    query.setLimit(offsetAndLimits.getNewImageLimit());
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgTest");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    logger.info(esQueryWrapper.getSearchSourceBuilder().toString());




          logger.info("日期排序查询："+esQueryWrapper.getSearchSourceBuilder().toString());

        try {
          result=queryService.query(query);
        } catch (Exception e) {
          logger.info("error to query new Images");
        }

        return result;

  }

  public List<Map<String,String>> getGoodImageList(int offsetDays,int pageNum,List<KeywordService.KwInfo> kwInfos,OffsetAndLimits offsetAndLimits){
    List<Map<String,String>> result=null;
    LocalDateTime to = new LocalDateTime();

    to = new LocalDateTime();
    to = to.minusDays(3);
    to = to.withHourOfDay(0);
    to = to.withMinuteOfHour(0);
    to = to.withSecondOfMinute(0);
    String dateRangeQuery=0+"TO"+to.toDate().getTime()/1000;
    String indexName="vcg_creative";
    String typeName="vcgcsdn";
    SubQuery finalQuery=new SubQuery();
    finalQuery.setLogic("AND");
    SubQuery filterQuery=new SubQuery();
    QueryItem qi=new QueryItem("onlineState", Lists.newArrayList("1"));
    filterQuery.setQueryItem(qi);
    qi.setIsFilterType(true);
    SubQuery filterQuery1=new SubQuery();
    QueryItem qi1=new QueryItem("uploadTime", Lists.newArrayList(dateRangeQuery));
    filterQuery1.setQueryItem(qi1);
    qi1.setIsFilterType(true);
    SubQuery preKeyQuery=new SubQuery();
    preKeyQuery.setLogic("AND");

    List<SubQuery> preQuerys=Lists.newArrayList();
    for(KeywordService.KwInfo kwInfo: kwInfos){
      SubQuery subQuery=new SubQuery();
      subQuery.setQueryItem(new QueryItem("prekey", Lists.newArrayList(kwInfo.getKwIds().iterator()), true));

      preQuerys.add(subQuery);


    }

    preKeyQuery.setSubQuerys(preQuerys);


    finalQuery.setSubQuerys(Lists.newArrayList(preKeyQuery,filterQuery,filterQuery1));




    EsCommonQuery query = new EsCommonQuery(-1,1, null, Lists.newArrayList("id"), indexName, typeName);
    //doc['uploadTime'].value+_score*100000000000
    query.setScoreScript("doc['uploadTime'].value/1000.0+_score*100000.0");
    query.setOffset(offsetAndLimits.getGoodImageOffset());
    query.setLimit(offsetAndLimits.getGoodImageLimit());
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgTest");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    logger.info(esQueryWrapper.getSearchSourceBuilder().toString());
    try {
      result=queryService.query(query);
    } catch (Exception e) {
      logger.info("error to get totalCount of NewImage");
    }
    return result;
  }
  private OffsetAndLimits computeImageOffsetAndLimit(int numPerPage, int pageNum, int goodImageNumPerPage,
      int newImageNumPerPage, int totalNewImage, int totalGoodImage) {
    OffsetAndLimits offsetAndLimits=new OffsetAndLimits();
    int maxNewImagePageNum=totalNewImage/newImageNumPerPage;
    int maxGoodImageNum=totalGoodImage/goodImageNumPerPage;
    int goodImageOffset=offsetAndLimits.getGoodImageOffset();
    int goodImageLimit=offsetAndLimits.getGoodImageLimit();
    int newImageOffset=offsetAndLimits.getNewImageOffset();
    int newImageLimit=offsetAndLimits.getNewImageLimit();
    if(maxGoodImageNum==maxNewImagePageNum){
      goodImageLimit=90;
      newImageLimit=10;
      goodImageOffset=(pageNum-1)*goodImageLimit;
      newImageOffset=(pageNum-1)*newImageLimit;

    }
    if(maxGoodImageNum>maxNewImagePageNum){
      //仅仅好图排序的开始位置
      int leftNewImageNum =totalNewImage%newImageNumPerPage;

      int begainOffset=maxNewImagePageNum*goodImageNumPerPage;

      if(pageNum<=maxNewImagePageNum){
        goodImageLimit=90;
        newImageLimit=10;
        goodImageOffset=(pageNum-1)*goodImageNumPerPage;
        newImageOffset=(pageNum-1)*newImageNumPerPage;

      }
      else if(pageNum==maxNewImagePageNum+1){
        newImageOffset=maxNewImagePageNum*newImageNumPerPage;
        newImageLimit=leftNewImageNum;

        goodImageLimit=numPerPage-leftNewImageNum;
        goodImageOffset=maxNewImagePageNum*goodImageNumPerPage;


      }
      else {
        goodImageLimit=numPerPage;
        goodImageOffset=begainOffset+(numPerPage-leftNewImageNum)+(pageNum-maxNewImagePageNum-2)*numPerPage;

      }

    }
    if(maxGoodImageNum<maxNewImagePageNum){
      //仅仅新图排序的其实位置
      int leftGoodImageNum =totalGoodImage%goodImageNumPerPage;

      int begainOffset=maxGoodImageNum*newImageNumPerPage;

      if(pageNum<=maxGoodImageNum){
        goodImageLimit=90;
        newImageLimit=10;
        goodImageOffset=(pageNum-1)*goodImageNumPerPage;
        newImageOffset=(pageNum-1)*newImageNumPerPage;

      }
     else if(pageNum==maxGoodImageNum+1){
       goodImageOffset=maxGoodImageNum*goodImageNumPerPage;
       goodImageLimit=leftGoodImageNum;

       newImageLimit=numPerPage-leftGoodImageNum;
       newImageOffset=maxGoodImageNum*newImageNumPerPage;


     }
      else{
       newImageLimit=numPerPage;
       newImageOffset=begainOffset+(numPerPage-leftGoodImageNum)+(pageNum-maxGoodImageNum-2)*numPerPage;

     }


    }
    offsetAndLimits.setGoodImageLimit(goodImageLimit);
    offsetAndLimits.setGoodImageOffset(goodImageOffset);
    offsetAndLimits.setNewImageLimit(newImageLimit);
    offsetAndLimits.setNewImageOffset(newImageOffset);
    return offsetAndLimits;
  }

  private List<Map<String, String>> merageNewImage(List<Map<String, String>> results_,
      List<Map<String, String>> results_new,int meragePolicy) {
    List<Map<String, String>> finalResults=Lists.newArrayList();
    if(meragePolicy==0){
      finalResults= new MeragerRandom().merage(results_,results_new);

    }
    if(meragePolicy==1){
      finalResults= new MeragerSimple().merage(results_,results_new);

    }
    if(meragePolicy==2){
      finalResults= new MeragerInterval().merage(results_,results_new);

    }
    return finalResults;



  }


  public static interface   NewImageMerager{
    public List<Map<String, String>> merage(List<Map<String, String>> results_,
        List<Map<String, String>> results_new);

  }
  public static class MeragerRandom implements NewImageMerager{

    @Override public List<Map<String, String>> merage(List<Map<String, String>> results_,
        List<Map<String, String>> results_new) {
      List<Map<String, String>> finalResults=Lists.newArrayList();
      finalResults.addAll(results_);
      finalResults.addAll(results_new);
      Collections.shuffle(finalResults);

      return finalResults;
    }
  }
  public static class MeragerSimple implements NewImageMerager{

     public List<Map<String, String>> merage(List<Map<String, String>> results_,
        List<Map<String, String>> results_new) {
       List<Map<String, String>> finalResults=Lists.newArrayList();
       finalResults.addAll(results_);
       finalResults.addAll(results_new);

       return finalResults;
    }
  }
  public static class MeragerInterval implements NewImageMerager{

    @Override public List<Map<String, String>> merage(List<Map<String, String>> results_,
        List<Map<String, String>> results_new) {
      List<Map<String, String>> finalResults=Lists.newArrayList();
      int interval=9;
      int totalSize=results_.size()+results_new.size();

      if(!results_new.isEmpty()){

        if(results_.size()==9*results_new.size()){
          //制定位置防止新图片
          int length=results_new.size();
          for(int i=1;i<=length;i++){
            finalResults.addAll(results_.subList((i-1)*9,i*9));
            finalResults.add(results_new.get(i-1));


          }


        }
        else {
          finalResults.addAll(results_);
          finalResults.addAll(results_new);

        }


      }
      else {
        finalResults.addAll(results_);
      }





      return finalResults;

    }
  }

  public static class SearchParams{
  String queryText=null;
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
public static List<String> getNewImageOfCurrentPageNum(List<String> goodImages, List<String> totalNewImages,int pageNum,int newImageNumPerPage){
  Set<String> goodImagesSet=Sets.newHashSet(goodImages);
  List<String> finalNewImages=Lists.newArrayList();
  int location=-1;
  int begain=newImageNumPerPage*(pageNum-1);
  int end=newImageNumPerPage*pageNum-1;


  for(int i=0;i<totalNewImages.size();i++){
    if(finalNewImages.size()==newImageNumPerPage){
      break;

    }
    String currentNewImageId=totalNewImages.get(i);
    if(goodImagesSet.contains(currentNewImageId)){
      continue;

    }
    else {
      location+=1;
    }

    if(location>=begain&&location<=end){
      finalNewImages.add(currentNewImageId);

    }



  }

  return finalNewImages;

}

public static void main(String[] args) throws Exception {

//  String scoreScript="0.0";//"long uploadTime=doc['uploadTime'].value;def now = new Date() ;long comparedTime=now.getTime()-uploadTime; int hours=comparedTime/1000/60/60/24/1000000;if (hours>3) _score; else _score+100.0";
//
//  final int maxPageNumForNew=100;
//  final int numPerPage=100;
//  final int newImageNumPerPage=10;
//  final int goodImageNumPerPage=numPerPage-newImageNumPerPage;
//  final int totalNewImageNum=maxPageNumForNew*newImageNumPerPage+goodImageNumPerPage*maxPageNumForNew;
//  final int pageNum=1;
//  String inputText="蓝天";
//  int withinHours=24*3;
//
//  SearchParams searchParams=new SearchParams();
//  searchParams.setQueryText(inputText);
//  searchParams.setScoreScript(scoreScript);
//  searchParams.setFetchSize(numPerPage);
//  searchParams.setPageNum(pageNum);
//  VcgSearchService vcgSearchService= ServiceUtils.getVcgSearchService();
//  String queryStr=GsonHelper.objToJson(searchParams);
//
//
//  List<Map<String,String>> results =(List<Map<String,String>>)vcgSearchService.search(queryStr).get("results");
//  System.out.print(results.size());
  int numPerPage=100;
  int pageNum=4726;
  int goodImageNumPerPage=90;
  int newImageNumPerPage=10;
  int totalNewImage=4814;
  int totalGoodImage=467694;
  VcgSearchService vcgSearchService= ServiceUtils.getVcgSearchService();
  OffsetAndLimits offsetAndLimits=vcgSearchService.computeImageOffsetAndLimit(numPerPage, pageNum,
      goodImageNumPerPage, newImageNumPerPage, totalNewImage, totalGoodImage);
  System.out.print(offsetAndLimits.toString()+"\n");











}



  public int getNewImageNum(int offsetDay,List<KeywordService.KwInfo> kwInfos) throws Exception {
    int totalCount=0;

    LocalDateTime from = new LocalDateTime();
    LocalDateTime to = new LocalDateTime();
    from = new LocalDateTime();
    from = from.minusDays(Math.abs(offsetDay));
    from = from.withHourOfDay(0);
    from = from.withMinuteOfHour(0);
    from = from.withSecondOfMinute(0);
    to = new LocalDateTime();
    to = to.minusDays(1);
    to = to.withHourOfDay(23);
    to = to.withMinuteOfHour(59);
    to = to.withSecondOfMinute(59);
    String dateRangeQuery=from.toDate().getTime()/1000+"TO"+to.toDate().getTime()/1000;
    String indexName="vcg_creative";
    String typeName="vcgcsdn";
    SubQuery finalQuery=new SubQuery();
    finalQuery.setLogic("AND");
    SubQuery filterQuery=new SubQuery();
    QueryItem qi=new QueryItem("onlineState", Lists.newArrayList("1"));
    filterQuery.setQueryItem(qi);
    SubQuery filterQuery1=new SubQuery();
    QueryItem qi1=new QueryItem("uploadTime", Lists.newArrayList(dateRangeQuery));
    filterQuery1.setQueryItem(qi1);
    SubQuery preKeyQuery=new SubQuery();
    preKeyQuery.setLogic("AND");

    List<SubQuery> preQuerys=Lists.newArrayList();
    for(KeywordService.KwInfo kwInfo: kwInfos){
      SubQuery subQuery=new SubQuery();
      subQuery.setQueryItem(new QueryItem("prekey", Lists.newArrayList(kwInfo.getKwIds().iterator()), true));

      preQuerys.add(subQuery);


    }
    preQuerys.add(filterQuery1);
    preQuerys.add(filterQuery);
    preKeyQuery.setSubQuerys(preQuerys);


    finalQuery.setSubQuerys(Lists.newArrayList(preKeyQuery));




    EsCommonQuery query = new EsCommonQuery(1,1, null, Lists.newArrayList("id"), indexName, typeName);
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgTest");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    logger.info(esQueryWrapper.getSearchSourceBuilder().toString());
    try {
      Map<String,Object> result=queryService.queryAll(query);
      Map<String,Object> exInfo=(Map<String,Object>)result.get("exInfo");
      totalCount=Integer.parseInt(String.valueOf(exInfo.get("totalCount")));
    } catch (Exception e) {
      logger.info("error to get totalCount of NewImage");
    }

    return totalCount;

  }
  public int getGoodImageNum(int offsetDay,List<KeywordService.KwInfo> kwInfos){
    int totalCount=0;

    LocalDateTime to = new LocalDateTime();

    to = new LocalDateTime();
    to = to.minusDays(4);
    to = to.withHourOfDay(23);
    to = to.withMinuteOfHour(59);
    to = to.withSecondOfMinute(59);
    String dateRangeQuery=0+"TO"+to.toDate().getTime()/1000;
    String indexName="vcg_creative";
    String typeName="vcgcsdn";
    SubQuery finalQuery=new SubQuery();
    finalQuery.setLogic("AND");
    SubQuery filterQuery=new SubQuery();
    QueryItem qi=new QueryItem("onlineState", Lists.newArrayList("1"));
    filterQuery.setQueryItem(qi);
    SubQuery filterQuery1=new SubQuery();
    QueryItem qi1=new QueryItem("uploadTime", Lists.newArrayList(dateRangeQuery));
    filterQuery1.setQueryItem(qi1);
    SubQuery preKeyQuery=new SubQuery();
    preKeyQuery.setLogic("AND");

    List<SubQuery> preQuerys=Lists.newArrayList();
    for(KeywordService.KwInfo kwInfo: kwInfos){
      SubQuery subQuery=new SubQuery();
      subQuery.setQueryItem(new QueryItem("prekey", Lists.newArrayList(kwInfo.getKwIds().iterator()), true));

      preQuerys.add(subQuery);


    }
    preQuerys.add(filterQuery1);
    preQuerys.add(filterQuery);
    preKeyQuery.setSubQuerys(preQuerys);


    finalQuery.setSubQuerys(Lists.newArrayList(preKeyQuery));




    EsCommonQuery query = new EsCommonQuery(1,1, null, Lists.newArrayList("id"), indexName, typeName);
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgTest");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    logger.info(esQueryWrapper.getSearchSourceBuilder().toString());
    try {
      Map<String,Object> result=queryService.queryAll(query);
      Map<String,Object> exInfo=(Map<String,Object>)result.get("exInfo");
      totalCount=Integer.parseInt(String.valueOf(exInfo.get("totalCount")));
    } catch (Exception e) {
      logger.info("error to get totalCount of NewImage");
    }

    return totalCount;

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
    logger.debug(sql);
    results=con.createQuery(sql).executeAndFetchTable().asList();




    return results;

  }
}