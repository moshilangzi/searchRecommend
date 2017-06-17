package com.comm.sr.web.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.comm.sr.common.entity.Image;
import com.comm.sr.common.entity.ThreadShardEntity;
import com.comm.sr.common.utils.Constants;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.SearchServiceFactory;
import com.comm.sr.service.ServiceUtils;
import com.comm.sr.service.cache.CacheService;
import com.comm.sr.service.topic.TopicService;
import com.comm.sr.service.vcg.VcgBasedSearchService;
import com.comm.sr.service.vcg.VcgImageSearchService;
import com.yufei.utils.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by jasstion on 15/6/30.
 */
@Controller
@RequestMapping(value = "/inner/srservice")
public class BHSRAction {


    private final static Logger mLog = LoggerFactory.getLogger(BHSRAction.class);

    @RequestMapping(value = "/recommend")
    public void recommend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get appkey, profile user id, visited user id
        //get recommend service using appley, then call service to return Map result
        //format map result to response
        int code = 200;
        String msg = "正常调用";
        Map data = null;
        String params = request.getParameter("params");
        try {
            if (params != null && !params.trim().isEmpty()) {
                Map<String, String> paraMap = JSON.parseObject(params, new TypeReference<Map<String, String>>() {
                });
                String appKey = paraMap.get("appKey");
//                IRecommend iRecommend = RecommendFactory.createRecommender(appKey);
//                data = iRecommend.recommend(paraMap);

            } else {
                code = -100;
                msg = "调用错误，请正确的传递参数.";
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            mLog.info("appKey对应的srevice调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);

    }

    @RequestMapping(value = "/clearAppRuleCache")
    public void clearAppRuleCache(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get appkey, profile user id, visited user id
        //get recommend service using appley, then call service to return Map result
        //format map result to response
        int code = 200;
        String msg = "正常调用";
        Map data = null;
        String params = request.getParameter("params");
        try {
            if (params != null && !params.trim().isEmpty()) {
                Map<String, String> paraMap = JSON.parseObject(params, new TypeReference<Map<String, String>>() {
                });
                String appKey = paraMap.get("appKey");
//                IRecommend iRecommend = RecommendFactory.createRecommender(appKey);
//                iRecommend.clearAppRuleCache(appKey);

            } else {
                code = -100;
                msg = "调用错误，请正确的传递参数.";
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            mLog.info("appKey对应的srevice调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);

    }
    @RequestMapping(value = "/search")
    public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        Constants.threadShardEntity.set(threadShardEntity_);

        int code = 200;
        String msg = "正常调用";
        Object data = null;
        String parameterErrorMsg = "参数传递错误！";
        String params = request.getParameter("params");
        String serviceName=request.getParameter("serviceName");
        if(serviceName==null||serviceName.isEmpty()){
            serviceName="vcgTestSearchService";
        }

       //params= URLDecoder.decode(params,"UTF-8");
        //String appKey=request.getParameter("appKey");
        try {
//            if(StringUtils.isEmpty(appKey)){
//                code=-100;
//                msg = "搜索服务调用失败，请传递搜索服务标识";
//
//
//
//            }

            if (StringUtils.isEmpty(params)) {
                code=-100;
                msg = "搜索服务调用失败，请传递搜索查询内容";
            }
            else{
                VcgBasedSearchService vcgBasedSearchService= SearchServiceFactory.vcgSearchServices.get(serviceName);
                data=vcgBasedSearchService.search(params);


            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            mLog.info("搜索服务调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);
        Constants.threadShardEntity.remove();
    }

    @RequestMapping(value = "/imageSearch")
    public void listImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        Constants.threadShardEntity.set(threadShardEntity_);

        int code = 200;
        String msg = "正常调用";
        Object data = null;
        String parameterErrorMsg = "参数传递错误！";
        String params = request.getParameter("params");



        try {

            if (StringUtils.isEmpty(params)) {
                code=-100;
                msg = "搜索服务调用失败，请传递搜索查询内容";
            }
            else{
                VcgImageSearchService vcgBasedSearchService= ServiceUtils.getVcgImageSearchService();
                data=vcgBasedSearchService.search(params);


            }
        } catch (Exception e) {
            code = -100;
            msg = ExceptionUtil.getExceptionDetailsMessage(e);
            mLog.info("搜索服务调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);
        Constants.threadShardEntity.remove();
    }


    @RequestMapping(value = "/addImagesByUrl", method = RequestMethod.GET)
    public void addImagesByUrl(@RequestParam("images") String newImagesJsonInfo ,HttpServletRequest request,HttpServletResponse response)
            throws IOException {
        //send image bytes to kafka also generate imageId, later get image features from redis by imageId,then search most similirity images from elastic
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        Constants.threadShardEntity.set(threadShardEntity_);
        TopicService topicService=ServiceUtils.getTopicService();
        final String topicName=request.getParameter("ImageUpdatedTopic");
        int code = 200;
        String msg = "正常调用";
        Object data = null;
        try {
            List<Image> images= JSON.parseObject(newImagesJsonInfo,new TypeReference<List<Image>>(){

            });

            images.parallelStream().forEach(new Consumer<Image>() {
                @Override
                public void accept(Image image) {
                    //push image stream to kafka

                    String imageUrl=image.getUrl();
                    String imageId=image.getImageId();
                    topicService.publishTopicMessage(topicName,imageId,imageUrl);



                }
            });




        }catch (Exception e){
            code=-100;
            msg= ExceptionUtil.getExceptionDetailsMessage(e);


        }
        printJsonTemplate(code, msg, data, request, response);
        Constants.threadShardEntity.remove();






    }



    @RequestMapping(value = "/matchImageBasedImageUrl", method = RequestMethod.GET)
    public void matchImageBasedImageUrl(@RequestParam(name="scoreThresholdValue",required = false,defaultValue = "10") String scoreThresholdValue,@RequestParam(name="groupNum",required = false,defaultValue = "1") String groupNum,@RequestParam(name="topNum",required = false,defaultValue = "10") String topNum,@RequestParam(name = "fields",required = false, defaultValue = "url") String fields,@RequestParam("distanceType") String distanceType, @RequestParam("imageUrl") String imageUrl, HttpServletRequest request,HttpServletResponse response)
            throws IOException {
        //send image bytes to kafka also generate imageId, later get image features from redis by imageId,then search most similirity images from elastic
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        Constants.threadShardEntity.set(threadShardEntity_);
        int code = 200;
        String msg = "正常调用";
        Object data = null;
        try {
            VcgImageSearchService.ImageSearchParams imageSearchParams=new VcgImageSearchService.ImageSearchParams();
            imageSearchParams.setDistanceType("euclidean");
            imageSearchParams.setFields(fields);
            imageSearchParams.setMatchedTopNum(Integer.parseInt(topNum));
            imageSearchParams.setMatchPictureUrl(imageUrl);
            imageSearchParams.setGroupNum(Integer.parseInt(groupNum));
            imageSearchParams.setScoreThresholdValue(Double.parseDouble(scoreThresholdValue));
            String indexName=request.getParameter("indexName");
            imageSearchParams.setImageIndexName(indexName);

            String params = GsonHelper.objToJson(imageSearchParams);


            VcgImageSearchService vcgBasedSearchService = ServiceUtils.getVcgImageSearchService();
            data = vcgBasedSearchService.search(params);
        }catch (Exception e){
            code=-100;
            msg= ExceptionUtil.getExceptionDetailsMessage(e);
            mLog.info("matchImageBasedImageUrl failure, error message:"+msg+"");


        }
        printJsonTemplate(code, msg, data, request, response);
        Constants.threadShardEntity.remove();






    }



    /**
     * Upload single file using Spring Controller
     */

    @RequestMapping(value = "/imageUploadAndSearch", method = RequestMethod.POST)
    public void imageUploadAndSearch(@RequestParam(name="topNum",required = false,defaultValue = "10") String topNum,@RequestParam(name = "fields",required = false, defaultValue = "url") String fields,@RequestParam("groupNum") String groupNum,@RequestParam("distanceType") String distanceType, @RequestParam("file") MultipartFile file, HttpServletRequest request,HttpServletResponse response)
        throws IOException {
                //send image bytes to kafka also generate imageId, later get image features from redis by imageId,then search most similirity images from elastic
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        Constants.threadShardEntity.set(threadShardEntity_);
        int code = 200;
        String msg = "正常调用";
                byte[] imageBytes=file.getBytes();
        Object data = null;
        try {
            String imageId = UUID.randomUUID().toString();
            TopicService topicBytesService = ServiceUtils.getByteTopicService();
            topicBytesService.publishTopicMessage("uploadedImageForSearch", imageId.getBytes(), imageBytes);
            Thread.currentThread().sleep(3*1000);
            CacheService<String, String> redisCacheService = ServiceUtils.getCacheService();
            String features = redisCacheService.get(imageId);
            if(features==null){
                throw new  RuntimeException("system error! detail message: image_upload topic not consumed correctly!");

            }
            mLog.debug("search images based on features:" + features + ",using distanceType:"+distanceType+"");

            VcgImageSearchService.ImageSearchParams imageSearchParams = new VcgImageSearchService.ImageSearchParams();
            imageSearchParams.setcNNFeatures(features);



            imageSearchParams.setFields(fields);
            imageSearchParams.setMatchedTopNum(Integer.parseInt(topNum));
            imageSearchParams.setGroupNum(Integer.parseInt(groupNum));
            imageSearchParams.setDistanceType(distanceType);

            String params = GsonHelper.objToJson(imageSearchParams);

            VcgImageSearchService vcgBasedSearchService = ServiceUtils.getVcgImageSearchService();
            data = vcgBasedSearchService.search(params);
        }catch (Exception e){
            code=-100;
            msg= ExceptionUtil.getExceptionDetailsMessage(e);
            mLog.info("imageUploadAndSearch failure, error message:"+msg+"");



        }
        printJsonTemplate(code, msg, data, request, response);
        Constants.threadShardEntity.remove();






            }
    private void printJsonTemplate(int code, String msg, Object data,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("traceID", request.getParameter("traceID"));
        result.put("code", code);
        result.put("msg", msg);
        ThreadShardEntity threadShardEntity=null;
        try {
             threadShardEntity=Constants.threadShardEntity.get();
        } catch (Exception e) {
            mLog.info("error to access threadShardEntity, message is: "+ ExceptionUtils.getMessage(e.getCause())+"");
        }
        String uuidStr =threadShardEntity.getSearchId();
        result.put("uuid",uuidStr);
        result.put("data", (data != null) ? data : new JSONObject());

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));
        out.flush();
        out.close();
    }


    public static void main(String[] args){

//        String params_="{\"cacheStrategy\":null,\"distance\":null,\"fls\":[\"userID\",\"score\",\"age\",\"height\",\"registeDate\"],\"functionQuerysList\":[],\"gender\":-1,\"locationPoint\":null,\"pageNum\":1,\"pageSize\":10,\"queryItems\":[{\"fieldName\":\"height\",\"isFilterType\":false,\"matchedValues\":[\"158#TO #159\",\"178#TO# 179\"]},{\"fieldName\":\"age\",\"isFilterType\":false,\"matchedValues\":[\"1988\",\"1999\"]},{\"fieldName\":\"gender\",\"isFilterType\":true,\"matchedValues\":[\"0\"]},{\"fieldName\":\"registeDate\",\"isFilterType\":true,\"matchedValues\":[\"2014-02-15T18:59:51Z#TO #2015-02-15T18:59:51Z\",\"2009-02-15T18:59:51Z#TO #2011-02-15T18:59:51Z\"]}],\"sortItems\":[{\"fieldName\":\"age\",\"sort\":\"desc\"},{\"fieldName\":\"height\",\"sort\":\"desc\"},{\"fieldName\":\"registeDate\",\"sort\":\"desc\"}]}";
//        final String userID = "130106652";
//        String url = "http://srservice1.comm.com/inner/searchRecommend/search.json";
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("params", params_);
//        params.put("APIKey", "1BJUTYXAQA6LS9796PZ7ET8P0X9KT1J1");
//
//        String result=HttpUtils.executeWithHttp(url,params);
//
//
//        System.out.print(result);
        String images="[{'imageId':12,'url':'http://sdf'},{'imageId':12,'url':'http://sdf'},{'imageId':12,'url':'http://sdf'}]";
       List<Image> images_= JSON.parseObject(images,new TypeReference<List<Image>>(){

        });

       images_.forEach(va -> System.out.println(va.toString()));

    }
}
