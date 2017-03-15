package com.comm.sr.service.vcg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.DateTimeUtil;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.common.utils.HttpUtils;
import com.comm.sr.service.SearchServiceFactory;
import com.comm.sr.service.score.ScriptService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.JsonPath;
import com.yufei.utils.CommonUtil;
import com.yufei.utils.ExceptionUtil;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by jasstion on 16/02/2017.
 */
public class VcgOnlineMockService extends VcgBasedSearchService {
  protected KeywordService keywordService=null;
  protected AbstractQueryService queryService=null;
  protected ScriptService scriptService=new ScriptService();
  static  Map<String,String>  kwWordsMap=null;
  static {
    try {
      kwWordsMap = (Map<String,String>)CommonUtil.deSerializeObj("/data/vcg/kwWordMap.obj");
    }catch (Exception e){

    }

  }


  public VcgOnlineMockService(Properties settings,KeywordService keywordService,AbstractQueryService queryService) {

    super(settings);
    this.keywordService=keywordService;
    this.queryService=queryService;

  }
  private String searchUrlRequest(SearchParams searchParams){

    String searchServiceUrl=settings.getProperty("vcgSearchServiceUrl");
    String response=null;
    //'{"phrase":"sky","onlineState":1,"debug":"true"}'
    String phrase=searchParams.getQueryText();

    int currentPageNum=searchParams.getPageNum();
   int fetchSize=100;
    if(currentPageNum%3==1||currentPageNum==1){
      fetchSize=300;

    }
    Map<String,Object> requestParames= Maps.newHashMap();
    requestParames.put("phrase",searchParams.getQueryText());
    requestParames.put("onlineState",String.valueOf(1));
    requestParames.put("debug",String.valueOf(true));
    requestParames.put("perpage",String.valueOf(fetchSize));
    requestParames.put("page",String.valueOf(currentPageNum));
    requestParames.put("sort","best_adv");
    requestParames.put("fields",Lists.newArrayList("brandId","collectionId","resId","uploadTime","licenseType"));

    response= HttpUtils.executeWithHttpPost(searchServiceUrl, requestParames);
    logger.info(response);

    return response;

  }

