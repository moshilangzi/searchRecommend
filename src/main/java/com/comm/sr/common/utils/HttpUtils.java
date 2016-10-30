package com.comm.sr.common.utils;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class HttpUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClient httpClient = new HttpClient(httpConnectionManager);

    static {


        httpClient.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(500);

        httpClient.getHttpConnectionManager().getParams().setMaxTotalConnections(1000);

        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        httpClient.getHttpConnectionManager().getParams().setSoTimeout(5000);

        httpClient.getHttpConnectionManager().getParams().setTcpNoDelay(true);

        httpClient.getHttpConnectionManager().getParams().setLinger(1000);


        httpClient.getHttpConnectionManager().getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

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


    public static NameValuePair[] convertMapToNameValuePair(Map<String, Object> params) {
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


}
