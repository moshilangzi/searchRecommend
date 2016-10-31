package com.comm.sr.common.component;

import com.comm.sr.common.logging.Log4jSrLogger;
import com.comm.sr.common.logging.SrLogger;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by jasstion on 29/10/2016.
 */
public abstract class AbstractComponent {
    protected final Properties settings;
    protected final SrLogger logger;

    public AbstractComponent(Properties settings) {
        this.settings=settings;
        logger=new Log4jSrLogger("", Logger.getLogger(getClass()));


    }

}
