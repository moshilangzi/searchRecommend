package com.comm.sr.common.component;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.comm.sr.common.logging.Log4jSrLogger;
import com.comm.sr.common.logging.SrLogger;

/**
 * Created by jasstion on 29/10/2016.
 */
public abstract class AbstractComponent {
  protected final Properties settings;
  protected final SrLogger logger;

  public AbstractComponent(Properties settings) {
    this.settings = settings;
    logger = new Log4jSrLogger("", Logger.getLogger(getClass()));

  }

}
