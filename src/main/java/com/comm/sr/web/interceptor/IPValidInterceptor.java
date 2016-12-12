/**
 * User: Renxc Date: 13-8-15 Time: 上午9:38
 */
package com.comm.sr.web.interceptor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.FastHashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.comm.sr.common.utils.RequestUtils;

public class IPValidInterceptor implements HandlerInterceptor {
  private boolean openIPCheck = true;
  private String accessIP;
  private FastHashMap cache;

  public void init() {
    cache = new FastHashMap();
    cache.setFast(true);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o)
      throws Exception {
    if (openIPCheck) {
      String IP = RequestUtils.getRequestIP(request);
      if (IP != null && isAllow(IP)) {
        return true;
      } else {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        Map<String, Object> _map = new HashMap<String, Object>();
        _map.put("traceID", request.getParameter("traceID"));
        _map.put("code", -60);
        _map.put("msg", "调用方IP:[" + IP + "]不在允许访问lie");
        _map.put("data", new JSONObject());
        out.print(JSON.toJSONString(_map, SerializerFeature.WriteDateUseDateFormat));
        out.flush();
        out.close();
        return false;
      }
    } else {
      return true;
    }
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o,
      ModelAndView modelAndView) throws Exception {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o,
      Exception e) throws Exception {
  }

  private boolean isAllow(String ip) {
    // 如果已经通过验证,直接返回
    if (cache.containsKey(ip)) {
      return true;
    } else {
      // 遍历白名单,判断是否匹配
      String[] accessIPArray = accessIP.split("\\|");
      for (String tarIP : accessIPArray) {
        String[] srcIPGroup = ip.split("\\.");
        String[] tarIPGroup = tarIP.split("\\.");
        boolean match = true;
        for (int i = 0; i < 4; i++) {// 分组计算
          if (tarIPGroup[i].contains("-")) {// 区间通配符判断是否在区间内
            String[] tmp = tarIPGroup[i].replaceAll("[\\[\\]]", "").split("\\-");
            int min = Integer.valueOf(tmp[0]);
            int max = Integer.valueOf(tmp[1]);
            int srcTmp = Integer.valueOf(srcIPGroup[i]);
            if (srcTmp < min || srcTmp > max) {
              match = false;
              break;
            }
          } else if (!"*".equals(tarIPGroup[i]) && !srcIPGroup[i].equals(tarIPGroup[i])) {
            match = false;
            break;
          }
        }
        if (match) {
          cache.put(ip, true);
          ;
          return true;
        }
      }
      return false;
    }
  }

  public boolean isOpenIPCheck() {
    return openIPCheck;
  }

  public void setOpenIPCheck(boolean openIPCheck) {
    this.openIPCheck = openIPCheck;
  }

  public String getAccessIP() {
    return accessIP;
  }

  public void setAccessIP(String accessIP) {
    this.accessIP = accessIP;
  }
}
