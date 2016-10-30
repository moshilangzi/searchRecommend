package com.comm.sr.service.impl;

import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.SearchServiceRule;
import com.comm.sr.service.AbstractQueryService;
import com.comm.sr.service.BasedSearchService;
import com.comm.sr.service.elasticsearch.EsQueryService;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jasstion on 23/10/2016.
 */
public class EsTestSearchService extends BasedSearchService<EsQueryService,EsCommonQuery,SearchServiceRule,List<Map<String,Object>>> {

    public EsTestSearchService(AbstractQueryService queryService, SearchServiceRule serviceRule,Properties settings) {
        super(queryService, serviceRule,settings);
    }



    @Override
    public void customizableFinalQuery(EsCommonQuery query) {


    }

    @Override
    public List<Map<String, Object>> postOperationAfterSearch(List<Map<String, Object>> results) {
        return results;
    }



}
