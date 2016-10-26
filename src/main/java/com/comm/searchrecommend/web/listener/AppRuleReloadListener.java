package com.comm.searchrecommend.web.listener;

import com.comm.searchrecommend.service.AppRuleAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jasstion on 8/23/16.
 */
public class AppRuleReloadListener extends org.springframework.web.context.ContextLoaderListener{
    private static final Logger logger = LoggerFactory.getLogger(AppRuleReloadListener.class);

    public AppRuleReloadListener()
    {
        logger.info( "Starting reload the apprule to redis......" );
        AppRuleAdmin.createRecommendAppRules();
        logger.info( "finishing  reload the apprule to redis!" );

    }

}
