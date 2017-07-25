package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.distance.DistanceMeasureFactory;
import com.comm.sr.common.elasticsearch.EsQueryService;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.kd.KDTree;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.ServiceUtils;
import com.comm.sr.service.cache.CacheService;
import com.comm.sr.service.topic.TopicService;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yufei.utils.ExceptionUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by jasstion on 15/03/2017.
 */
public class VcgImageSearchService extends AbstractComponent{

  protected AbstractQueryService queryService=null;
  protected CacheService<String,String> cacheService=null;
  protected TopicService bytesTopicService=null;
  protected final static Map<String,Integer> imageCenterVectorToGroupMap=Maps.newHashMap();
  protected   Sql2o sql2o =null;
  static {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    }catch (Exception e){

    }
  }



  public VcgImageSearchService(Properties settings, AbstractQueryService queryService, CacheService<String,String> cacheService, TopicService bytesTopicService) {
    super(settings);
    this.queryService=queryService;
    this.cacheService=cacheService;
    String mysqlUrl=settings.getProperty("mysqlUrl");
    String mysqlUserName=settings.getProperty("mysqlUserName");
    String mysqlPasswd=settings.getProperty("mysqlPasswd");



    this.sql2o= new Sql2o(mysqlUrl,mysqlUserName,mysqlPasswd);
    this.bytesTopicService=bytesTopicService;



  }


public static class ImageSearchParams {
  private String distanceType=null;
  private String cNNFeatures=null;
  private String imageId=null;
  private int matchedTopNum=20;
  //url,imageId....
  private String fields=null;
  private int pageNum=1;
  private int fetchSize=100;
  private String matchPictureUrl;
  //vec or text
  private String searchPolicy=null;
  private int groupNum=1;
  private String clusterIndentity="vcgImage";
  private double scoreThresholdValue=10d;
  //creative or editorial,  contains 'editorial' is editorial, else creative
  private String imageIndexName="vcg_image";



  @Override public String toString() {
    return "ImageSearchParams{" +
        "cNNFeatures='" + cNNFeatures + '\'' +
        ", distanceType='" + distanceType + '\'' +
        ", matchedTopNum=" + matchedTopNum +
        '}';
  }

  public String getClusterIndentity() {
    return clusterIndentity;
  }

  public void setClusterIndentity(String clusterIndentity) {
    this.clusterIndentity = clusterIndentity;
  }

  public String getImageIndexName() {
    return imageIndexName;
  }

  public void setImageIndexName(String imageIndexName) {
    this.imageIndexName = imageIndexName;
  }

  public double getScoreThresholdValue() {
    return scoreThresholdValue;
  }

  public void setScoreThresholdValue(double scoreThresholdValue) {
    this.scoreThresholdValue = scoreThresholdValue;
  }

  public int getGroupNum() {
    return groupNum;
  }

  public void setGroupNum(int groupNum) {
    this.groupNum = groupNum;
  }

  public String getMatchPictureUrl() {
    return matchPictureUrl;
  }

  public void setMatchPictureUrl(String matchPictureUrl) {
    this.matchPictureUrl = matchPictureUrl;
  }

  public String getSearchPolicy() {
    return searchPolicy;
  }

  public String getFields() {
    return fields;
  }

  public void setFields(String fields) {
    this.fields = fields;
  }

  public void setSearchPolicy(String searchPolicy) {
    this.searchPolicy = searchPolicy;
  }

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getcNNFeatures() {
    return cNNFeatures;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public void setcNNFeatures(String cNNFeatures) {
    this.cNNFeatures = cNNFeatures;
  }

  public String getDistanceType() {
    return distanceType;
  }

  public void setDistanceType(String distanceType) {
    this.distanceType = distanceType;
  }

  public int getMatchedTopNum() {
    return matchedTopNum;
  }

  public void setMatchedTopNum(int matchedTopNum) {
    this.matchedTopNum = matchedTopNum;
  }
}
   //imageId 不为空，vcg image图片搜索， imageId为空，cNNFeatures不为空，外部图片搜索，

   public Map<String, Object> search(String searchParamsStr) throws Exception {
     Map<String,Object> finalResults= Maps.newHashMap();

    ImageSearchParams searchParams=(ImageSearchParams) GsonHelper
        .jsonToObj(searchParamsStr, ImageSearchParams.class);
     EsCommonQuery query=null;
     //maybe creative or editorial
     final String indexName=searchParams.getImageIndexName();
     final String typeName="image";

     if(searchParams.getImageId()==null&&searchParams.getcNNFeatures()==null&&searchParams.getMatchPictureUrl()==null){
       //list images
       int pageNumber=searchParams.getPageNum();
       int pageSize=searchParams.getFetchSize();


       query= new EsCommonQuery(pageNumber,pageSize, Lists.newArrayList(new SortItem("imageId","desc")), Lists.newArrayList("imageId"), indexName, typeName);
       SubQuery finalSubQuery=new SubQuery();
       QueryItem queryItem=new QueryItem("groupId",Lists.newArrayList("-1TO100000"));
       finalSubQuery.setQueryItem(queryItem);
       query.setSubQuery(finalSubQuery);
       query.setClusterIdentity(searchParams.getClusterIndentity());
     }
     else if(searchParams.getMatchPictureUrl()!=null){

       String imageId = UUID.randomUUID().toString();
       TopicService topicBytesService = ServiceUtils.getByteTopicService();
       Stopwatch stopwatch=Stopwatch.createStarted();


       //byte[] imageBytes= HttpUtils.executeWithHttpImageUrl(searchParams.getMatchPictureUrl(),null);

       URL url = new URL(searchParams.getMatchPictureUrl());

       byte[] imageBytes = IOUtils.toByteArray(url.openStream());
       stopwatch.stop();
       long timeSeconds=stopwatch.elapsed(TimeUnit.MILLISECONDS)/1000;
       logger.info("spent "+timeSeconds+" s to fetch "+searchParams.getMatchPictureUrl()+"");
       topicBytesService.publishTopicMessage("uploadedImageForSearch", imageId.getBytes(), imageBytes);
       Thread.currentThread().sleep(3*1000);
       CacheService<String, String> redisCacheService = ServiceUtils.getCacheService();
       String features = redisCacheService.get(imageId);
       if(features==null){
         throw new  RuntimeException("system error! detail message: image_upload topic not consumed correctly!");

       }
       logger.debug("search images based on features:" + features + ",using distanceType:"+searchParams.getDistanceType()+"");

      searchParams.setcNNFeatures(features);
      query=matchedImageBasedOnCNNFeatures(searchParams);





     }
     else{
       //已有图搜索
       if(searchParams.getImageId()!=null){
         //compare features distance
         if(searchParams.getSearchPolicy()==null||searchParams.getSearchPolicy().equals("vec")){
           int pageNumber=1;
           int pageSize=searchParams.getMatchedTopNum();

           query= new EsCommonQuery(pageNumber,pageSize, Lists.newArrayList(new SortItem("_score","asc")), Lists.newArrayList("imageId"), indexName, typeName);
           query.setClusterIdentity(searchParams.getClusterIndentity());
           Map<String,Object> scriptParams= Maps.newHashMap();
           String cNNFeatures=null;
           //get cNNFeatures from index by imageId, also get groupId
           String[] results=getCNNFeaturesByImageId(searchParams.getImageId(),searchParams);
           //groupId filter

          // Integer groupId=getGroupIdBasedOnCNNFeature(cNNFeatures);
           Integer groupId=Integer.parseInt(results[1]);
           cNNFeatures=results[0];
           List<String> groupIds=getGroupIdsBasedOnCNNFeatureUsingKdTree(cNNFeatures,searchParams.getGroupNum());

           SubQuery finalSubQuery=new SubQuery();
           if(groupIds!=null){

             QueryItem queryItem=new QueryItem("groupId",groupIds);
             finalSubQuery.setQueryItem(queryItem);
           }
           query.setSubQuery(finalSubQuery);

           scriptParams.put("vecStr",cNNFeatures);
           scriptParams.put("vecStrFieldName","cNNFeatures");
           scriptParams.put("distanceType",searchParams.getDistanceType());
           query.setScriptLangType("native");
           query.setScriptParams(scriptParams);
           query.setScript("vectors_distance");
         }



       }
       //以图搜图
       if(searchParams.getcNNFeatures()!=null){
         query = matchedImageBasedOnCNNFeatures(searchParams);

       }

     }
     List<Map<String,Object>> results=null;
     //EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);
     //features full text search
     if(searchParams.getSearchPolicy()!=null&&searchParams.getSearchPolicy().equals("text")){
       String cNNFeaturesText=getCNNFeaturesTextByImageId(searchParams.getImageId(),searchParams);
      results =matchCNNFeatures(cNNFeaturesText,searchParams.getMatchedTopNum());





     }
     else{
       results= queryService.processQuery(query);
     }


     //populate image url
     final List<String> imageIds=Lists.newArrayList();
     results.forEach(new Consumer<Map<String, Object>>() {
       @Override public void accept(Map<String, Object> stringObjectMap) {
         imageIds.add((String)stringObjectMap.get("imageId"));
       }
     });
     if(searchParams.getFields()==null||searchParams.getFields().contains("url")){
       Map<String,String> idUrlMap=getImageUrlInfo(imageIds);

       results.forEach(new Consumer<Map<String, Object>>() {
         @Override public void accept(Map<String, Object> stringObjectMap) {
           String url=idUrlMap.get(stringObjectMap.get("imageId"));
           if(url==null){
             url="";
           }
           stringObjectMap.put("url",url);
         }
       });
     }
   else {
      //results= results.parallelStream().map(map -> map.put("imageId",Integer.parseInt((String)map.get("imageId"))+600000000) ).;

       results.forEach(new Consumer<Map<String, Object>>() {
         @Override
         public void accept(Map<String, Object> map) {
           if(!indexName.contains("editorial")){
             // change imageId if creative image

             map.put("imageId",Integer.parseInt((String)map.get("imageId"))+600000000);
           }


         }
       });
     }


    


     finalResults.put("result",results);
     if(results.size()>0){

       double score=Double.parseDouble(String.valueOf(results.get(0).get("score")==null?0:results.get(0).get("score")));
       if(score<=searchParams.getScoreThresholdValue()){
         finalResults.put("sameImageId",results.get(0).get("imageId"));

       }

     }



    return finalResults;
  }

