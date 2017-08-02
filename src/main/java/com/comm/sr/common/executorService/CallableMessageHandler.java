package com.comm.sr.common.executorService;

import com.yufei.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by jasstion on 6/6/16.
 */
public abstract class CallableMessageHandler<A> implements Callable<Boolean> {
  protected final static Logger mLog = LoggerFactory.getLogger(CallableMessageHandler.class);
  protected List<A> msgs = null;
  protected int maxRetryNum = 3;

  public CallableMessageHandler(List<A> messages) {
    this.msgs = messages;

  }

  public CallableMessageHandler(List<A> messages, int maxRetryNum) {
    this.msgs = messages;
    this.maxRetryNum = maxRetryNum;

  }

  public static void convertDate(String key, Map<String, String> map) {
    String time = map.get(key);// lastLoginTime

    map.remove(key);
    if (time != null && !time.isEmpty()) {
      if (time.indexOf("T") < 0 && time.indexOf("Z") < 0) {
        time = time.split("\\.")[0].replaceAll(" ", "T") + "Z";
      }
      map.put(key, time);

    }

  }

  public static String convertDate(String dateStr) {

    if (dateStr != null && !dateStr.isEmpty()) {
      if (dateStr.indexOf("T") < 0 && dateStr.indexOf("Z") < 0) {
        dateStr = dateStr.split("\\.")[0].replaceAll(" ", "T") + "Z";
      }

    }
    return dateStr;

  }

  @Override
  public Boolean call() throws Exception {

    int retryedNum = maxRetryNum;
    boolean isSucced = false;
    while (retryedNum > 0)
      try {
        if (isSucced) {
          break;
        }
        retryedNum--;

        processMessage();
        isSucced = true;
        mLog.info("messages process succeed! RetryNum is:" + (maxRetryNum - retryedNum) + "");
      } catch (Exception e) {
      mLog.info(ExceptionUtil.getExceptionDetailsMessage(e));

        mLog.info("messages process failure, will try again, left tryNum:" + (retryedNum) + "");

      }

    return isSucced;
  }

  public abstract void processMessage() throws Exception;
}
