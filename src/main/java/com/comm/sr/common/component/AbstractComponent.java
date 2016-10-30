package com.comm.sr.common.component;

import com.comm.sr.common.logging.SrLogger;

import java.util.Properties;

/**
 * Created by jasstion on 29/10/2016.
 */
public abstract class AbstractComponent {
    protected final Properties settings;
    protected final SrLogger srLogger;

    public AbstractComponent(Properties settings) {
        this.settings=settings;
        srLogger=null;


    }

}
