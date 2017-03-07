package com.comm.sr.service.score;

import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.util.Map;

/**
 * Created by jasstion on 07/11/2016.
 */
public class ScriptService {
  final static ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();// new
                                                                                        // ScriptEngineManager().getEngineByName("nashorn");

  public static Object eval(String script, Map<String, Object> params) throws ScriptException {
    Object result = null;
    SimpleBindings simpleBindings = new SimpleBindings();
    for (Map.Entry<String, Object> param : params.entrySet()) {
      simpleBindings.put(param.getKey(), param.getValue());
    }
    result = engine.eval(script, simpleBindings);
    return result;
  }

  public static void main(String[] args) throws IOException, ScriptException {
    ScriptService scriptService = new ScriptService();

//    // （b+clickNum/(viewedNum+1)*f1+downloadNum/(viewedNum+1)*f2+favoriteNum/(viewedNum+1))*f3／f(viewedNum+1)
//    String computeScript =
//        "(b+(clickNum+d)/(viewedNum+1)*fc+(downloadNum+d)/(viewedNum+1)*fd+(favoriteNum+d)/(viewedNum+1)*ff)/((Math.log(viewedNum+1) / Math.LN10)+m)";// +1/((Math.log(viewedNum+1)
//    // //
//    // Math.LN10)+1)
//
//
//    Map<String, Object> params_ = Maps.newHashMap();
//    params_.put("clickNum", 226);
//    params_.put("viewedNum", 226);
//    params_.put("downloadNum", 0);
//    params_.put("favoriteNum", 0);
//    params_.put("b", 0.5);
//    params_.put("m", 2);//
//    params_.put("d", 0.1);//提升曝光次数为零的权重
//    params_.put("fc", 1);
//    params_.put("fd", 1);
//    params_.put("ff", 1);
//
//    Double dou = (Double) scriptService.eval(computeScript, params_);
//    System.out.print(dou + "\n");


    String computeScript =
        "Math.log(clickNum*fc+downloadNum*fd+favoriteNum*ff+3)";// +1/((Math.log(viewedNum+1)
    Map<String, Object> params_ = Maps.newHashMap();
    Map values_ = Maps.newLinkedHashMap();
    int clickNumTotal = 1855;
    int downloadNumTotal =1846;
    int favoriteNumTotal = 1;
    params_.put("clickNum", clickNumTotal);
    params_.put("downloadNum", downloadNumTotal);
    params_.put("favoriteNum", favoriteNumTotal);
    params_.put("fc", 1);
    params_.put("fd", 10);
    params_.put("ff", 3);
    Double dou = (Double) scriptService.eval(computeScript, params_);
    System.out.print(dou);

  }
}