  private EsCommonQuery matchedImageBasedOnCNNFeatures(ImageSearchParams searchParams) throws Exception {
    EsCommonQuery query;
    int pageNumber=1;
    int pageSize=searchParams.getMatchedTopNum();
    String typeName="image";
    query= new EsCommonQuery(pageNumber,pageSize, Lists.newArrayList(new SortItem("_score","asc")), Lists.newArrayList("imageId"), searchParams.getImageIndexName(), typeName);
    SubQuery finalSubQuery=new SubQuery();

    query.setClusterIdentity(searchParams.getClusterIndentity());
    Map<String,Object> scriptParams= Maps.newHashMap();
    String cNNFeatures=searchParams.getcNNFeatures();


    //Integer groupId=getGroupIdBasedOnCNNFeatureUsingKdTree(cNNFeatures);
    List<String> groupIds=getGroupIdsBasedOnCNNFeatureUsingKdTree(cNNFeatures,searchParams.getGroupNum());
    if(groupIds!=null){

     QueryItem queryItem=new QueryItem("groupId",groupIds);
      finalSubQuery.setQueryItem(queryItem);
    }
    query.setSubQuery(finalSubQuery);

    scriptParams.put("vecStr",cNNFeatures);
    scriptParams.put("vecStrFieldName","cNNFeatures");
    scriptParams.put("distanceType",searchParams.getDistanceType());
    query.setScriptLangType("native");
    query.setScriptParams(scriptParams);
    query.setScript("vectors_distance");
    return query;
  }

  private List<Map<String,Object>> matchCNNFeatures(String cNNfeaturesText,int topMatchedNum){
    List<Map<String,Object>> results=Lists.newArrayList();
    TransportClient client=((EsQueryService)queryService).getEsClient("vcgImage");
    MatchQueryBuilder cNNFeaturesTextQuery=new MatchQueryBuilder("cNNFeaturesText",cNNfeaturesText);
    SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
    searchSourceBuilder.query(cNNFeaturesTextQuery);
    searchSourceBuilder.from(0);
    searchSourceBuilder.size(topMatchedNum);
    searchSourceBuilder.fetchSource("imageId",null);
    searchSourceBuilder.sort("_score", SortOrder.DESC);



    SearchResponse searchResponse=client.prepareSearch()
            .setSource(searchSourceBuilder.toString()).setIndices("vcg_image")
            .execute().actionGet();
    SearchHits searchHits=searchResponse.getHits();
    long totalCount=searchHits.getTotalHits();
    for (SearchHit hit : searchHits.getHits()) {
      Map<String, Object> values = hit.getSource();
      float score=hit.getScore();

      if(score>0f){
        values.put("score",score);
      }





      results.add(values);

    }


    return results;

  }
  private String getCNNFeaturesTextByImageId(String imageId, VcgImageSearchService.ImageSearchParams searchParams)throws Exception{
    String indexName="vcg_image";
    String typeName="image";
    String cNNFeaturesText=null;
    EsCommonQuery query=null;
    query= new EsCommonQuery(1,1,null, Lists.newArrayList("cNNFeaturesText"), indexName, typeName);
    query.setClusterIdentity(searchParams.getClusterIndentity());
    SubQuery subQuery=new SubQuery("AND",new QueryItem("imageId",Lists.newArrayList(imageId)));
    query.setSubQuery(subQuery);
    List<Map<String,Object>> results= queryService.processQuery(query);
    cNNFeaturesText=(String)results.get(0).get("cNNFeaturesText");
    //List<String> strs=Lists.newArrayList(cNNFeatures.split(","));
//
//    String cNNFeaturesText= org.apache.commons.lang.StringUtils.join(strs.parallelStream().map(va ->
//                    String.valueOf(Math.round(Double.parseDouble(va)*10000)/10000)
//            ).collect(Collectors.toList())," ");







    return cNNFeaturesText;
  }
  protected  final static KDTree<Integer> kdTree=new KDTree<Integer>(2048);
  private Integer getGroupIdBasedOnCNNFeatureUsingKdTree(String cNNFeature) throws Exception {

    Integer groupId=null;

    if(kdTree.size()==0){
      if(this.cacheService!=null){
        String imageCenterVectorKeyPrefix=settings.getProperty("image.centerVectorKeyPrefix");
        Integer clusterNum=Integer.parseInt(settings.getProperty("image.cluster.num"));
        for(int i=0;i<clusterNum;i++){
          String key=imageCenterVectorKeyPrefix+i;
          String vecStr=cacheService.get(key);
          double[] vec = Lists.newArrayList(vecStr.split(",")).parallelStream()
                  .mapToDouble(va -> Double.parseDouble(va)).toArray();
          kdTree.insert(vec,i);


        }

      }

    }

    if(kdTree.size()>0){
      Stopwatch stopwatch=Stopwatch.createStarted();




      stopwatch.stop();
      long timeSeconds=stopwatch.elapsed(TimeUnit.MILLISECONDS)/1000;
      double[] vec = Lists.newArrayList(cNNFeature.split(",")).parallelStream()
              .mapToDouble(va -> Double.parseDouble(va)).toArray();
      groupId=kdTree.nearest(vec);
      logger.info("spent "+timeSeconds+" s to get target vector's groupId: "+groupId+"");




    }




    return groupId;
  }

  private List<String> getGroupIdsBasedOnCNNFeatureUsingKdTree(String cNNFeature,int groupNum) throws Exception {

    if(groupNum>10){
      groupNum=10;
    }
    String imageCenterVectorKeyPrefix=settings.getProperty("image.centerVectorKeyPrefix");
    Integer clusterNum=Integer.parseInt(settings.getProperty("image.cluster.num"));

    List<String> groupIds=null;

    if(kdTree.size()==0){
      if(this.cacheService!=null){

        for(int i=0;i<clusterNum;i++){
          String key=imageCenterVectorKeyPrefix+i;
          String vecStr=cacheService.get(key);
          double[] vec = Lists.newArrayList(vecStr.split(",")).parallelStream()
                  .mapToDouble(va -> Double.parseDouble(va)).toArray();
          kdTree.insert(vec,i);


        }

      }

    }

    if(kdTree.size()>0){
      Stopwatch stopwatch=Stopwatch.createStarted();





      double[] vec = Lists.newArrayList(cNNFeature.split(",")).parallelStream()
              .mapToDouble(va -> Double.parseDouble(va)).toArray();
//      int targetGroupId=kdTree.nearest(vec);
//      String key=imageCenterVectorKeyPrefix+targetGroupId;
//      double[] targetGroupVec=Lists.newArrayList(cacheService.get(key).split(",")).parallelStream()
//              .mapToDouble(va -> Double.parseDouble(va)).toArray();

      //int topMatchedGroupIdNum= Integer.parseInt(settings.getProperty("topMatchedGroupIdNum"));
      groupIds=kdTree.nearest(vec,groupNum).stream().map(val -> String.valueOf(val)).collect(Collectors.toList());
      stopwatch.stop();
      long timeSeconds=stopwatch.elapsed(TimeUnit.MILLISECONDS)/1000;
      logger.info("spent "+timeSeconds+" s to get target vector's groupIds: "+groupIds.toString()+"");




    }




    return groupIds;
  }
  private Integer getGroupIdBasedOnCNNFeature(String cNNFeature) throws Exception {
    Integer groupId=null;
    if(imageCenterVectorToGroupMap.isEmpty()){
      if(this.cacheService!=null){
        String imageCenterVectorKeyPrefix=settings.getProperty("image.centerVectorKeyPrefix");
        Integer clusterNum=Integer.parseInt(settings.getProperty("image.cluster.num"));
        for(int i=0;i<clusterNum;i++){
          String key=imageCenterVectorKeyPrefix+i;
            imageCenterVectorToGroupMap.put(cacheService.get(key),i);


        }

      }

    }

    if(!imageCenterVectorToGroupMap.isEmpty()){
      Stopwatch stopwatch=Stopwatch.createStarted();


      String distanceMeasureName="euclidean";

      DistanceMeasure distanceMeasure= DistanceMeasureFactory.createDistanceMeasure(distanceMeasureName);
      Optional<String> vector=imageCenterVectorToGroupMap.keySet().parallelStream().max(
          new Comparator<String>() {
            @Override public int compare(String v1, String v2) {

              double[] v1s = Lists.newArrayList(v1.split(",")).parallelStream()
                  .mapToDouble(va -> Double.parseDouble(va)).toArray();
              double[] v2s = Lists.newArrayList(v2.split(",")).parallelStream()
                  .mapToDouble(va -> Double.parseDouble(va)).toArray();
              double[] vs = Lists.newArrayList(cNNFeature.split(",")).parallelStream()
                  .mapToDouble(va -> Double.parseDouble(va)).toArray();
              double v1Ditance = distanceMeasure.compute(v1s, vs);
              double v2Distance = distanceMeasure.compute(v2s, vs);
              double comparedValue =v2Distance-v1Ditance;
              if (comparedValue > 0) {
                return 1;

              }
              if (comparedValue == 0) {
                return 0;

              } else {
                return -1;
              }

            }
          });
      String v=vector.get();
      stopwatch.stop();
      long timeSeconds=stopwatch.elapsed(TimeUnit.MILLISECONDS)/1000;
      groupId=imageCenterVectorToGroupMap.get(v);
      logger.info("spent "+timeSeconds+" s to get target vector's groupId: "+groupId+"");




    }



    return groupId;

  }
  private String[] getCNNFeaturesByImageId(String imageId,VcgImageSearchService.ImageSearchParams searchParams) throws Exception{
    String[] results=new String[2];
    String indexName="vcg_image";
    String typeName="image";
    String cNNFeatures=null;
    EsCommonQuery query=null;
    query= new EsCommonQuery(1,1,null, Lists.newArrayList("cNNFeatures","groupId"), indexName, typeName);
    query.setClusterIdentity(searchParams.getClusterIndentity());
    SubQuery subQuery=new SubQuery("AND",new QueryItem("imageId",Lists.newArrayList(imageId)));
    query.setSubQuery(subQuery);
    List<Map<String,Object>> results_= queryService.processQuery(query);
    cNNFeatures=(String)results_.get(0).get("cNNFeatures");
    String groupId=String.valueOf(results_.get(0).get("groupId"));
    results[0]=cNNFeatures;
    results[1]=groupId;




    return results;
  }


  public  Map<String,String> getImageUrlInfo(List<String> imageIds) throws Exception{
    String imageStore=settings.getProperty("image.store");
    Map<String,String> results=Maps.newHashMap();



    if(imageStore.equalsIgnoreCase("aws")){
      String awsImagePrefix=settings.getProperty("image.aws.url.prefix");
      for (String imageId : imageIds) {
          results.put(imageId,awsImagePrefix+imageId+".jpg");
      }

      return results;

    }

    String ossImagePrefix=settings.getProperty("image.oss.url.prefix");





    Connection con = sql2o.open();
    String tableName = "res_image";
    StringBuffer clause=new StringBuffer();
    imageIds = imageIds.parallelStream().map(va -> String.valueOf(Integer.parseInt(va) + 600000000)).collect(Collectors.toList());

    clause.append("(").append(org.apache.commons.lang3.StringUtils.join(imageIds,",")).append(")");

    String sql="select oss_800,id from res_image where id in "+clause.toString();
    logger.debug(sql);
    List<Map<String, Object>>  re=con.createQuery(sql).executeAndFetchTable().asList();
    for (Map<String, Object> map:re){
      try {
        if (!map.containsKey("oss_800") || !map.containsKey("id")) {
          continue;
        }
        results.put(String.valueOf(((Long)map.get("id")).intValue()-600000000), ossImagePrefix+map.get("oss_800").toString());
      }catch (Exception e){
        logger.info(ExceptionUtil.getExceptionDetailsMessage(e));
        logger.debug(map.toString());
      }
    }
    con.close();









    return results;

  }