  @Override public Map<String, Object> search(String searchParamsStr) throws Exception {

    //<String,String> brandsMap=(Map<String,String>)CommonUtil.deSerializeObj(Thread.currentThread().getContextClassLoader().getResourceAsStream("brandsMap.obj"));
    //Map<String,String> brandTypeMap=(Map<String,String>)CommonUtil.deSerializeObj(Thread.currentThread().getContextClassLoader().getResourceAsStream("brandTypeMap.obj"));
    Map<String,String> brandsMap=(Map<String,String>)CommonUtil.deSerializeObj("/data/vcg/brandsMap.obj");
    Map<String,String> brandTypeMap=(Map<String,String>)CommonUtil.deSerializeObj("/data/vcg/brandTypeMap.obj");
    SearchParams searchParams=(SearchParams) GsonHelper
        .jsonToObj(searchParamsStr, SearchParams.class);

    String response=searchUrlRequest(searchParams);
    //parse kwids

   final  List<String> kwIds= Lists.newArrayList();
    JSONObject topObject= JSON.parseObject(response);
    kwIds.addAll(JsonPath.read(response, "$.debug.keywordDetail.kids"));

    List<Map<String, Object>> images =  JsonPath.parse(response).read("$.datas[*]");
   final  List<String> imageIds=Lists.newArrayList();
    images.forEach(new Consumer<Map<String, Object>>() {
      @Override public void accept(Map<String, Object> stringObjectMap) {
        imageIds.add(stringObjectMap.get("id").toString());
      }
    });


    Map<String,List<String>> imagesStatMap=getBatchImagesStatistics(imageIds,kwIds);
    //images=getImagesDetailInfoAgain(imageIds);
    Map<String,Map<String,Object>> imagesInfoFromEs=getImagesDetailInfoAgain(imageIds);
    for (Map<String, Object> image : images) {
      image.putAll(imagesInfoFromEs.get(image.get("id")));
    }
    Map<String, String> imageUrlInfos=getImageUrlInfo(imageIds);

    int currentPageNum=searchParams.getPageNum();
    //1，4，7...n+3顶部显示:以下300个结果中，GI品牌占xx个，本土品牌占xx个
    //exInfo:"以下300个结果中，GI品牌占xx个，本土品牌占xx个"
    Map<String,Object> finalResults= Maps.newHashMap();

    List<Map<String,String>> results= Lists.newArrayList();
    StringBuffer exInfStrBuffer=new StringBuffer();
    //brandid->brandName






    exInfStrBuffer.append(" 以下300个结果中，");//关键词权重计算公司：Math.log(clickNum*fc+downloadNum*fd+favoriteNum*ff+3),

    if(currentPageNum%3==1||currentPageNum==1){
      Map<String, Long> counting= images.stream().map(
          (Map<String, Object> val) -> new Pair<String, Integer>(
              brandTypeMap.get(val.get("brandId"))==null?"null":brandTypeMap.get(val.get("brandId")), 1))
          .collect(Collectors.groupingBy(Pair::getKey, Collectors.counting()));
      counting.forEach((key,val) ->exInfStrBuffer.append(key+"品牌占").append(counting.get(key)).append("; ")



      );

      images=images.subList(0,100);







    }
    //populate exinfo for images
      images.forEach(new Consumer<Map<String, Object>>() {
      @Override public void accept(Map<String, Object> valMap) {
        //id,brandName,brandId,imageType(RM/RF),uploadTime,score,downloadNum,favoriteNum,clickedNum,brandType(gi/local)
        try {
          Map<String, String> imageMap = Maps.newHashMap();
          imageMap.put("id", valMap.get("id").toString());
          imageMap.put("brandName", brandsMap.get(valMap.get("brandId")));
          //1 rm,  2 rf
          Object licenseType=valMap.get("licenseType");
          String licenseTypeStr="";


          if(licenseType!=null){
            if(licenseType.toString().equals("1")){
              licenseTypeStr="rm";

            }
            if(licenseType.toString().equals("2")){
              licenseTypeStr="rf";

            }



          }
          imageMap.put("imageType",licenseTypeStr );
          Integer uploadTime=Integer.parseInt(valMap.get("uploadTime").toString());
          String timeStr= DateTimeUtil.getFormatDate(DateTimeUtil.getDateFromTimeMillis((long)uploadTime*1000),DateTimeUtil.TIME_FORMAT_CN);

          imageMap.put("uploadTime", timeStr);
          imageMap.put("brandType", brandTypeMap.get(valMap.get("brandId")));
          imageMap.put("brandId", valMap.get("brandId")!=null?valMap.get("brandId").toString():"");
          //所有关键词的下载次数等
         // List<String> imageStatistics=Lists.newArrayList();
          //imageStatistics=getImageStatistics(valMap.get("id").toString(),kwIds);

          List<String> imageStatistics=imagesStatMap.get(valMap.get("id").toString());

          double score=0d;
           List<String> imageStatistics_=Lists.newArrayList();
          if(imageStatistics!=null){
            for(String s:imageStatistics){

              String[] array=s.split(":");
              //kwId:downloadNum:favoriteNum:clickNum
              int downloadNum=Integer.parseInt(array[1]);
              int favoriteNum=Integer.parseInt(array[2]);
              int clickNum=Integer.parseInt(array[3]);
              String computeScript =
                  "Math.log(clickNum*fc+downloadNum*fd+favoriteNum*ff+3)";// +1/((Math.log(viewedNum+1)
              Map<String, Object> params_ = Maps.newHashMap();
              Map values_ = Maps.newLinkedHashMap();

              params_.put("clickNum", clickNum);
              params_.put("downloadNum", downloadNum);
              params_.put("favoriteNum", favoriteNum);
              params_.put("fc", 1);
              params_.put("fd", 10);
              params_.put("ff", 3);
              double payload = 0d;
              try {
                payload = (Double) scriptService.eval(computeScript, params_);
                if(payload<0){
                  payload=0d;
                }
              } catch (Exception e) {
                logger.info(
                    "error to compute payload, message:" + ExceptionUtil.getExceptionDetailsMessage(e) + "");

              }
              s+=":"+Precision.round(payload,2);
              imageStatistics_.add(s);
              score+=payload;

            }
          }




          imageMap.put("imageKwIdStatistics", StringUtils.join(imageStatistics_,"@"));


          imageMap.put("score", String.valueOf(Precision.round(score,2)));
          String imageUrl = imageDomain + imageUrlInfos.get(valMap.get("id"));
          imageMap.put("url",imageUrl);
          results.add(imageMap);
        }catch (Exception e){
          logger.info("error to populate imageMap,"+ ExceptionUtil.getExceptionDetailsMessage(e)+"");

        }





      }
    });




    logger.debug(results.toString());
    finalResults.put("result",results);
    finalResults.put("exInfo",exInfStrBuffer.toString());





    return finalResults;
  }
  final  String imageDomain="http://bj-feiyuantu.oss-cn-beijing.aliyuncs.com/";
  public  Map<String,String> getImageUrlInfo(List<String> imageIds) throws Exception{
   Map<String,String> results=Maps.newHashMap();
    String mysqlUrl=settings.getProperty("mysqlUrl");
    String mysqlUserName=settings.getProperty("mysqlUserName");
    String mysqlPasswd=settings.getProperty("mysqlPasswd");



    final Sql2o sql2o =
        new Sql2o(mysqlUrl,mysqlUserName,mysqlPasswd);
    Connection con = sql2o.open();
    String tableName = "resource";
    StringBuffer clause=new StringBuffer();
    clause.append("(").append(org.apache.commons.lang3.StringUtils.join(imageIds,",")).append(")");

    String sql="select oss_id5,id from resource where id in "+clause.toString();
    logger.debug(sql);
    List<Map<String, Object>>  re=con.createQuery(sql).executeAndFetchTable().asList();
    for (Map<String, Object> map:re){
      try {
        if (!map.containsKey("oss_id5") || !map.containsKey("id")) {
          continue;
        }
        results.put(map.get("id").toString(), map.get("oss_id5").toString());
      }catch (Exception e){
        logger.info(ExceptionUtil.getExceptionDetailsMessage(e));
        logger.debug(map.toString());
      }
    }
    con.close();








    return results;

  }

