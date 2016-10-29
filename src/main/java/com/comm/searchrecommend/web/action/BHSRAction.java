package com.comm.searchrecommend.web.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.comm.searchrecommend.entity.ThreadShardEntity;
import com.comm.searchrecommend.service.BasedSearchService;
import com.comm.searchrecommend.service.IRecommend;
import com.comm.searchrecommend.service.RecommendFactory;
import com.comm.searchrecommend.service.SearchServiceFactory;
import com.comm.searchrecommend.utils.HttpUtils;
import com.comm.searchrecommend.utils.ThreadLocalHelper;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jasstion on 15/6/30.
 */
@Controller
@RequestMapping(value = "/inner/srservice")
public class BHSRAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(BHSRAction.class);

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
                IRecommend iRecommend = RecommendFactory.createRecommender(appKey);
                data = iRecommend.recommend(paraMap);

            } else {
                code = -100;
                msg = "调用错误，请正确的传递参数.";
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            LOGGER.info("appKey对应的srevice调用失败，具体异常信息是：" + msg + "");
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
                IRecommend iRecommend = RecommendFactory.createRecommender(appKey);
                iRecommend.clearAppRuleCache(appKey);

            } else {
                code = -100;
                msg = "调用错误，请正确的传递参数.";
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            LOGGER.info("appKey对应的srevice调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);

    }

    @RequestMapping(value = "/search")
    public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UUID uuid = UUID.randomUUID();
        //每次服务请求对应的唯一id
        String uuidStr = uuid.toString();
        ThreadLocal<ThreadShardEntity> threadShardEntity = new ThreadLocal<ThreadShardEntity>();
        ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
        threadShardEntity.set(threadShardEntity_);

        int code = 200;
        String msg = "正常调用";
        Object data = null;
        String parameterErrorMsg = "参数传递错误！";
        String params = request.getParameter("params");
        String appKey=request.getParameter("appKey");
        try {
            if(StringUtils.isEmpty(appKey)){
                code=-100;
                msg = "搜索服务调用失败，请传递搜索服务标识";



            }

            if (StringUtils.isEmpty(params)) {
                code=-100;
                msg = "搜索服务调用失败，请传递搜索查询内容";
            }
            else{
                BasedSearchService searchService=SearchServiceFactory.srServices.get(appKey);


                data =  searchService.search(params);
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            LOGGER.info("搜索服务调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);
        threadShardEntity.remove();
    }




    private void printJsonTemplate(int code, String msg, Object data,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("traceID", request.getParameter("traceID"));
        result.put("code", code);
        result.put("msg", msg);
        ThreadShardEntity threadShardEntity=null;
        try {
             threadShardEntity= ThreadLocalHelper.getThreadShardEntity();
        } catch (Exception e) {
            LOGGER.info("error to access threadShardEntity, message is: "+ ExceptionUtils.getMessage(e.getCause())+"");
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

        String params_="{\"cacheStrategy\":null,\"distance\":null,\"fls\":[\"userID\",\"score\",\"age\",\"height\",\"registeDate\"],\"functionQuerysList\":[],\"gender\":-1,\"locationPoint\":null,\"pageNum\":1,\"pageSize\":10,\"queryItems\":[{\"fieldName\":\"height\",\"isFilterType\":false,\"matchedValues\":[\"158#TO #159\",\"178#TO# 179\"]},{\"fieldName\":\"age\",\"isFilterType\":false,\"matchedValues\":[\"1988\",\"1999\"]},{\"fieldName\":\"gender\",\"isFilterType\":true,\"matchedValues\":[\"0\"]},{\"fieldName\":\"registeDate\",\"isFilterType\":true,\"matchedValues\":[\"2014-02-15T18:59:51Z#TO #2015-02-15T18:59:51Z\",\"2009-02-15T18:59:51Z#TO #2011-02-15T18:59:51Z\"]}],\"sortItems\":[{\"fieldName\":\"age\",\"sort\":\"desc\"},{\"fieldName\":\"height\",\"sort\":\"desc\"},{\"fieldName\":\"registeDate\",\"sort\":\"desc\"}]}";
        final String userID = "130106652";
        String url = "http://srservice1.comm.com/inner/searchRecommend/search.json";
        Map<String, Object> params = Maps.newHashMap();
        params.put("params", params_);
        params.put("APIKey", "1BJUTYXAQA6LS9796PZ7ET8P0X9KT1J1");

        String result=HttpUtils.executeWithHttp(url,params);


        System.out.print(result);

    }
}