  public static void main(String[] args) throws Exception {

//    AbstractQueryService queryService=ServiceUtils.getQueryService();
//    String indexName="vcg_image";
//    String typeName="image";
//    int pageNumber=1;
//    int pageSize=20;
//
//
//    EsCommonQuery query = new EsCommonQuery(pageNumber,pageSize, Lists.newArrayList(new SortItem("_score","asc")), Lists.newArrayList("imageId"), indexName, typeName);
//    query.setClusterIdentity("vcgImage");
//    Map<String,Object> scriptParams= Maps.newHashMap();
//    scriptParams.put("vecStr","0.861843,0.0501282,0.0230174,0.31559,0.558148,0.0417566,0.637796,0.446353,0.472662,0.135962,0.471548,0.85809,0.0193137,0.240001,0.128319,0.201755,0.535372,0.444987,0.0511621,0.213289,0.0984105,0.219994,0.614889,0.211672,0.0612657,0.540796,0.417201,0.127961,0.244234,0.0916717,0.28961,0.934867,0.0417946,0.65682,0.35461,0.0698856,0.424101,1.60084,0.0182688,0.335812,0.0616422,0.0492882,0.284311,0.53107,0.205961,0.292776,0.109754,0.110599,0.207916,0.188774,0.0430436,0.266573,0.537092,0.140295,0.371834,0.536334,0.676308,0.0248954,0.202005,0.686543,0.279334,0.638491,0.209941,0.410552,0.334189,0.0549273,0.375369,0.761711,0.589243,0.534326,0.67209,0.0926709,0.323228,0.0962432,0.294089,0.732956,0.290485,0.09695,0.218067,0.66126,0.148426,0.59298,0.0881107,1.04143,0.366153,0.134849,0.163326,0.276957,0.0248152,0.62026,0.176253,0.16316,0.154999,0.201541,0.38255,0.122719,0.20805,0.148191,0.445558,0.354084,0.229149,0.0788706,0.0580411,0.132705,0.0841508,0.658266,0.138703,0.33631,0.463987,0.182546,0.190092,0.154758,0.614474,0.528978,0.185886,0.132943,0.439586,0.0875334,0.722615,0.26538,0.357563,0.269554,0.198738,0.10856,0.137122,0.119209,0.0350248,0.0698553,0.645356,1.04464,0.255259,0.356294,0.115938,0.0667978,0.256102,0.103661,0.135182,0.0610292,0.117542,0.150862,0.208234,0.225915,0.177547,0.0320673,0.150155,0.136368,0.908459,0.310878,1.22916,0.205629,0.231006,0.0700176,0.16757,0.325096,0.248977,0.165625,0.493694,0.520767,0.147612,0.0524576,0.12517,0.204964,0.233236,0.147224,0.157689,0.121878,0.512217,0.845068,0.0481387,0.437877,0.198804,0.0203959,0.22771,0.038473,0.362474,0.303299,0.0649974,0.0513411,0.0350082,1.05197,0.074113,0.373109,0.398111,0.440264,0.501537,0.0679793,0.232452,0.0376081,0.16437,0.494192,0.106269,0.0981541,0.162181,1.92792,0.240024,0.0635985,0.570898,0.701385,0.897399,0.388786,0.15901,0.27633,0.203855,0.498236,0.342644,0.240201,0.615279,0.083353,0.307046,0.0186124,0.157981,0.713938,0.577188,0.291883,0.202194,0.294848,0.0321173,0.057595,0.146658,0.465431,0.0898372,0.260716,0.0918634,0.160371,0.0636766,0.0620283,0.174294,0.0442967,0.0287053,0.433117,0.266925,0.187819,0.0661387,0.00260632,0.260706,0.614594,0.511405,0.179117,1.23721,0.335435,0.240325,0.072114,0.332432,0.037538,0.0173772,0.947874,0.0455779,0.251131,0.241049,0.546342,0.0346696,0.754636,0.087601,0.0696682,0.0736652,0.366744,0.269038,1.27249,0.344063,0.20725,0.157128,0.121047,0.244244,0.0501962,0.245233,0.145654,0.215267,1.15546,0.319715,0.863149,0.0467396,0.336889,0.111816,0.484105,0.196179,0.191793,0.0224085,0.289267,0.298838,0.129276,0.305224,0.592475,0.0203181,0.426377,0.296165,0.336953,0.37354,0.698579,0.000444365,0.277611,0.149839,1.17364,0.621194,0.564129,0.914185,0.0406093,0.0822219,0.167231,0.300274,0.239803,0.119242,0.0550857,0.580699,0.250629,0.358478,0.599477,0.465264,0.140708,1.03918,0.18904,0.590196,0.297003,0.508224,0.400373,0.275888,0.253425,0.189664,0.850377,0.16064,0.164051,0.0195422,0.27047,0.148996,0.119008,0.250346,0.104827,0.0903693,0.456469,1.09099,0.146909,0.277234,0.479029,0.0926264,0.0797222,0.192115,0.0315991,0.0657777,0.133162,0.133087,0.800331,0.149347,0.264113,0.457037,0.370219,0.156316,0.148371,0.00915788,0.0434563,0.343581,0.0627355,0.0,0.0532762,0.000567827,0.307643,0.132681,0.399607,0.248792,0.128613,0.0839064,0.0298439,0.200051,0.0425103,0.172486,0.258587,0.00741151,0.478377,0.0807283,0.00419916,0.669115,0.151147,0.0511875,0.224653,1.00936,0.698649,0.280344,0.096869,0.0202173,0.0966826,0.129996,0.356186,0.00383457,0.0835369,0.0829106,0.0807602,0.656205,0.1538,0.290755,0.229427,0.0778942,0.236201,0.601161,0.0912586,0.0808153,0.00842899,0.409894,0.119344,0.00615711,0.13719,0.57751,0.202726,0.23801,0.293527,0.0988587,0.0227649,1.82718,0.140755,0.166445,0.121065,0.0393455,1.09076,0.190263,0.460598,0.287859,0.0465999,0.00179315,0.248625,0.0380212,0.527862,0.313888,0.356635,0.0147742,0.0909691,0.216688,0.0753357,0.349956,0.150559,0.383517,0.504156,0.173725,0.629268,1.49552,0.408884,0.127286,1.78153,0.15049,0.223664,0.135469,0.0841807,0.250202,0.466168,0.695787,0.28331,0.0288069,0.466295,0.414595,0.0833918,0.260575,0.043414,0.00344406,0.362787,0.257956,0.06807,0.0,0.099581,0.104883,0.597747,0.0440549,0.0215428,0.0435281,0.259364,0.291364,0.0891494,0.108819,0.245768,0.0173913,0.493106,1.12165,0.513659,0.00424476,0.109551,0.197266,0.310222,0.0845168,0.400799,0.00886541,0.436917,0.520804,0.384109,0.162636,0.0185012,0.0899322,0.118867,0.197853,0.603194,0.724054,0.0821571,0.134723,0.400094,0.136689,0.211021,0.1511,0.0388782,0.643719,0.624287,0.0114652,0.0792996,0.0670145,0.0518445,0.439725,0.45412,0.32072,0.00810981,0.0,0.0862362,0.0347002,0.0224417,0.101766,0.0978056,0.416422,0.166492,0.438583,0.0737133,0.40871,0.0585079,1.0222,0.0728112,0.655843,0.119086,0.0140639,0.433324,0.515162,0.152407,0.0295921,0.0525142,0.0344537,0.393147,1.11366,0.480551,0.437489,0.682912,0.0442979,0.153798,0.408866,0.398952,0.86378,0.157156,0.160088,0.0281205,0.835306,0.085393,0.0440454,0.0545139,0.08787,0.552933,0.0337791,0.202676,0.498711,0.0,0.314156,0.0306395,0.114876,0.00918351,0.116563,0.0,0.0323929,0.341958,0.564898,0.0712178,0.100398,0.0195548,0.631547,0.0690953,0.181357,0.0565345,0.109497,0.0829184,0.101071,0.0736613,0.43658,0.358096,0.32745,0.695488,0.0,0.0732443,0.000776905,0.106563,0.197546,0.0730742,0.07089,0.00461666,0.372728,0.147616,0.469429,0.0454069,0.0464038,0.0244109,0.283705,0.18872,0.20321,0.0127856,0.520395,0.992843,0.396914,0.340854,0.165194,0.21878,0.117159,0.274478,0.204666,1.04219,0.122598,0.184348,0.219057,0.0964675,0.15715,0.0671877,0.0414682,0.776448,0.119788,0.132673,1.06103,0.207779,0.345571,0.380136,0.149841,0.0424857,0.180486,0.114623,0.0810409,0.175595,0.754689,0.166241,0.0631303,0.190511,0.414263,0.0721654,0.010289,0.287574,0.372808,0.0449067,0.0208763,0.570646,0.228549,0.345388,0.334518,0.248554,0.454203,0.036933,0.666819,0.113586,0.349118,0.153644,0.0700395,0.0967214,0.0850754,0.544364,0.561201,0.791569,0.0919192,0.567954,0.0131393,0.353571,0.284656,0.324372,0.159235,0.837198,0.173367,0.258648,0.337289,0.104574,0.23278,0.0731031,0.763415,0.0727554,0.165971,0.109662,0.132809,0.133003,0.225954,0.110829,0.403416,0.0619063,0.315582,0.287645,0.00816472,0.223801,0.131001,0.560897,0.0640643,0.25043,0.49938,0.521842,0.244907,0.181238,0.490653,0.217214,0.206691,0.189827,0.36278,0.00583233,0.3664,0.183069,0.190387,0.0333805,0.240895,0.300413,0.0861283,0.448009,0.243255,0.216186,0.1087,0.0,0.289999,0.200678,0.355311,0.0874617,0.0808086,0.732551,0.447893,0.0698828,0.243532,0.12861,0.124051,0.0652226,0.269726,0.690894,0.479015,0.11729,0.22148,0.570973,0.155796,0.702798,0.53627,0.0303928,0.216904,0.365066,0.156804,0.158279,0.0601924,0.0759884,0.121574,0.0124972,0.0187282,0.205156,0.589386,0.106848,0.242341,0.0408586,0.0327856,0.929686,0.0465119,0.0160817,0.183361,0.261739,0.11284,0.023346,0.392056,0.99362,0.110195,0.420348,0.767142,0.186452,0.195138,0.123665,0.017481,1.26388,0.130497,0.969499,0.418642,0.0285535,0.107276,0.405191,0.119903,0.19208,0.170543,0.506377,0.0488438,0.118665,0.307964,0.111036,0.177486,0.197038,0.194326,0.19039,0.627503,0.243991,0.105353,0.164255,0.24384,0.131601,0.00207234,0.632479,0.118112,0.0707782,0.769044,0.161501,0.220632,0.451899,0.135584,0.460145,0.0104895,0.144687,0.13921,0.272446,0.0188087,0.244189,0.14447,0.0784059,0.901379,0.426701,0.239073,0.263257,0.173019,0.00351111,0.341669,0.0103029,0.039922,0.0332896,0.0365881,0.757747,0.593828,0.249879,0.239464,0.0466266,0.183548,0.936787,0.298977,0.409031,0.0502625,0.242517,0.435314,0.0111307,0.155371,0.181375,0.00812501,0.00519807,0.0378168,0.0606751,0.166477,0.0718771,0.0,0.441292,0.290913,0.517687,0.0107151,0.0694397,0.117065,0.259432,0.0619462,0.595334,0.138308,1.12117,0.369461,0.0910477,0.122586,0.10867,0.307155,0.0213028,0.265184,0.0202185,0.108954,0.420592,0.15639,0.0729353,0.0732908,0.0218484,0.259674,0.749529,0.198606,0.267813,0.80521,0.32608,0.185139,0.314861,0.855983,0.0399037,0.313474,0.359713,0.0868539,0.040681,0.0530569,0.00575001,0.662431,0.427812,0.147491,2.73182,0.0523843,0.185319,0.085546,0.88024,1.02968,0.452345,0.856753,0.0306139,0.0765528,0.387526,0.302095,0.460707,0.0683896,0.0252364,0.560286,0.331781,0.486379,0.686491,0.0304952,0.031314,0.23148,0.00815178,0.257795,0.054411,0.22641,0.00556228,0.450346,0.0899192,0.584373,0.236811,0.0413712,0.319073,0.38014,0.295718,0.0813803,0.0,0.441939,0.153785,0.129935,0.0126553,0.401013,0.0340427,0.133465,0.113312,0.175682,1.13499,0.479352,0.173949,0.120617,0.610257,0.0143954,0.273135,0.208283,1.19681,0.327807,0.1553,0.0652794,0.0852714,0.150119,0.445918,0.553874,0.204981,0.145809,0.142136,0.0467182,0.598817,0.0661492,0.723139,0.465833,1.35256,0.0148071,0.166148,0.272546,0.129987,0.0607309,0.14902,0.533141,0.151902,0.0560134,0.280695,0.14685,0.101367,0.31958,0.0,0.0425109,0.188501,0.00744705,0.429364,0.142087,0.177702,0.315317,0.752405,0.0230576,1.25458,0.088145,0.00582392,0.0220635,0.630046,0.0,0.0054088,0.082036,0.219769,0.181848,0.0349358,0.219492,0.250994,0.00477231,0.100815,0.324504,0.0626776,0.106424,0.255412,0.229825,0.0340534,0.284011,0.0175004,0.553133,0.370397,0.622346,0.12387,0.318752,1.1106,0.622149,0.0767506,0.350828,0.134283,0.0461969,0.726876,0.025505,0.26742,0.237873,0.587235,0.0577057,0.292072,0.348618,0.620779,0.529294,0.198319,0.250234,0.487623,0.22797,0.0886454,0.309581,0.0365414,0.870164,0.00962456,0.266095,0.771832,0.00877133,0.270381,0.325401,0.104715,0.112138,0.0795194,0.157587,0.146083,0.0230738,0.0858953,0.0470928,0.413008,0.217055,0.584726,0.435957,0.179868,0.125358,0.137694,0.0579815,0.20849,0.0,0.0912116,0.794176,0.000443026,0.230064,0.0091674,0.101275,0.0526757,0.347306,0.292082,0.168683,0.192107,0.0464251,0.65214,0.0070409,0.665093,0.410962,0.669709,0.215132,0.0424193,0.0481222,0.192763,0.255853,0.085383,0.395547,0.0480107,0.820178,0.392333,0.223742,0.0994508,0.283563,0.785437,0.094358,0.200191,0.496807,0.134411,0.0558574,0.4004,0.147565,0.214188,0.081853,0.0366044,0.096055,0.330646,0.0173362,0.0195012,0.871327,0.0444163,0.26864,0.215199,0.294027,0.0635858,0.0304067,0.274979,0.0724675,0.186106,0.535537,0.759492,0.213319,0.0269567,0.0999243,0.568767,0.0510194,0.301683,0.0710024,0.0609781,0.267088,0.160466,0.0256883,0.0904385,0.0142063,0.0209425,0.98845,0.143424,0.00838517,0.661733,0.0663206,0.428895,0.000888645,0.64434,0.472604,0.0598567,0.0301714,0.589934,0.943264,0.0151968,0.4787,0.353814,0.00586439,0.82827,0.0885664,0.663514,0.415518,1.20265,0.163404,0.45805,0.412268,0.182605,0.0215042,0.00578528,0.5009,0.0612807,1.30189,0.545806,0.0766621,0.168772,1.6741,0.112647,0.569972,0.120602,0.00222769,0.214515,0.163518,0.38153,0.0955237,0.213137,0.0122413,0.395394,0.302651,0.000595636,0.0334973,0.447358,0.251721,0.119761,0.206176,0.219668,0.504993,0.100455,0.146152,0.182714,0.354489,0.116491,0.0488144,0.0550081,0.487103,0.0543008,0.0505531,0.0817404,0.134573,0.000141509,0.198256,0.475194,0.217915,0.118133,0.891946,0.55779,0.383949,0.271754,0.241522,0.103765,0.099008,0.149522,0.0130156,0.191193,0.43877,0.0105666,0.0345046,0.139867,0.163536,0.140535,0.0783743,0.257699,0.345197,0.0216252,0.156585,0.00227001,0.160573,0.380694,0.274662,0.18816,0.0970464,0.153571,0.00504079,0.0,0.0696989,0.0,0.0850758,0.0652285,0.0119478,0.892628,0.334017,0.265019,0.122637,0.161171,0.248768,0.345672,0.955895,0.165762,0.0827044,0.12986,0.0,0.186512,0.477345,0.977099,0.0173384,0.118468,0.341272,0.0220664,0.924096,0.0717433,1.19823,0.149086,0.0110595,0.187892,0.0234665,0.014896,0.159373,0.446643,0.451572,0.0,0.473445,0.0171196,0.607536,0.205065,0.923026,0.0548245,0.212986,0.0813769,0.00131174,0.00975893,0.123269,0.278998,0.594969,0.013892,0.0388543,0.426752,0.194207,0.0037245,0.00537933,0.0779553,0.0831459,0.166899,0.487382,0.0857498,0.23206,0.0,0.0,0.286138,0.0038415,0.561086,0.0602791,0.141732,0.107439,0.0111692,0.0152565,0.976055,0.0163229,0.137349,0.0357857,0.0590467,1.51492,1.35307,0.354566,0.198456,0.161018,0.0314327,0.0415836,0.0867626,0.004169,0.192699,0.104857,0.300042,0.16165,0.14852,0.127145,0.0126545,0.00360418,0.430964,0.109368,0.0103903,0.208973,0.410081,0.416089,0.787037,0.207058,0.269708,0.0971687,0.247436,0.242773,0.069713,0.00536976,0.241585,0.179565,0.348669,0.385511,0.0,0.329171,0.317094,0.0514509,0.0190129,0.0316661,0.0252094,0.000110571,0.00327871,0.13873,0.0317128,0.287645,0.737571,0.0232316,0.710432,0.154821,0.0640264,0.9101,1.13871,0.00205648,0.0868996,0.0313697,0.00880593,0.267912,0.145562,0.0971609,0.0,0.0645332,0.214821,0.140241,0.975166,0.0680651,0.247188,0.134091,0.00977614,0.0159842,0.0578313,0.18367,0.101353,0.165473,0.457619,0.0734545,0.0,0.0197543,0.814359,0.426656,0.456637,0.134745,0.362584,0.301048,0.00299192,0.796647,0.658207,0.0277642,0.00200175,0.0110937,0.239864,0.346976,0.0696288,0.580896,0.376127,0.069668,0.298126,0.372469,0.0425257,0.15211,0.0224789,1.07303,0.171203,0.133531,0.153162,1.2129,0.00916422,0.340807,0.078428,0.2105,0.0409836,0.0267533,0.167061,0.0528596,0.481103,0.247336,0.328192,0.13995,0.0101312,0.248854,0.0166473,0.209718,0.340464,0.0107668,0.0981251,0.0293406,0.600875,0.000731743,0.000267677,0.0789416,0.0817068,0.0828379,0.0465199,0.00492587,0.0455337,0.0587102,0.102356,0.0805575,0.238712,0.219504,0.104069,0.0141436,0.171391,0.0382045,0.0696383,0.277025,0.250788,0.0650033,0.00566632,0.335106,0.0244019,0.182974,0.200764,0.180639,0.132488,0.0213867,0.355564,0.0876477,0.122089,0.0393124,0.0547711,0.592029,0.987015,0.126356,0.0465371,0.340364,0.232095,0.047831,0.420168,0.183702,0.00161287,0.581753,0.123179,0.12236,0.029699,0.154429,0.239023,0.00525156,0.0075834,0.164663,0.00249824,0.454296,0.52052,0.189844,0.0325731,0.285312,0.28457,1.03508,0.0600518,0.0988823,0.219335,0.0419691,0.513277,0.411529,0.000813582,0.0896854,0.0975173,0.00780996,0.0260448,0.0153565,0.333854,0.157433,0.0627355,0.299182,0.363777,0.854511,0.115946,0.0801319,0.000728221,0.0,0.785357,0.294412,0.153652,0.0624538,0.0272848,0.451101,0.0939767,0.214081,0.0,0.179165,0.0521672,0.17429,0.946324,0.376361,0.121687,0.291531,0.352569,0.0367151,0.603677,0.22814,0.0160807,0.108091,0.171035,0.142893,0.728226,0.0321225,0.176815,0.0931184,0.618181,0.315931,0.342039,0.523892,0.134571,0.38452,0.205571,0.177634,0.114622,0.376634,0.0436747,0.016896,0.487929,0.0747499,0.0440968,0.203137,0.0613336,0.251323,0.0133133,0.0100805,0.326162,0.0790897,0.1053,0.163185,0.359183,0.0445745,1.05575,1.46786,0.0164489,0.138552,0.259282,0.348124,0.100845,0.339147,0.0155415,0.0512565,0.0,0.26145,0.303874,0.17051,0.0359929,0.0755882,0.914253,0.171884,0.0208493,0.239014,0.253142,0.0190452,0.404846,0.0186815,0.0970345,0.366716,0.036378,0.0157264,0.0304364,0.0388653,0.0106965,0.375038,0.102742,0.574927,0.0208198,0.139519,0.000116351,0.262938,0.840933,0.43111,0.54086,0.696321,0.0552777,0.0828537,1.01553,0.318745,0.471075,0.272125,1.05423,0.424651,0.415882,0.18809,0.0523717,0.0507489,0.00744074,0.0855738,0.909007,0.616776,0.222456,0.0109247,0.503103,0.950023,0.244357,0.232998,0.0326229,0.267664,0.124915,0.0341154,0.205206,0.0,0.0511637,0.0348988,0.0141182,0.949848,1.04884,0.0702153,0.0,0.185681,0.191764,0.390943,0.0367948,0.0142284,0.244562,0.0,0.243205,0.0700673,0.539804,0.00171192,0.0486175,0.0414978,0.302601,0.251197,0.445574,0.293579,0.0218821,0.0912192,0.0484378,0.704284,0.0213177,0.0579064,0.101728,0.0185363,0.0693984,0.0774859,0.17158,0.116515,0.0526779,0.374738,0.0335857,0.0751226,0.281393,0.323617,0.233404,0.35455,0.0553548,0.00225358,0.0135856,0.320087,0.0688837,0.73577,0.348223,0.026559,0.16854,0.305532,0.129711,0.191551,0.494288,0.152749,0.17007,0.0229432,0.126028,0.316633,0.138062,0.11975,0.650868,0.258182,0.0473371,0.413372,0.0871055,0.120039,0.157532,0.426892,0.573699,1.33781,0.675767,0.274262,0.425773,0.131376,0.214267,0.00996898,0.237064,0.0326754,0.0145936,0.00866503,0.14667,0.354724,0.965087,0.11551,0.357134,1.01369,0.513165,0.0981037,0.125182,0.0,0.271242,0.253176,0.0152873,0.167971,0.818634,0.0760188,0.0441304,0.132558,0.06864,0.446123,0.945548,0.470101,0.660483,0.0244234,0.417141,0.0652612,0.0416115,0.350414,0.242044,0.250127,0.492256,0.0179066,0.0104664,0.150088,0.198141,0.0198401,0.170652,0.232101,0.634964,0.107651,0.133114,0.120746,0.15442,0.169318,0.0119419,0.0426162,0.0816188,0.0302108,0.260727,0.279935,0.134443,0.0833903,0.570992,0.0511932,0.565901,0.011902,0.0954576,0.00554594,0.247183,0.168264,0.195907,0.11746,0.160072,0.0,0.266564,0.00821676,0.12804,0.0783124,0.102176,0.309916,0.281339,0.510647,0.063789,0.0478289,0.0123716,0.0116935,0.140164,0.564163,0.602055,0.60996,0.108333,0.450341,0.54326,0.200085,0.000667035,0.0313931,0.105941,0.139189,0.156675,0.325103,0.390431,0.779268,0.250218,0.124995,0.00193002,0.122617,0.0939613,0.0112978,0.384629,0.736708,0.0126581,0.00576401,0.0290696,1.20037,0.0704815,0.0921158,0.0667617,0.918206,0.26122,0.671303,0.364362,0.0404387,0.199112,0.111165,0.623896,0.339085,0.437823,0.0818201,0.0198185,0.683168,0.271808,0.134982,0.382943,0.532533,0.147456,0.185441,0.117984,0.0753702,0.683435,0.0818203,0.50386,0.0980933,0.0609991,0.0541919,0.0067916,0.207254,0.742208,0.476167,0.0296971,0.183901,0.0209756,0.084189,0.0316086,0.424155,0.487529,0.0593696,0.693591,0.937124,0.362211,0.486715,0.465134,0.990376,0.259284,0.257999,0.240411,0.0559834,0.888314,2.40695,0.325015,0.105802,0.738314,0.278566,0.0137285,0.621353,0.266326,0.718617,0.0,0.99903,0.344435,0.139979,0.0225327,0.485026,1.41865,0.952507,0.110045,0.0347131,0.450174,0.480665,0.63781,0.0924035,0.139308,0.907281,0.148627,0.318721,0.384145,0.712983,0.61028,0.0573636,0.133864,0.323526,0.398763,0.301255,0.502694,0.437948,0.00697376,0.351953,0.156321,0.637937,0.0557083,0.0414809,0.594993,0.881629,0.0295166,0.517637,1.45642,0.845053,0.239844,2.27403,0.413294,0.497596,0.646323,0.130307,0.572109,0.454436,0.360275,0.645887,0.347815,0.885878,0.159494,0.475505,0.0294909,0.0026852,0.0278713,0.0,0.192841,0.264421,0.00912669,0.57389,0.0231363,1.00142,0.285288,0.249549,0.526277,0.509396,0.663423,0.482217,0.295749,0.925037,0.535202,0.81671,0.521948,0.163123,0.372136,0.0672249,0.222851,0.196505,0.0367376,0.301359,0.215272,1.84721,0.292902,0.432811,0.107566,0.0853629,0.296602,0.346338,0.801722,0.0709226,0.136218,0.778572,0.00721008,0.628118,0.316298,0.421515,0.992053,0.632461,0.0121994,0.18369,0.491094,0.461473,0.224394,0.414086,0.466454,0.169498,0.618744,2.06684,0.440636,0.0350079,0.187685,0.454667,0.254863,0.380894,0.611919,0.861775,0.65573,1.64022,0.777457,0.027032,0.111335,0.454474,0.584962,0.232677,1.07336,0.601215,1.57765,0.383657,0.0896635,0.217359,0.206573,0.930014,0.540747,0.307042,0.543411,0.0342931,0.520635,0.0521758,0.391819,0.132459,0.0508753,1.27695,1.3732,0.490911,0.33439,0.433349,0.826633,0.320787,0.970241,0.553567,0.263438,0.0567404,0.024601,0.240542,1.50809,0.725856,0.0143649,0.0436913,0.340418,0.0346825,0.0519197,0.0718045,0.505376,0.195169,0.448014,0.834325,0.811811");
//    scriptParams.put("vecStrFieldName","cNNFeatures");
//    scriptParams.put("distanceType","chi2");
//    query.setScriptLangType("native");
//    query.setScriptParams(scriptParams);
//    query.setScript("vectors_distance");
//    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);
//
//    List<Map<String,Object>> results= queryService.processQuery(query);
//
//    results.forEach(new Consumer<Map<String, Object>>() {
//      @Override public void accept(Map<String, Object> stringObjectMap) {
//        System.out.print(stringObjectMap.get("imageId")+"\n");
//      }
//    });
    String cNNFeatures="0.411092340946,0.184973090887,0.0479358918965,0.226157471538,0.105257816613,0.58000600338,0.056710422039,0.388880491257,0.321774244308,0.120996385813,0.0781068876386,1.22586202621,0.10339230299,0.315655201674,0.19838860631,0.0193605981767,0.28875246644,0.398440152407,0.0114077478647,0.149867132306,0.352148920298,0.0813920497894,0.0885203331709,0.320672810078,0.0792003050447,0.0977839529514,0.136605665088,0.0568574555218,0.53524929285,0.0926573202014,0.131460160017,0.571841955185,0.0627830177546,0.120147570968,0.614603996277,0.449683427811,0.0717920809984,0.30298653245,0.182092055678,0.0443122498691,1.04512059689,0.153106570244,0.142154112458,0.447940737009,0.195604458451,0.564676642418,0.227103024721,0.153350114822,0.151711151004,0.151249319315,0.0781347006559,0.289929717779,0.0769174695015,0.0475876182318,0.141099393368,0.213679790497,0.486106753349,0.0557239130139,0.238115727901,0.137390688062,0.0657538920641,0.261540263891,0.0420635566115,0.0693618059158,0.431920558214,0.263491123915,0.725832760334,0.0665078535676,0.0874351859093,0.714683413506,0.36663839221,0.143779456615,0.344694942236,0.113625220954,0.640116333961,1.44899606705,0.123772680759,0.233456537127,0.185148566961,0.317484408617,0.137640863657,0.165169328451,0.287731826305,1.23854076862,0.085708424449,0.159788742661,0.0568791367114,0.427117347717,0.341116130352,0.215513989329,0.0298257227987,0.158096060157,0.470301985741,0.169659927487,0.385250777006,0.382124394178,0.349741518497,0.127399548888,0.0617998838425,0.0529323332012,0.0925726890564,0.0482667535543,0.666524529457,0.627067744732,0.353907346725,0.0528655871749,0.0839520543814,0.730880379677,0.215333431959,0.101709976792,0.0562614761293,0.202191844583,0.348951548338,0.155112490058,0.236252740026,0.333680868149,0.101886346936,0.0318293832242,0.152453839779,0.429635345936,0.105824738741,0.268408000469,0.161015421152,0.356181889772,0.130454853177,0.0,0.102001331747,0.103264831007,0.674504578114,0.889771521091,0.214327007532,0.384941935539,0.170487970114,0.252891093493,0.393260300159,0.196509510279,0.115963809192,0.13511300087,0.0664560124278,0.654925346375,0.302667468786,0.120082840323,0.267243534327,0.032629430294,0.324744105339,0.0690834373236,0.143209934235,0.343271493912,0.531186521053,0.13769428432,0.0988778248429,0.308266371489,0.0268045663834,0.272115111351,0.199490070343,0.511440992355,0.149375110865,0.198345705867,0.125698238611,0.221259877086,0.189987465739,0.107545949519,1.04834330082,0.579563260078,0.0590082034469,0.0516796521842,0.278452903032,0.0515702366829,0.0321797691286,0.180992290378,0.033473353833,0.0621763244271,0.103035226464,0.0570329390466,0.246216252446,0.265565812588,0.331144034863,0.2691591084,0.104851335287,0.712450385094,0.370279401541,0.2688190341,0.349020421505,0.300446987152,0.0389679558575,0.126640364528,0.488802373409,0.0126555552706,0.213849529624,0.411406517029,0.10480824858,0.178999587893,0.224041357636,0.912397921085,0.0664960071445,0.0801303312182,0.13654628396,0.13891762495,0.440665543079,0.161615028977,0.206506252289,0.0628731101751,0.0417194403708,0.369692802429,0.150119751692,0.338203758001,0.151456341147,0.0033405655995,0.163955822587,0.177093684673,0.3728967309,0.0509131848812,0.239204362035,0.266751050949,0.436722755432,0.164668917656,0.0551922507584,0.187954962254,0.124023541808,1.2858761549,0.0206004194915,0.734388530254,0.122621163726,0.316860586405,0.0192953776568,0.0864759907126,0.4960270226,0.109328344464,0.167706131935,0.266471475363,0.178829237819,0.077034920454,0.0357632897794,0.37447860837,1.15874302387,0.051484324038,0.0452124215662,0.257351636887,0.306491464376,0.308266490698,0.0428315624595,0.552961111069,0.233961626887,0.188091665506,0.157911926508,0.112730748951,0.0860766321421,0.0905723869801,0.0639257580042,0.22909784317,0.177200779319,0.361966729164,0.0973955765367,0.00895734503865,0.169007375836,0.400886952877,0.55940413475,0.123808264732,0.0330396927893,0.189722001553,0.0424426980317,0.723330914974,0.150316029787,0.0279839076102,0.4675809443,0.189977154136,0.260159939528,0.636115849018,0.104877702892,0.290977418423,0.116089768708,0.111660689116,0.602744281292,0.221976488829,0.214731767774,0.117839038372,0.245291694999,0.416701644659,0.280220121145,0.0303418524563,0.0445983707905,0.779061377048,0.254633456469,0.0518600046635,0.137057006359,0.163714021444,0.268510788679,0.123508505523,0.708466053009,0.53496837616,0.627757608891,0.308140456676,0.287264168262,0.0891787856817,0.583519995213,0.753531098366,1.06962227821,0.237882494926,0.0582797564566,0.11696331948,0.0104136895388,0.0606581829488,0.311206400394,0.154112264514,0.02169691585,0.787213981152,0.320760935545,0.435882627964,0.704244613647,0.199049249291,0.0661815330386,0.71303409338,0.312812656164,0.612841546535,0.441340118647,0.484794318676,0.151728823781,0.85081923008,0.286647945642,0.0745145380497,0.134253919125,0.58006978035,0.294410794973,0.000633769435808,0.522181272507,0.0889801010489,0.234731972218,0.104099430144,0.347966343164,0.18689006567,0.307154655457,0.131754204631,0.00322456145659,0.0488792732358,0.00556780770421,0.762181460857,0.121381208301,0.925737321377,0.124627798796,0.196217849851,0.00224508903921,0.0830453038216,0.149078667164,0.121215909719,0.0289284083992,0.0563610717654,0.0207266453654,0.222506552935,0.255098432302,0.0809712707996,0.263585358858,0.0141805084422,0.190268754959,0.426770061255,0.193071708083,0.473053872585,0.357370048761,0.463618755341,0.296091735363,0.103070504963,0.298112869263,0.165407046676,0.0776766613126,0.238989770412,0.167496219277,0.304996132851,0.169296309352,0.520213007927,0.0773102417588,0.307875275612,0.158597916365,0.238163456321,0.421515345573,1.15673422813,0.0198700278997,0.10912284255,0.0728192105889,0.283287137747,0.113372921944,1.22486758232,0.223923519254,0.0074752937071,0.301686376333,0.647783398628,0.215581566095,0.0559958815575,0.36934247613,0.340119928122,0.245669841766,0.731119155884,0.831399381161,0.0273932777345,0.125178933144,0.196130678058,0.216243460774,0.101414553821,0.0284143313766,0.462400943041,0.126388728619,0.360486626625,0.305588334799,0.225179150701,0.191391974688,0.293135493994,0.536215424538,0.0404750108719,0.0152463084087,1.16142857075,0.420562684536,0.259113818407,0.209899514914,0.0522711649537,0.000758931273594,0.115589983761,0.247628480196,0.0666972622275,0.0560992844403,0.0473041683435,0.295991361141,0.38520231843,0.0940370932221,0.988689482212,0.00114002777264,0.0693935155869,0.103413566947,0.0437498241663,0.236670687795,0.147172093391,0.0463280491531,0.100497305393,0.0351494029164,0.0373647361994,0.234891548753,0.286940872669,0.015473742038,0.0303091686219,0.119634583592,0.00569777935743,0.470521450043,0.132679134607,0.775135934353,0.113012097776,0.38502368331,0.735388278961,0.0412567481399,0.148896351457,0.0776386409998,0.0,0.0104722008109,0.660357773304,0.249074146152,0.123964458704,0.34580591321,0.103287644684,0.303146958351,0.0502882003784,0.0338819883764,0.429472565651,0.186594054103,0.451317667961,0.0984708145261,0.138582184911,0.0566073767841,0.559811234474,0.00199537770823,0.0646023675799,0.0424559377134,0.108991675079,0.227365717292,0.246381610632,0.689940214157,0.349295526743,0.192385330796,0.0839620828629,0.139431640506,0.365020483732,0.0348823331296,1.68195784092,0.283058941364,0.275974929333,0.515103697777,0.0581828467548,0.104310497642,0.827592790127,0.0,0.044793125242,0.227759420872,0.081308349967,0.215202793479,0.0322968661785,0.348407715559,0.110565170646,0.194517970085,0.0615732371807,0.60279738903,0.143982678652,0.737105131149,0.0350337848067,0.14106144011,0.128586485982,0.113640919328,0.0846180170774,0.0,0.239776000381,0.378778934479,0.533460617065,0.132225692272,0.077581897378,0.928374886513,0.23549747467,0.350926458836,0.0498130321503,0.0298525579274,0.0827222242951,0.120432510972,0.353491842747,0.00635329075158,0.0462777167559,0.151576712728,0.0563224554062,0.0820179283619,0.0702920556068,0.860844433308,0.227290183306,0.0477817282081,0.677055776119,0.134832844138,0.313102394342,0.267072826624,0.251768738031,0.087865523994,0.0121734505519,0.380690872669,0.174014419317,0.136286601424,0.677970290184,0.216021597385,0.152638882399,0.430311173201,0.184253901243,0.0412138402462,0.108592942357,0.325393229723,0.059130936861,0.295937389135,1.25704562664,0.485315799713,0.736382365227,0.152796536684,1.02839171886,0.0281686782837,0.193241611123,0.142112240195,0.414493083954,0.215346291661,0.685392260551,0.696203172207,0.767192006111,0.111527279019,0.718333005905,0.0786334723234,1.1425652504,0.213227018714,0.0754221454263,0.122591130435,0.152536451817,0.0385848581791,0.0523035191,0.0172117501497,0.0394288264215,0.260089278221,0.314705401659,0.142874717712,0.127287387848,0.48136562109,0.163605257869,0.28432688117,0.810966849327,0.0121425483376,0.0166828930378,0.0265704914927,0.0200031995773,0.407494723797,0.243860900402,0.425636678934,0.158581584692,0.052864599973,0.199318602681,0.0774902254343,0.454167574644,0.135457575321,0.453228980303,0.0823430269957,0.170610204339,0.0813410878181,0.0816026329994,0.0844225883484,0.000193593325093,0.419416487217,0.408911496401,0.0176703482866,0.288445979357,0.366058975458,0.0933564305305,1.10418522358,0.155546814203,0.111723557115,0.632572889328,0.500533461571,0.798562586308,0.0519099012017,0.0285740494728,0.228641152382,0.0947936177254,0.169851750135,0.0301998946816,0.0367737300694,0.226641044021,0.137045964599,0.0612915083766,0.0432766675949,0.20223583281,0.090595126152,0.134781450033,0.134652853012,0.000558270607144,0.0,0.172240138054,0.473129570484,0.703362643719,0.0193865485489,0.13378469646,0.175976753235,0.00499091204256,0.00801027007401,0.414233148098,0.0188825801015,0.743675231934,0.0322773382068,0.0490974113345,0.0998769402504,0.128670871258,0.0628969743848,0.607824087143,0.119685359299,0.272442042828,0.190165087581,0.344909280539,0.441631644964,0.5128890872,0.191693112254,0.37787130475,0.270194172859,0.256867229939,0.315023690462,0.654064893723,0.712990999222,0.0184362716973,0.267576396465,0.0572106763721,0.1321387887,0.176883712411,0.353596508503,0.373857527971,0.122478812933,0.290659099817,0.360376864672,0.310665726662,0.137613520026,0.371823191643,0.183056101203,0.137336969376,0.0658736824989,0.674031972885,0.0334713645279,0.328945785761,0.357900202274,0.210042238235,0.0259395129979,0.55641245842,0.0179873164743,0.404124498367,0.271973490715,0.15478245914,0.0294562913477,0.564807772636,1.24926257133,0.654806315899,0.2536046803,0.0983520746231,0.123238474131,0.0266931522638,0.438551515341,0.139726623893,0.134197056293,0.00726081756875,0.0685056895018,0.174635007977,0.0507995560765,0.232724443078,0.225999251008,0.305951654911,0.102083936334,0.0132337827235,0.252771764994,0.346846014261,0.0356430634856,0.136580228806,0.416005760431,0.240618482232,0.161495625973,0.089968174696,0.29462569952,0.563718497753,0.0792105644941,0.310974627733,0.179877027869,0.147628903389,0.162855833769,0.515105009079,0.0937328115106,0.145304977894,0.0694517195225,0.0217831823975,0.0968334376812,0.156141206622,0.0187789592892,0.0828941762447,0.179869115353,0.460334211588,0.10798522085,0.388751983643,0.0120778447017,0.323582857847,0.358103871346,0.279683768749,0.350344181061,0.607075691223,0.264246463776,0.214009433985,0.118665568531,0.163963362575,0.33712798357,0.0696459114552,0.844874083996,0.395688146353,1.00921201706,0.465766012669,0.109172426164,0.461634278297,0.402276575565,0.0531717538834,0.0416331216693,0.22511343658,0.143336027861,0.238840043545,0.134656906128,0.0063883648254,0.0554148703814,0.040684685111,0.382193565369,0.0995543599129,0.224587514997,0.0195209626108,0.0899007469416,0.209068998694,0.0590879656374,0.0318447090685,0.0529044494033,0.0847275033593,0.0406876131892,0.0795585587621,0.0613147951663,0.0518167428672,0.347181200981,0.0963935777545,0.409367769957,0.0967975482345,0.158097594976,0.118423685431,0.48113951087,0.357429653406,0.203799784184,0.0881094038486,0.224425032735,0.433233946562,0.0191221442074,0.104307539761,0.415552765131,0.188975006342,0.143311813474,0.0231502819806,0.0967324972153,0.18998979032,0.198823690414,0.25469198823,0.623825132847,0.0,0.877178490162,0.139157906175,0.0865738466382,0.100606642663,0.0406627953053,0.12777402997,0.123218968511,0.0513690263033,0.666239738464,0.437945485115,0.0905573517084,0.600621163845,0.56290769577,0.0193116255105,0.253385394812,0.0796077251434,0.0192488469183,0.189785897732,0.4363514781,0.293226182461,0.00892961677164,0.789668619633,0.557959616184,0.43358039856,0.0509036667645,0.0956363454461,0.928968727589,0.0916509106755,0.0949288159609,0.100305996835,0.0174567587674,0.0363272055984,0.200067386031,0.508688688278,0.379637241364,0.443894863129,1.28553128242,0.433391958475,0.294874340296,0.711281657219,0.131184011698,0.629455089569,0.366070240736,0.693885684013,0.0780161544681,0.119738921523,0.0168283320963,0.111638620496,0.373941451311,0.0661542117596,0.0805255025625,0.159606605768,0.290760457516,0.151363804936,0.252737939358,0.15921780467,0.550721108913,0.0322462394834,0.463324487209,0.0760000646114,0.0185533519834,0.425070881844,0.374031513929,0.0138895679265,0.25293970108,0.0302325170487,0.274413228035,0.00332245510072,0.281958520412,0.114744916558,0.136519297957,0.230420783162,0.125278115273,0.463193297386,0.0832024440169,0.700535655022,0.203078404069,0.82779109478,0.0271863173693,0.0204398557544,0.0288648158312,0.0230915416032,0.264798283577,0.35657081008,0.0879395529628,0.105751276016,0.0113405855373,0.544668078423,0.549177825451,0.201145797968,0.265217661858,0.124260447919,0.670488834381,0.0445160344243,0.143326193094,0.0265326183289,0.0134448632598,0.0686936676502,0.261444687843,0.14786978066,0.08486738801,0.212671756744,0.0303170289844,0.019590716809,0.0679427310824,0.0414092838764,0.426797986031,0.899086773396,0.146083042026,0.0251989234239,0.0927182734013,0.0490284375846,0.353098928928,0.0272696353495,0.291453003883,0.300009191036,0.189698010683,0.0213933847845,0.0390584170818,0.0789492353797,0.0942653343081,0.0715838596225,0.225374341011,0.717600941658,0.0542049892247,0.0573154948652,0.115512937307,0.212579771876,0.512666642666,0.0446503274143,0.0121789295226,0.687717080116,0.010054256767,0.0873043313622,0.224405780435,0.0727642476559,0.247822761536,0.228516086936,0.0193826667964,0.1192394346,0.0826841294765,0.135570496321,0.246407061815,0.797485053539,0.170515835285,0.41598379612,0.165881335735,1.09725451469,0.0188064295799,0.536348342896,0.448131710291,0.10950718075,0.056888718158,0.0539314523339,0.112479686737,0.0263474062085,0.141585886478,0.0856251493096,0.0034229552839,0.0792016685009,0.208625093102,0.0850261002779,0.416548788548,0.383899897337,0.0787531137466,0.0734122768044,0.149533435702,0.142393499613,0.197471052408,0.37424582243,0.0381344258785,0.820325672626,0.894009113312,0.0995853692293,0.47992643714,0.051755219698,0.0342250466347,0.286729216576,0.00492940377444,0.00219289725646,0.0157861541957,0.0282299183309,0.376778632402,0.0248689260334,0.00704360194504,0.634405434132,0.0148946987465,0.436564743519,0.0251271575689,0.291495263577,0.140871286392,0.0833081677556,0.013156699948,0.139177039266,0.0491846613586,0.248759582639,0.421666949987,0.0193250421435,0.0297446232289,0.0773384347558,0.0749640837312,0.492464542389,0.260286003351,0.393097430468,0.00806665513664,0.607271552086,0.0155853349715,0.0772188603878,0.150599658489,0.151137217879,0.12826141715,0.455529123545,0.201397761703,0.326740145683,1.22827517986,0.551864266396,0.0374547988176,0.12269859761,0.202842950821,0.0246400125325,0.613250374794,0.99633449316,0.581575512886,0.117138341069,0.264217466116,0.0467689447105,0.900134325027,0.1459312886,0.834459006786,0.391559660435,0.0354640334845,0.144272476435,0.246975615621,0.160190805793,0.0299275703728,0.0164640173316,0.32420116663,0.134974092245,0.165017530322,0.0581032969058,0.0573465898633,0.115065261722,0.188448771834,0.196107417345,0.392842054367,0.374921530485,0.119142025709,0.551883399487,0.405770868063,0.203254669905,0.0599783807993,0.0,0.162788033485,0.529924452305,0.100737936795,0.153251051903,0.0626949295402,0.0894106253982,0.195070654154,0.0903308466077,0.34491148591,0.430485159159,0.219781532884,0.209418863058,0.016872772947,0.135245516896,0.431376308203,0.241972714663,0.229882270098,0.0711712390184,0.301977545023,0.419499367476,0.232698202133,0.253847509623,0.0784494876862,0.0725812762976,0.28644233942,0.254956096411,0.423093497753,0.160153478384,0.233709171414,0.290701538324,0.0186840929091,0.219521865249,0.0989364907146,0.0694404840469,0.065369553864,0.328578591347,0.0696443617344,0.755766689777,0.0247038044035,0.0770813971758,0.0703474506736,0.49642470479,0.475195407867,0.146288216114,0.0036561973393,0.0585082024336,0.272775769234,0.0444696843624,0.047295704484,0.0304785836488,0.0154944779351,0.0963934138417,0.280848681927,0.101832047105,0.000983470585197,0.862520933151,0.0189769789577,0.0674206241965,0.0114772357047,0.0102217243984,0.0511490032077,0.132695421576,0.0360818766057,0.0,0.0549798086286,0.257277011871,0.0516210980713,0.0167090706527,0.118158638477,0.0686710104346,0.0,0.0864644423127,0.0,0.557976305485,0.652177453041,0.161732777953,0.0481548123062,0.0,0.543310582638,0.00175882410258,0.0482125356793,0.618639707565,0.365840673447,1.18869960308,0.0335333384573,0.323326051235,0.185000404716,1.10584580898,0.214980795979,0.83039355278,0.0991157889366,0.100087054074,0.036727655679,0.0758351311088,0.196264714003,0.000713487388566,0.465134859085,0.984953343868,1.34396219254,0.739081323147,0.239004075527,0.508505582809,1.19594359398,0.00987577717751,0.223793491721,0.00577574362978,0.0360020361841,0.959422945976,0.0978630781174,0.216961473227,0.291085213423,0.667252063751,0.116159379482,0.101375624537,0.790517926216,0.674962699413,0.000702111283317,0.339490801096,0.0683583319187,0.722391068935,0.023469498381,0.0146645978093,0.216947928071,0.0502713248134,0.275663018227,0.00593809969723,0.1261729002,0.149269118905,0.622074007988,0.0132887680084,0.10723323375,0.00343519262969,0.598415315151,0.0498477369547,0.308375358582,0.00431866711006,0.0365649536252,0.0724762827158,0.143807336688,0.0196084845811,0.0209473427385,0.0449477694929,0.0357345864177,0.811792492867,0.149462148547,0.00722499517724,0.0995739772916,0.0441562086344,0.285231620073,0.00375195662491,0.00406575808302,0.0808691680431,0.160487771034,0.308732122183,0.0867277234793,0.0155488839373,0.462906956673,0.0916612222791,0.04117115587,0.147726595402,0.308624535799,0.0260253269225,0.0175891108811,0.0621806830168,0.796728610992,0.115994907916,0.057935371995,0.10770586133,0.364701390266,0.0573973059654,0.532931208611,0.139561772346,0.00753222871572,0.148598656058,0.0964360162616,0.0332072712481,0.00104941288009,0.00418798578903,0.040113247931,0.0,0.394883424044,0.398399055004,0.00463825184852,0.615151524544,0.00547853205353,1.2450209856,0.258975028992,0.0381024517119,0.0253063291311,0.00896205194294,0.00302622001618,0.0138663779944,0.686778306961,0.0355009622872,0.0674643442035,0.046296376735,0.0412223376334,0.225729987025,0.0193768814206,0.0545152686536,0.25689291954,0.0334549099207,0.289072751999,0.302174955606,0.108417853713,0.775924742222,0.00358442426659,0.0737469494343,0.198517903686,0.0303434934467,0.0814324617386,0.00593625707552,0.0419782400131,0.656723737717,0.0289908014238,0.00168061791919,0.396283358335,0.0283576026559,0.0134169897065,0.0120910992846,0.193839430809,0.0377576202154,0.0403867736459,0.197562620044,0.123631633818,0.0109991924837,0.00813973322511,0.254904478788,0.0283639077097,0.00213097920641,0.0104531878605,0.837579727173,0.0134580787271,0.0520817227662,0.0039493246004,0.0575936734676,0.54173463583,0.00699443183839,0.038067497313,0.605379223824,0.0,0.162131696939,0.0,0.00264187576249,1.31514656544,0.335712641478,0.190933719277,0.00968535616994,0.01176295802,0.0342306569219,0.351164638996,0.0811961367726,0.0167575217783,0.0171839110553,0.176996558905,0.038630027324,0.340494692326,0.0,0.0971744284034,0.0112735945731,0.176237598062,0.155792191625,0.0380094498396,0.595915138721,0.00352073926479,0.0159380156547,0.109592676163,0.727560400963,0.587370336056,0.474819093943,0.00242560449988,0.00568071380258,0.23364508152,0.339549392462,0.190095886588,0.0890498459339,0.0117959938943,0.0234033241868,0.000719191972166,0.0816267058253,0.113139055669,0.0532584190369,0.0513373911381,0.0156888794154,0.0387393459678,0.0106431012973,0.0476059578359,0.203109949827,0.038016833365,0.0441768802702,0.642023861408,0.212870180607,0.0118486452848,0.00799508672208,0.155746743083,0.00725902803242,0.0974295735359,0.00269123958424,0.0,0.110829859972,0.0120974965394,0.00389869860373,0.0895373299718,0.935928940773,0.0994545519352,0.269388198853,0.630682051182,0.000545315444469,0.0499898940325,0.0146080916747,0.268827468157,0.0622619837523,0.218295887113,0.0333047918975,0.0619244612753,0.126970201731,0.0879521220922,0.0436752103269,0.334438174963,0.694551944733,0.0067029632628,0.267680257559,0.00577199738473,0.602901637554,0.0180501136929,0.0225892774761,0.0988598018885,0.0246036089957,0.0367559120059,0.0187041163445,1.16216397285,0.0779021531343,0.0948516279459,0.0442389845848,0.71961915493,0.00177509500645,0.170329034328,0.210488855839,0.770814597607,0.0407094620168,0.00829571951181,0.46628922224,0.466830432415,0.05624871701,0.092265971005,0.016511913389,0.360685646534,0.0571593493223,0.00206915079616,0.00394970038906,0.249132484198,0.0146855674684,0.0105189392343,0.00846741627902,0.0250597037375,0.0386135540903,0.282041192055,0.0156811624765,0.143516302109,0.0842106938362,0.223716527224,0.000467305770144,0.395111382008,0.019979448989,0.112680569291,0.00435704365373,0.0124308383092,0.083665214479,0.227514147758,0.216149821877,0.00115729216486,0.455272167921,0.130055934191,0.0127803748474,0.081982344389,0.0378664433956,0.783441066742,0.0382231287658,0.0,0.00289689702913,0.234370991588,0.0424025543034,0.0132269477472,0.000504817347974,0.0501698330045,0.0,0.0126745225862,0.016334053129,0.651952326298,0.0439143590629,0.0242177508771,0.0168780758977,0.0111071541905,0.0389923006296,0.0334292501211,0.0770481228828,0.0022202488035,0.123051501811,1.05052018166,0.104200877249,0.0436934717,0.217067569494,0.0049856910482,0.0,0.00479577854276,0.0322927460074,0.132659748197,0.0099990349263,0.622718095779,0.272536039352,0.332822591066,0.00219918042421,0.0226702354848,0.0,0.0850346386433,0.160342544317,0.387047320604,0.0138592552394,0.0395643152297,0.0638105422258,0.0196611732244,0.0610881671309,0.002212125808,0.1532907933,0.085152797401,0.24440485239,0.787578940392,0.326308995485,0.00545544177294,0.0,0.0909949541092,0.314984440804,0.0799992904067,0.00641878275201,0.00249843206257,0.0,0.73724848032,0.449348300695,0.0716172903776,0.00329358666204,0.168120741844,0.00038134586066,0.490669935942,0.912902235985,0.0422755181789,0.051135700196,0.451882362366,0.00171367684379,0.587629020214,0.336098700762,0.52617418766,0.110419511795,0.00141733605415,0.448212325573,0.536383807659,0.0113832131028,0.132884651423,1.35382866859,0.166563123465,0.00169625645503,0.130625277758,0.0257781483233,0.097158908844,0.495476067066,0.260237723589,0.0314247794449,0.139843165874,0.343794673681,0.00405086018145,0.30072721839,0.0545860379934,0.659790754318,0.0803818553686,0.0375682860613,0.0677357688546,0.110953085124,0.0252120774239,0.0231028683484,0.0978343561292,0.887349247932,0.293226718903,0.322879076004,0.00295312376693,0.0371373742819,0.361296117306,0.814877748489,0.136809900403,0.185797929764,0.420261859894,0.660775005817,0.0247032102197,0.310161948204,0.525621056557,0.0292688161135,0.0918537527323,0.11317243427,0.169436067343,1.10702311993,0.0587038211524,0.333958089352,0.612435042858,0.0393942408264,0.114631086588,0.027774007991,0.247713133693,0.0,0.0361240245402,0.0197185631841,0.0192002430558,0.854899048805,0.597332119942,0.26470169425,0.0252889897674,0.0972670316696,0.000880174804479,0.0698245018721,0.0147896157578,0.0423156842589,0.0726180225611,0.0642533227801,0.130394428968,0.261300712824,0.147076040506,0.137189373374,0.00612993352115,0.408934235573,0.379396766424,0.0344037376344,0.0416709184647,0.438744485378,0.0,0.374189585447,0.00956999976188,1.32161128521,0.0557634793222,0.133781194687,0.00298627559096,0.0809903666377,0.394600361586,0.119135059416,0.256975531578,0.108033560216,0.0843575149775,0.0071180309169,0.0351005680859,1.46885240078,0.00711281085387,0.52862393856,0.0279441121966,0.547908484936,0.940910935402,0.0496466271579,0.395802170038,0.688525140285,0.0997196137905,0.0265255048871,0.124921187758,0.0902678892016,1.04186868668,0.0604294762015,0.0477306693792,0.0212711654603,0.356651246548,0.213600412011,0.0155855016783,1.09170162678,0.579066693783,0.264642804861,0.0313640721142,0.0,0.048506628722,0.0284692533314,0.055553983897,0.0,0.600157916546,0.187893912196,0.0128779727966,0.204080268741,0.593655347824,2.23370194435,0.38813239336,0.700075030327,0.0196641981602,0.0481576062739,0.018623188138,0.264799118042,0.26554453373,0.263002693653,0.0830674096942,0.0266387220472,0.0785994455218,0.565079689026,0.00316098378971,0.39966109395,0.0,0.0120810745284,0.0,0.016890630126,0.137140840292,1.30362510681,0.182201877236,0.0470458976924,0.230604127049,0.00456230575219,0.273890137672,0.102742306888,0.0147469853982,0.0971837714314,0.00791829731315,0.237574741244,0.305908918381,0.0402055084705,0.0530829876661,0.194601401687,0.00328482314944,0.00448990659788,0.0998358651996,0.0,0.00316459685564,1.08253252506,0.69427138567,0.0885876938701,0.0535135045648,0.0368514209986,0.812693357468,0.00815312750638,0.0,0.0,0.876908123493,0.0445905812085,0.063009954989,0.00142137310468,0.920707404613,0.0466325804591,0.192146316171,0.0201824661344,0.025326166302,0.00764616206288,0.474765628576,0.231299623847,0.564079165459,0.308105021715,0.0,0.803703546524,0.0664616152644,0.0314855836332,0.122029490769,0.0,0.332436770201,0.0303847789764,0.429258555174,0.0,0.551066100597,0.0353132411838,0.0249421354383,0.0268917381763,0.275468319654,0.720529854298,0.0461869053543,0.118118710816,0.0120259672403,0.163348183036,0.00325796054676,0.0411032177508,0.000239494722337,0.0592385977507,0.903646171093,0.00545776588842,0.00842357613146,0.763462126255,0.0257119126618,0.524002492428,0.000955321826041,0.437167853117,0.260804653168,0.0176752377301,0.0419666096568,0.233842685819,0.650950372219,0.536416649818,0.00597296096385,0.0114752538502,0.543854415417,0.709751486778,0.624042212963,0.0532879307866,0.0736822932959,0.303730279207,0.564356863499,0.000412854831666,0.568433463573,0.149164959788,0.0242778826505,0.460752546787,0.134086802602,0.154189765453,0.192752912641,0.195197343826,0.00313336215913,0.0229408871382,0.0260357204825,0.22637668252,0.426114112139,0.0427384413779,0.48738515377,0.160382866859,0.0787891000509,4.28494531661e-05,0.0894981026649,0.0261732116342,0.0211299471557,0.582932174206,1.1620978117,0.0,0.101718433201,0.676489830017,1.07525491714,0.0079173091799,0.0112729873508,0.291120648384,0.0318385623395,0.00210968032479,0.415445774794,0.484818339348,0.00437775813043,0.036342356354,0.729166865349,1.42593991756,0.000725054880604,0.0974607244134,0.117060527205,0.119849726558,0.123626902699,1.18173336983,0.353527069092,0.000182463089004,0.899170279503,0.0691073983908,0.0375591702759,0.58871614933,0.567847549915,1.02393770218,0.0654677227139,0.230800658464,0.192339807749,0.188393771648,0.00702806189656,0.984226942062,0.00174264027737,0.0091594606638,0.60396951437,0.0374210327864,0.109405703843,0.0927359983325,0.400156825781,0.0602541342378,0.0596412941813,0.0965519696474,0.236410528421,0.0513746738434,0.0238600876182,0.510525286198,0.100851327181,0.022401008755,0.0112535785884,0.0144059462473,2.43849134445,0.0478345155716,0.47062420845,0.679274737835,0.410869240761,0.0157686769962,0.879860043526,1.07159376144,0.116898924112,0.071393802762,0.165851697326,0.362527340651,0.0341002158821,0.192965611815,0.10642836988,0.0985770225525,0.0215260703117,0.100366301835,0.844595909119,0.0455779917538,0.789621472359,0.0,0.0834364369512,0.0865138545632,0.19904537499,0.119200460613,0.00239022099413,0.103171259165,0.0836632400751,0.87653452158,0.0100031383336,0.0488698221743,0.444840401411,0.227536246181,0.0071535804309,0.0707742273808,0.00228515686467,0.241826638579,0.316433221102,0.00189020903781,0.147257998586,0.296655267477,0.932275593281,0.196106746793,0.597144246101,0.562287449837,0.0632174760103,0.199032068253,0.152641281486,0.0251573789865,1.08055245876,0.42417961359,0.54651927948,0.383674263954,0.122700937092,0.331725418568,0.254960626364,0.0176651850343,0.485295802355,0.299192488194,0.340689867735,0.298688560724,0.371532618999,0.0309660062194,0.39555516839,0.280081093311,0.853437185287,0.0159048158675,0.396189689636,0.50550287962,0.184924945235,0.17342081666,0.180988639593,0.739905953407,0.638553261757,0.113167189062,1.3752771616,0.0552474707365,0.0547881945968,0.0922383069992,0.651813626289,0.125791355968,0.192565202713,0.246634200215,0.170940548182,0.127030268312,0.990434527397,0.0931499004364,0.567219436169,0.00069547345629,0.0763292312622,0.00911231152713,0.311778068542,0.567961990833,1.04377567768,0.139542937279,0.0242711901665,0.0128261707723,0.394104778767,0.894312620163,1.18452274799,0.135379552841,0.295439213514,0.0512124300003,0.405069380999,0.259468108416,0.392340034246,0.855827450752,0.12152902782,0.208237215877,0.305239439011,0.113366439939,0.0792571082711,0.0176324862987,0.156807631254,0.540347576141,0.585717499256,0.336952149868,0.340296566486,0.523036241531,0.487098604441,0.155867695808,0.103336147964,0.0331286564469,0.455969274044,1.06979465485,0.485164761543,1.00510776043,0.767115831375,0.198272481561,0.165205851197,0.230244383216,0.269677966833,0.0678645670414,0.72020304203,0.344334602356,0.0496092997491,2.21789669991,0.0471943244338,0.583732426167,0.012508444488,0.676252067089,0.253692597151,0.216634497046,0.258942753077,0.223488420248,0.194200783968,0.728172600269,0.492932766676,0.145574614406,0.107679374516,0.598984360695,1.00750494003,0.21211476624,0.492010265589,0.835527479649,0.71244597435,0.0312937647104,0.291234016418,0.289014041424,0.31597790122,0.0230240263045,0.114483110607,0.38316360116,0.106412440538,0.0608528368175,0.0446281917393,0.111690551043,0.538385689259,0.106126248837,0.52626144886,0.319511562586,0.729744851589,0.804696619511,0.282092958689,0.196917340159,1.06439936161,0.374320298433,0.866612792015,0.0210627038032,0.402274787426,0.309354960918,0.394912928343,0.131301105022,0.124345995486,0.0833431556821,0.477919489145,0.017019925639,0.330525815487,0.226233914495,0.164853766561,0.885432839394,0.135407134891,0.0168876722455,0.15427325666,0.930745065212,0.33024379611,0.125934079289,0.281184583902,0.136243715882,0.00176680088043,1.70071697235,0.369281291962,0.622054934502,0.110069997609,0.435296535492,0.642758309841,0.431211233139,0.430840730667,0.908533275127,0.299230456352,0.025114344433,0.0779901593924,0.382291346788,0.165208891034,0.818376898766,0.0166023112833,0.252397805452,0.535134851933,0.00379054783843,0.226461872458,0.114886932075,0.580870985985,0.386102765799,1.54226791859,0.0980105027556,0.32457870245,0.407946318388,1.16022491455,0.0725749582052,0.223467230797,0.244797214866";
     ImageSearchParams imageSearchParams=new ImageSearchParams();

     // imageSearchParams.setImageId("208869627");
    imageSearchParams.setcNNFeatures(cNNFeatures);
    imageSearchParams.setMatchedTopNum(10);
    imageSearchParams.setDistanceType("euclidean");
//     imageSearchParams.setPageNum(1);
//     imageSearchParams.setFetchSize(100);
    //imageSearchParams.setSearchPolicy("text");
     String searchParams=GsonHelper.objToJson(imageSearchParams);
     System.out.print(searchParams+"\n");
     ImageSearchParams imageSearchParams1=(ImageSearchParams)GsonHelper.jsonToObj(searchParams,ImageSearchParams.class);
     VcgImageSearchService vcgImageSearchService= ServiceUtils.getVcgImageSearchService();
     //int groupId=vcgImageSearchService.getGroupIdBasedOnCNNFeature(cNNFeatures);
   Map<String,Object> finalResults= vcgImageSearchService.search(searchParams);
System.out.print(finalResults.get("result").toString()+"\n");






  }

}
