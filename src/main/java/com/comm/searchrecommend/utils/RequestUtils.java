package com.comm.searchrecommend.utils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RequestUtils {

    /**
     * 获取远程访问IP地址
     * @return
     */
    public static String getRequestIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null) {
            String[] ipArray = ip.split(",");
            if (ipArray.length > 1) {
                ip = ipArray[0];
            }
        }
        return ip;
    }

    /**
     * 获取请求参数,同名参数只打印第一个
     * @param request
     * @return
     */
    public static Map<String, String> getRequsetParamMap(HttpServletRequest request) {
        Map properties  = request.getParameterMap();
        Iterator<Entry> it = properties.entrySet().iterator();
        Map<String, String> paramMap = new HashMap<String, String>();
        while(it.hasNext()) {
            Entry entry = it.next();
            String key = (String) entry.getKey();
            Object objectValue = entry.getValue();
            String value = null;
            if (null == objectValue) {
                value = "";
            } else if (objectValue instanceof String[]) {
                String[] values = (String[]) objectValue; //只打印同名参数的第一个值
                value = values[0];
            } else {
                value = objectValue.toString();
            }
            try {
                value = URLDecoder.decode(value, "utf-8");
            } catch (UnsupportedEncodingException e) {
                value = URLDecoder.decode(value);
            }
            paramMap.put(key, value);
        }
        return paramMap;
    }


    /**
     * 获取所有请求参数,同名参数的值用-拼接
     * @param request
     * @return
     */
    public static Map<String, String> getRequsetAllParamMap(HttpServletRequest request) {
        Map properties  = request.getParameterMap();
        Iterator<Entry> it = properties.entrySet().iterator();
        Map<String, String> paramMap = new HashMap<String, String>();
        while(it.hasNext()) {
            Entry entry = it.next();
            String key = (String) entry.getKey();
            Object objectValue = entry.getValue();
            String value = "";
            if (null == objectValue) {
                value = "";
            } else if (objectValue instanceof String[]) {
                String[] values = (String[]) objectValue;
                int valuesLength = values.length;
                for (int i = 0; i < valuesLength; i++) {  //同名参数的值用-拼接
                    if (i == (valuesLength - 1)) {
                        value = value + values[i];
                    } else {
                        value = value + values[i] + "-";
                    }
                }
            } else {
                value = objectValue.toString();
            }
            paramMap.put(key, value);
        }
        return paramMap;
    }


    /**
     * 获取请求数据
     * @param request
     * @return
     */
    public static String getRequestData(HttpServletRequest request) {
        StringBuilder requestData = new StringBuilder();
        requestData.append("BHRequestInfo=RemoteAddr:").append(getRequestIP(request));
        requestData.append("--RequestURL:").append(request.getRequestURL());
        requestData.append("--Referer:").append(request.getHeader("referer"));
        requestData.append("--AllParameterMap:").append(getRequsetAllParamMap(request));
        return requestData.toString();
    }
}