  private Map<String,List<String>> getBatchImagesStatistics(List<String> imageIds,List<String> kwIds){

    Map<String,List<String>> finalMap=Maps.newHashMap();
    String indexName="kwid_image_sum_statistics-lc";
    String typeName="statistics";
    EsCommonQuery query = new EsCommonQuery(1,1000, null, Lists.newArrayList("imageId","kwId","downloadNum","viewedNum","clickNum","favoriteNum"), indexName, typeName);
    List<SubQuery> subQueries=Lists.newArrayList();
    subQueries.add(new SubQuery(null,new QueryItem("imageId",imageIds),null));
    subQueries.add(new SubQuery(null,new QueryItem("kwId",kwIds),null));
    //

    SubQuery finalQuery=new SubQuery("AND",null,subQueries);
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgstatistics");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    System.out.print(esQueryWrapper.getSearchSourceBuilder().toString() + "\n");
    try {
      List<Map<String, Object>> result=queryService.query(query);
      for (Map<String, Object> valMap:result){
        String imageId=(String)valMap.get("imageId");
        String str=kwWordsMap.get(valMap.get("kwId").toString())+":"+valMap.get("downloadNum").toString()+":"+valMap.get("favoriteNum").toString() +":"+valMap.get("clickNum").toString();

        if(finalMap.get(imageId)==null){
          List<String> kwIdsStr=Lists.newArrayList();

          kwIdsStr.add(str);
          finalMap.put(imageId,kwIdsStr);



        }
        else {
          List<String> kwIdsStr =finalMap.get(imageId);
          kwIdsStr.add(str);
          finalMap.put(imageId,kwIdsStr);

        }




      }








//      result.forEach(valMap -> statistics.add(kwWordsMap.get(valMap.get("kwId").toString())+":"+valMap.get("downloadNum").toString()+":"+valMap.get("favoriteNum").toString()
//              +":"+valMap.get("clickNum").toString()
//      ));
    } catch (Exception e) {
      logger.info("error to get iamge statistics info, error message:"+ExceptionUtil.getExceptionDetailsMessage(e)+"");
    }


    return finalMap;


  }
  //kwId:downloadNum:favoriteNum:clickNum
private List<String> getImageStatistics(String imageId,List<String> kwIds){
  List<String> statistics=Lists.newArrayList();

  String indexName="kwid_image_sum_statistics-lc";
  String typeName="statistics";
  EsCommonQuery query = new EsCommonQuery(1,100, null, Lists.newArrayList("imageId","kwId","downloadNum","viewedNum","clickNum","favoriteNum"), indexName, typeName);
  List<SubQuery> subQueries=Lists.newArrayList();
  subQueries.add(new SubQuery(null,new QueryItem("imageId",Lists.newArrayList(imageId)),null));
  subQueries.add(new SubQuery(null,new QueryItem("kwId",kwIds),null));
  //

  SubQuery finalQuery=new SubQuery("AND",null,subQueries);
  query.setSubQuery(finalQuery);
  query.setClusterIdentity("vcgstatistics");
  EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

  System.out.print(esQueryWrapper.getSearchSourceBuilder().toString() + "\n");

  try {
    List<Map<String, Object>> result=queryService.query(query);

    result.forEach(valMap -> statistics.add(kwWordsMap.get(valMap.get("kwId").toString())+":"+valMap.get("downloadNum").toString()+":"+valMap.get("favoriteNum").toString()
    +":"+valMap.get("clickNum").toString()
    ));
  } catch (Exception e) {
    logger.info("error to get iamge statistics info, error message:"+ExceptionUtil.getExceptionDetailsMessage(e)+"");
  }

  return statistics;

}

