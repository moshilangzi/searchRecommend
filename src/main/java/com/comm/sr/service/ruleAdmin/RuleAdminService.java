package com.comm.sr.service.ruleAdmin;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.service.cache.CacheService;

import java.util.Properties;

/**
 * Created by jasstion on 29/10/2016.
 */
public class RuleAdminService extends AbstractComponent {
    protected final CacheService<String,String> cacheService;
    public RuleAdminService(Properties settings,CacheService<String,String> cacheService) {
        super(settings);
        this.cacheService=cacheService;
    }


}
