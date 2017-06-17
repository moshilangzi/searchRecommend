/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.web.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.comm.sr.common.entity.CommonQuery;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.entity.Image;
import com.comm.sr.common.utils.HttpUtils;
import org.junit.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public class BHSRActionTest {

    public BHSRActionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of recommend method, of class BHSRAction.
     */
    @Test
    public void testRecommend() throws Exception {
        //  a3da5d51b07251258cc2ac5a61b2da93：获取相似的人
        //59c937b30d1db924cc6d4d29d1ae607e: 获取感兴趣的人

        String recommendUrl = "http://localhost:8080/srService/inner/searchRecommend/recommend.json";
        //{"appKey":"59c937b30d1db924cc6d4d29d1ae607e","count":"6","profileUserId":"108134156","systemID":"sr","traceID":"1435904798602","visitUserId":"56027289"}

        Map<String, String> params = new HashMap<String, String>();
        params.put("traceID", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        params.put("systemID", "sr");
        //params.put("APIKey", userCloudKey);
        params.put("profileUserId", "108134156");
        params.put("visitUserId", "56027289");
        params.put("count", "16");
        params.put("appKey", "59c937b30d1db924cc6d4d29d1ae607e");
        //params.put("description", "ch");
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("params", com.alibaba.fastjson.JSONObject.toJSONString(params));
        String result1 = HttpUtils.executeWithHttp(recommendUrl, jsonMap);
        System.out.print("获取感兴趣的人的结果:" + result1 + "\n");

        params.put("appKey", "a3da5d51b07251258cc2ac5a61b2da93");
        
       jsonMap = new HashMap<String, Object>();
        jsonMap.put("params", com.alibaba.fastjson.JSONObject.toJSONString(params));
        result1 = HttpUtils.executeWithHttp(recommendUrl, jsonMap);
        System.out.print("获取相似的人的结果:" + result1 + "\n");
    }

    /**
     * Test of search method, of class BHSRAction.
     */
    @Test
    public void testSearch() throws Exception {
        String query_ = "{'cacheStrategy':'20','queryItems':[{'fieldName':'--height','matchedValues':['179']},{'fieldName':'age','matchedValues':['1988','1999']},{'fieldName':'gender','matchedValues':['0']},{'fieldName':'registeDate','matchedValues':['2014-02-15T18:59:51Z#TO #2015-02-15T18:59:51Z','2009-02-15T18:59:51Z#TO #2011-02-15T18:59:51Z']}],'gender':0,'pageNum':-1,'pageSize':18,'sortItems':[{'fieldName':'height','sort':'asc'}],'fls':['userID','height']}";
        String params = "{query:" + query_ + "}";
        String parameterErrorMsg = "参数传递错误！";
        List<Map<String, Object>> data = null;

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

        AbstractQueryService queryService =null;// SearchServiceFactory.createQueryService();
        CommonQuery query = null;//commQueryHelper.makecommQuery(queryStr, appKey);
        data = queryService.processQuery(query);
        for (Map<String, Object> data1 : data) {
            System.out.println(data1.get("userID") + ": " + data1.get("height") + "" + "\n");
        }
        String searchUrl = "http://localhost:8080/srService/inner/searchRecommend/searchNew.json";
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("traceID", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        paramsMap.put("systemID", "sr");
        paramsMap.put("query", query_);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        final String toJSONString = com.alibaba.fastjson.JSONObject.toJSONString(paramsMap);
        System.out.println(toJSONString + "\n");
        jsonMap.put("params", toJSONString);
        String result1 = HttpUtils.executeWithHttp(searchUrl, jsonMap);
        System.out.print(result1);
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
