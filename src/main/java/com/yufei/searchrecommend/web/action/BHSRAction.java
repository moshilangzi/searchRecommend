package com.yufei.searchrecommend.web.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.service.AbstractQueryService;
import com.yufei.searchrecommend.service.IRecommend;
import com.yufei.searchrecommend.service.RecommendFactory;
import com.yufei.searchrecommend.service.SearchFactory;
import com.yufei.searchrecommend.utils.BaiheQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jasstion on 15/6/30.
 */
@Controller
@RequestMapping(value = "/inner/searchRecommend")
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
        Map data =null;
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
        int code = 200;
        String msg = "正常调用";
        List<Map<String, Object>> data = null;
        String parameterErrorMsg = "参数传递错误！";
        String params = request.getParameter("params");
        try {
            if (params != null && !params.trim().isEmpty()) {

                Map<String, String> paraMap = JSON.parseObject(params, new TypeReference<Map<String, String>>() {
                });
                String appKey = paraMap.get("appKey");
                String queryStr = paraMap.get("query");
//                if (appKey == null || appKey.trim().length() < 1) {
//                    throw new Exception(parameterErrorMsg);
//                }
                if (queryStr == null || queryStr.trim().length() < 1) {
                    throw new Exception(parameterErrorMsg);
                }

                AbstractQueryService queryService = SearchFactory.createQueryService();
                BaiheQuery query = BaiheQueryHelper.makeBaiheQuery(queryStr, appKey);
                data = queryService.processQuery(query);

            } else {
                code = -100;
                msg = "调用错误，请正确的传递参数.";
            }
        } catch (Exception e) {
            code = -100;
            msg = e.getMessage();
            LOGGER.info("搜索服务调用失败，具体异常信息是：" + msg + "");
        }
        printJsonTemplate(code, msg, data, request, response);
    }

    private void printJsonTemplate(int code, String msg, Object data,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("traceID", request.getParameter("traceID"));
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", (data != null) ? data : new JSONObject());

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));
        out.flush();
        out.close();
    }
    

}
