package com.comm.sr.common.executorService;

import java.util.List;

/**
 * Created by jasstion on 5/13/16.
 */
public abstract class MessageHandler implements Runnable {

  protected List<String> msgs = null;

  public MessageHandler(List<String> messages) {
    this.msgs = messages;
  }

  @Override
  public void run() {
    processMessage();

  }

  public abstract void processMessage();
}
