package com.comm.sr.common.logging;

import org.elasticsearch.common.logging.support.AbstractESLogger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Created by jasstion on 30/10/2016.
 */
public class Slf4jSrLogger extends AbstractSrLogger {

    private final org.slf4j.Logger logger;
    private final LocationAwareLogger lALogger;
    private final String FQCN = AbstractESLogger.class.getName();

    public Slf4jSrLogger(String prefix, org.slf4j.Logger logger) {
        super(prefix);
        this.logger = logger;
        if (logger instanceof LocationAwareLogger) {
            lALogger = (LocationAwareLogger) logger;
        } else {
            lALogger = null;
        }
    }

    @Override
    public void setLevel(String level) {
        // can't set it in slf4j...
    }

    @Override
    public String getLevel() {
        // can't get it in slf4j...
        return null;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    protected void internalTrace(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, null);
        } else {
            logger.trace(msg);
        }
    }

    @Override
    protected void internalTrace(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, cause);
        } else {
            logger.trace(msg);
        }
    }

    @Override
    protected void internalDebug(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
        } else {
            logger.debug(msg);
        }
    }

    @Override
    protected void internalDebug(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, cause);
        } else {
            logger.debug(msg);
        }
    }

    @Override
    protected void internalInfo(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
        } else {
            logger.info(msg);
        }
    }

    @Override
    protected void internalInfo(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, cause);
        } else {
            logger.info(msg, cause);
        }
    }

    @Override
    protected void internalWarn(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
        } else {
            logger.warn(msg);
        }
    }

    @Override
    protected void internalWarn(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, cause);
        } else {
            logger.warn(msg);
        }
    }

    @Override
    protected void internalError(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
        } else {
            logger.error(msg);
        }
    }

    @Override
    protected void internalError(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, cause);
        } else {
            logger.error(msg);
        }
    }

    protected org.slf4j.Logger logger() {
        return logger;
    }




    public static void main(String[] args){
        org.slf4j.Logger logger= LoggerFactory.getLogger(Slf4jSrLogger.class);
        Slf4jSrLogger log4jSrLogger=new Slf4jSrLogger("",logger);

        String level=log4jSrLogger.getLevel();
        System.out.print(level);
        log4jSrLogger.internalDebug("hello");

    }
}

