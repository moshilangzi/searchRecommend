package com.comm.sr.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HttpUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClient httpClient = new HttpClient(httpConnectionManager);

    static {


        httpClient.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(500);

        httpClient.getHttpConnectionManager().getParams().setMaxTotalConnections(1000);

        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(8000);

        httpClient.getHttpConnectionManager().getParams().setSoTimeout(8000);

        httpClient.getHttpConnectionManager().getParams().setTcpNoDelay(true);

        httpClient.getHttpConnectionManager().getParams().setLinger(1000);

        HttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);


    }

    public static String executeWithHttp(String thirdURL){
        HttpMethod httpMethod = new GetMethod(thirdURL);
        URI uri = null;
        try {
            uri = httpMethod.getURI();
                LOGGER.debug(">>>>--{}", uri.toString());

            int statusCode = httpClient.executeMethod(httpMethod);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("执行Http Get方法出错ִ[{}]", statusCode);
            }
            String result = httpMethod.getResponseBodyAsString();
                LOGGER.debug("<<<<--{}", result);

            return result;
        } catch (Exception e) {
            LOGGER.error("异常信息：[{}]", uri != null ? uri.toString() : thirdURL, e);
        } finally {
            httpMethod.releaseConnection();
        }
        return "";
    }

    public static String executeWithHttp(String thirdURL, Map<String, Object> params)  {
        HttpMethod httpMethod = new GetMethod(thirdURL);
        URI uri = null;
        NameValuePair[] nvps = convertMapToNameValuePair(params);
        if (nvps !=null && (nvps.length > 0)) {
            httpMethod.setQueryString(nvps);
        }
        try {
            uri = httpMethod.getURI();
                LOGGER.debug(">>>>--{}", uri.toString());
            
            int statusCode = httpClient.executeMethod(httpMethod);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("执行Http Get方法出错ִ[{}]", statusCode);
            }
            String result = httpMethod.getResponseBodyAsString();
                LOGGER.debug("<<<<--{}", result);
            
            return result;
        } catch (Exception e) {
            LOGGER.error("异常信息：[{}]", uri != null ? uri.toString() : thirdURL, e);
        } finally {
            httpMethod.releaseConnection();
        }
        return "";
    }

    public static byte[] executeWithHttpImageUrl(String thirdURL, Map<String, Object> params)  {
        byte[] result=null;
        HttpMethod httpMethod = new GetMethod(thirdURL);
        URI uri = null;
        NameValuePair[] nvps = convertMapToNameValuePair(params);
        if (nvps !=null && (nvps.length > 0)) {
            httpMethod.setQueryString(nvps);
        }
        try {
            uri = httpMethod.getURI();
            LOGGER.debug(">>>>--{}", uri.toString());

            int statusCode = httpClient.executeMethod(httpMethod);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("执行Http Get方法出错ִ[{}]", statusCode);
            }
            result=httpMethod.getResponseBody();


            return result;
        } catch (Exception e) {
            LOGGER.error("异常信息：[{}]", uri != null ? uri.toString() : thirdURL, e);
        } finally {
            httpMethod.releaseConnection();
        }
        return null;
    }
    final static CloseableHttpClient httpclient = HttpClients.createDefault();
    static {


    }
    public static String executeWithHttpPost(String thirdURL, Map<String, Object> params)
     {
         String responseStr=null;
         HttpPost httppost=null;



        try {
             httppost= new HttpPost(thirdURL);
            String queryData=GsonHelper.objToJson(params);

            StringEntity stringEntity=new StringEntity(queryData,ContentType.APPLICATION_JSON);

            // It may be more appropriate to use FileEntity class in this particular
            // instance but we are using a more generic InputStreamEntity to demonstrate
            // the capability to stream out data from any arbitrary source
            //
            // FileEntity entity = new FileEntity(file, "binary/octet-stream");

            httppost.setEntity(stringEntity);

            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {

               responseStr=EntityUtils.toString(response.getEntity());
            } finally {
                response.close();
            }
        }catch (Exception e){

            LOGGER.info(com.yufei.utils.ExceptionUtil.getExceptionDetailsMessage(e));

        }



//        PostMethod httpMethod = new PostMethod(thirdURL);
//        NameValuePair[] nvps = convertMapToNameValuePair(params);
//
//        URI uri = null;
//
//        if (nvps !=null && (nvps.length > 0)) {
//            //httpMethod.setRequestBody(nvps);
//            String queryData=GsonHelper.objToJson(params);
//            LOGGER.info("post data:"+queryData+"");
//            //httpMethod.addParameter("data",queryData);
//            httpMethod.setRequestEntity(new StringRequestEntity(queryData));
//        }
//        try {
//            uri = httpMethod.getURI();
//            LOGGER.debug(">>>>--{}", uri.toString());
//
//            int statusCode = httpClient.executeMethod(httpMethod);
//            if (statusCode != HttpStatus.SC_OK) {
//                LOGGER.error("执行Http Post方法出错ִ[{}]", statusCode);
//            }
//            InputStream stream = httpMethod.getResponseBodyAsStream();
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
//            StringBuffer buf = new StringBuffer();
//            String line;
//            while (null != (line = br.readLine())) {
//                buf.append(line).append("\n");
//            }
//            LOGGER.debug("<<<<--{}", buf.toString());
//
//            return buf.toString();
//        } catch (Exception e) {
//            LOGGER.error("异常信息：[{}]", uri != null ? uri.toString() : thirdURL, e);
//        } finally {
//            httpMethod.releaseConnection();
//        }
//        return "";
         return responseStr;
    }

    public static NameValuePair[] convertMapToNameValuePair(Map<String, Object> params) {
        //"phrase":"sky","onlineState":1,"debug":"true"
        if (params == null) {
            return null;
        }
        Set<String> paramsSet = params.keySet();
        NameValuePair[] nvps = new NameValuePair[paramsSet.size()];
        int i = 0;
        for (Iterator<String> it = paramsSet.iterator(); it.hasNext(); i++) {
            String name = it.next();
            Object value = params.get(name);
            nvps[i] = new NameValuePair(name, value.toString());
        }
        return nvps;
    }
    public static void main(String[] args) throws UnsupportedEncodingException {
//        String testUrl="http://search.vcg.csdn.net/search?_client_=creative";
//        //perpage:300
//        //page:1
//        Map<String,Object> requestParames= Maps.newHashMap();
//        requestParames.put("phrase","sky");
//        requestParames.put("onlineState","1");
//        requestParames.put("debug",true);
//        requestParames.put("perpage","100");
//        requestParames.put("page","1");
//        requestParames.put("sort","best_adv");
//        requestParames.put("fields",Lists.newArrayList("brandId","collectionId","resId","uploadTime","licenseType"));
//
//       // "fields":["collectionId","resId","uploadTime","licenseType"]
//
//        String str=HttpUtils.executeWithHttpPost(testUrl,requestParames);
//        System.out.print(str);
//        List<String> kwIds= Lists.newArrayList();
//        JSONObject topObject= JSON.parseObject(str);
//        kwIds = JsonPath.read(str, "$.debug.keywordDetail.kids");
//        List<Map<String, String>> images =  JsonPath.parse(str).read("$.datas[*]");
//
//
//        LOGGER.error(kwIds.toString());
//        LOGGER.info(images.toString());
//        LOGGER.info(images.toString());
String str="http://60.205.226.115:8080/inner/srservice/matchImageBasedImageUrl.json?fields=imageId&topNum=10&groupNum=3&scoreThresholdValue=5&distanceType=euclidean&imageUrl=http%3A%2F%2Fww3.sinaimg.cn%2Forj360%2Fb7d8c289jw1exuvy4rg3aj20go0b1mzi.jpg";
System.out.println(HttpUtils.executeWithHttp(str));















    }


}
