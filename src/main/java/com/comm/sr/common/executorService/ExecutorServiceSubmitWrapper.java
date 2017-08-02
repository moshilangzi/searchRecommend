package com.comm.sr.common.executorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created by jasstion on 6/6/16.
 */
public class ExecutorServiceSubmitWrapper {
  protected final static Logger mLog = LoggerFactory.getLogger(ExecutorServiceSubmitWrapper.class);

  private int maxRetryNum = 1;
  private ExecutorService executorService;
  private Callable<Boolean> futureTask = null;

  public ExecutorServiceSubmitWrapper(int maxRetryNum, ExecutorService executorService,
      Callable<Boolean> futureTask) {

    this.maxRetryNum = maxRetryNum;
    this.executorService = executorService;
    this.futureTask = futureTask;

  }

  public ExecutorServiceSubmitWrapper(ExecutorService executorService,
      Callable<Boolean> futureTask) {

    this.executorService = executorService;
    this.futureTask = futureTask;

  }

  public void submit() throws ExecutionException, InterruptedException {
    executorService.submit(futureTask);

  }

}
