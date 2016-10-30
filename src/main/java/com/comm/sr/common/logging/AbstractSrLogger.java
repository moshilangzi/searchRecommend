package com.comm.sr.common.logging;

import org.elasticsearch.common.logging.support.LoggerMessageFormat;

/**
 * Created by jasstion on 30/10/2016.
 */
public abstract class AbstractSrLogger implements SrLogger {

    private final String prefix;

    protected AbstractSrLogger(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void trace(String msg, Object... params) {
        if (isTraceEnabled()) {
            internalTrace(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalTrace(String msg);

    @Override
    public void trace(String msg, Throwable cause, Object... params) {
        if (isTraceEnabled()) {
            internalTrace(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalTrace(String msg, Throwable cause);


    @Override
    public void debug(String msg, Object... params) {
        if (isDebugEnabled()) {
            internalDebug(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalDebug(String msg);

    @Override
    public void debug(String msg, Throwable cause, Object... params) {
        if (isDebugEnabled()) {
            internalDebug(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalDebug(String msg, Throwable cause);


    @Override
    public void info(String msg, Object... params) {
        if (isInfoEnabled()) {
            internalInfo(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalInfo(String msg);

    @Override
    public void info(String msg, Throwable cause, Object... params) {
        if (isInfoEnabled()) {
            internalInfo(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalInfo(String msg, Throwable cause);


    @Override
    public void warn(String msg, Object... params) {
        if (isWarnEnabled()) {
            internalWarn(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalWarn(String msg);

    @Override
    public void warn(String msg, Throwable cause, Object... params) {
        if (isWarnEnabled()) {
            internalWarn(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalWarn(String msg, Throwable cause);


    @Override
    public void error(String msg, Object... params) {
        if (isErrorEnabled()) {
            internalError(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalError(String msg);

    @Override
    public void error(String msg, Throwable cause, Object... params) {
        if (isErrorEnabled()) {
            internalError(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalError(String msg, Throwable cause);
}