  private Map<String,Map<String,Object>> getImagesDetailInfoAgain(List<String> imageIds){
    final Map<String,Map<String,Object>> images=Maps.newHashMap();

    String indexName="vcg_creative";
    String typeName = "vcgcsdn";
    EsCommonQuery query = new EsCommonQuery(1,500, null, Lists.newArrayList("id","brandId","licenseType","uploadTime"), indexName, typeName);
    List<SubQuery> subQueries=Lists.newArrayList();
    subQueries.add(new SubQuery(null,new QueryItem("id",imageIds),null));
    //

    SubQuery finalQuery=new SubQuery("AND",null,subQueries);
    query.setSubQuery(finalQuery);
    query.setClusterIdentity("vcgTest");
    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);

    System.out.print(esQueryWrapper.getSearchSourceBuilder().toString() + "\n");

    try {
      List<Map<String,Object>> re=queryService.query(query);
      re.forEach(new Consumer<Map<String, Object>>() {
        @Override public void accept(Map<String, Object> var) {
         images.put(var.get("id").toString(),var);


        }
      });

    } catch (Exception e) {
      logger.info("error to get iamge statistics info, error message:"+ExceptionUtil.getExceptionDetailsMessage(e)+"");
    }

    return images;

  }
  public static void main(String[] args) throws Exception {
//    AbstractQueryService queryService= ServiceUtils.getQueryService();
//    String indexName="kwid_image_sum_statistics-lc";
//    String typeName="statistics";
//    EsCommonQuery query = new EsCommonQuery(1,100, null, Lists.newArrayList("imageId","kwId","downloadNum","viewedNum","clickNum","favoriteNum"), indexName, typeName);
//    List<SubQuery> subQueries=Lists.newArrayList();
//    String imageId="204037276";
//    List<String> kwIds=Lists.newArrayList("43308","3549");
//    subQueries.add(new SubQuery(null,new QueryItem("imageId",Lists.newArrayList("204037276")),null));
//    subQueries.add(new SubQuery(null,new QueryItem("kwId",kwIds),null));
//    //
//
//    SubQuery finalQuery=new SubQuery("AND",null,subQueries);
//    query.setSubQuery(finalQuery);
//    query.setClusterIdentity("vcgstatistics");
//    EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);
//
//   System.out.print(esQueryWrapperSearchSourceBuilder().toString() + "\n");
//
//      List<Map<String, Object>> result=queryService.query(query);


//    String serviceName="vcgOnlineMockService";
//    VcgBasedSearchService vcgBasedSearchService= SearchServiceFactory.vcgSearchServices.get(serviceName);
//    SearchParams searchParams=new SearchParams();
//    searchParams.setFetchSize(100);
//    searchParams.setPageNum(2);
//    searchParams.setQueryText("男人");
//    String searchParamsStr=GsonHelper.objToJson(searchParams);
//
//
//    Map<String,Object> map=vcgBasedSearchService.search("{\"serviceName\":\"vcgOnlineMockService\",\"queryText\":\"girl sea\",\"fetchSize\":\"100\",\"pageNum\":\"1\",\"ifUseSecondSortBasedDate\":false}");
//    System.out.print(map.get("exInfo")+"\n");
//    System.out.print(map.get("result")+"\n");


   //populate brandIdMap, brandTypeMap

   //4，5，6是gi的，
   // 其他的本地的

//   final  Map<String,String> brandsMap=Maps.newHashMap();
//    //brandId->brandType
//    final Map<String,String> brandTypeMap=Maps.newHashMap();
//    FileUtil.readLines("/data/vcg/brands.csv").forEach(new Consumer<String>() {
//      @Override public void accept(String s) {
//        if(s.isEmpty()||!s.contains("@")){
//          return;
//
//        }
//        brandsMap.put(s.split("@")[0],s.split("@")[1]);
//      }
//    });
//    FileUtil.readLines("/data/vcg/brandType.csv").forEach(new Consumer<String>() {
//      @Override public void accept(String s) {
//        //brandId brandType
//        String brandId=s.split(":")[0].trim().replace("\"","");
//        String brandType=s.split(":")[1].trim().replace("\"","").replace(",","");;
//        if("456".contains(brandType)){
//          brandTypeMap.put(brandId,"gi");
//
//
//        }
//        else{
//          brandTypeMap.put(brandId,"local");
//        }
//
//
//      }
//    });
//    CommonUtil.serializeObj(brandsMap,"/data/vcg/brandsMap.obj");
//    CommonUtil.serializeObj(brandTypeMap,"/data/vcg/brandTypeMap.obj");
//
//    final Map<String,String> kwWordMap=Maps.newHashMap();
//        FileUtil.readLines("/data/apps/dataTools/kwInfo.csv").forEach(new Consumer<String>() {
//          @Override public void accept(String s) {
//            if(s.isEmpty()||!s.contains("@")||s.split("@").length<3){
//              return;
//
//            }
//
//            kwWordMap.put(s.split("@")[0],s.split("@")[2]);
//          }
//        });
//    CommonUtil.serializeObj(kwWordMap,"/data/vcg/kwWordMap.obj");



//    Map<String,String> brandsMap=(Map<String,String>)CommonUtil.deSerializeObj("/data/vcg/brandsMap.obj");
//
//
//    //Maps.newHashMap();
//    //brandId->brandType
//    Map<String,String> brandTypeMap=(Map<String,String>)CommonUtil.deSerializeObj("/data/vcg/brandTypeMap.obj");
//
//    System.out.print(brandsMap.get("11987")+"\n");
//    //local
//    System.out.print(brandTypeMap.get("10181")+"\n");
//    //gi
//    System.out.print(brandTypeMap.get("10572")+"\n");



//    final ExecutorService executorService =
//        new ThreadPoolExecutor(10, 10, 30, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<Runnable>(1), new ThreadPoolExecutor.CallerRunsPolicy());
//
//
//
//        String serviceName="vcgOnlineMockService";
//      final  VcgOnlineMockService vcgBasedSearchService=
//            (VcgOnlineMockService) SearchServiceFactory.vcgSearchServices.get(serviceName);
//        List<String> ids= FileUtil.readLines("/data/vcg/ids_all_newab");
//        List<List<String>> idss=CommonUtil.splitCollection(ids,10000);
//
//    for (List<String> tmp : idss) {
//      executorService.submit(new Runnable() {
//        @Override public void run() {
//
//          try {
//            vcgBasedSearchService.getImageUrlInfo(tmp).forEach(new BiConsumer<String, String>() {
//              @Override public void accept(String s, String s2) {
//                String imageUrl = vcgBasedSearchService.imageDomain + s2;
//                try {
//                  URL url = new URL(imageUrl);
//                  FileUtils
//                      .copyURLToFile(url, new File("/data/mlib_data/images/vcg_creative/" + s + ".jpg"));
//                  //
//                  //            InputStream is = url.openStream();
//                  //            FileOutputStream os = new FileOutputStream("/data/mlib_data/images/vcg_creative/"+s+".jpg");
//                  //
//                  //            byte[] b = new byte[2048];
//                  //            int length;
//                  //
//                  //            while ((length = is.read(b)) != -1) {
//                  //              os.write(b, 0, length);
//                  //            }
//                  //
//                  //            is.close();
//                  //            os.close();
//                } catch (IOException e) {
//                  System.out.print(e.getMessage());
//                }
//
//              }
//            });
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
//
//        }
//      });
//
//
//
//
//
//
//    }
    String imageDomain="http://bj-feiyuantu.oss-cn-beijing.aliyuncs.com/";

    List<String> imageIds=Lists.newArrayList("200205080,201454132,200986697,202061007,201387604,200222965,201038461,200796559".split(","));
    String serviceName="vcgOnlineMockService";
    final  VcgOnlineMockService vcgBasedSearchService=
                (VcgOnlineMockService) SearchServiceFactory.vcgSearchServices.get(serviceName);
    vcgBasedSearchService.getImageUrlInfo(imageIds).forEach(new BiConsumer<String, String>() {
      @Override public void accept(String s, String s2) {
        System.out.print(s+";"+imageDomain+s2+"\n");

      }
    });







  }

}
