package com.comm.sr.common.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by jasstion on 30/10/2016.
 */
public class Log4jSrLogger extends AbstractSrLogger {

    private final Logger logger ;
    private final String FQCN = AbstractSrLogger.class.getName();

    public Log4jSrLogger(String prefix, Logger logger) {
        super(prefix);
        this.logger = logger;



    }

    @Override
    public void setLevel(String level) {
        logger.setLevel(Level.toLevel(level));

    }

    @Override
    public String getLevel() {
        // can't get it in slf4j...
        return logger.getLevel().toString();
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
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    protected void internalTrace(String msg) {

            logger.trace(msg);

    }

    @Override
    protected void internalTrace(String msg, Throwable cause) {


            logger.trace(msg);

    }

    @Override
    protected void internalDebug(String msg) {


            logger.debug(msg);

    }

    @Override
    protected void internalDebug(String msg, Throwable cause) {


            logger.debug(msg);

    }

    @Override
    protected void internalInfo(String msg) {


            logger.info(msg);

    }

    @Override
    protected void internalInfo(String msg, Throwable cause) {


            logger.info(msg, cause);

    }

    @Override
    protected void internalWarn(String msg) {


            logger.warn(msg);

    }

    @Override
    protected void internalWarn(String msg, Throwable cause) {


            logger.warn(msg);

    }

    @Override
    protected void internalError(String msg) {


            logger.error(msg);

    }

    @Override
    protected void internalError(String msg, Throwable cause) {

            logger.error(msg);

    }

    protected Logger logger() {
        return logger;
    }




    public static void main(String[] args){
       Logger logger=Logger.getLogger(Log4jSrLogger.class.getPackage().getName());
//
               Log4jSrLogger log4jSrLogger=new Log4jSrLogger("com.comm.sr",logger);
        log4jSrLogger.setLevel("TRACE");
        String level=log4jSrLogger.getLevel();
        System.out.print(level+"\n");
             log4jSrLogger.internalInfo("info");
        log4jSrLogger.internalError("error");
        log4jSrLogger.internalTrace("trace");
        log4jSrLogger.internalDebug("trace");

    }
}

