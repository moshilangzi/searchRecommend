package com.comm.searchrecommend.service.impl;

import com.comm.searchrecommend.entity.SearchServiceRule;
import com.comm.searchrecommend.service.BasedSearchService;
import com.comm.basedSearch.elasticsearch.EsQueryService;
import com.comm.basedSearch.entity.EsCommonQuery;
import com.comm.basedSearch.service.AbstractQueryService;

import java.util.List;
import java.util.Map;

/**
 * Created by jasstion on 23/10/2016.
 */
public class EsTestSearchService extends BasedSearchService<EsQueryService,EsCommonQuery,SearchServiceRule,List<Map<String,Object>>> {

    public EsTestSearchService(AbstractQueryService queryService, SearchServiceRule serviceRule) {
        super(queryService, serviceRule);
    }



    @Override
    public void customizableFinalQuery(EsCommonQuery query) {


    }

    @Override
    public List<Map<String, Object>> postOperationAfterSearch(List<Map<String, Object>> results) {
        return results;
    }



}
