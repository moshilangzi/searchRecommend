/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.searchrecommend.service;


import com.comm.searchrecommend.entity.SearchServiceRule;
import com.comm.searchrecommend.service.impl.EsTestSearchService;
import com.comm.basedSearch.elasticsearch.EsQueryService;
import com.comm.basedSearch.service.AbstractQueryService;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public class SearchServiceFactory {
    private final static AbstractQueryService AB_ABSTRACT_QUERY_SERVICE=null;
    public static AbstractQueryService createQueryService(){
          return AB_ABSTRACT_QUERY_SERVICE==null?new EsQueryService(null):AB_ABSTRACT_QUERY_SERVICE;
    }


    final public static Map<String,BasedSearchService>  srServices= Maps.newHashMap();

    static {
        SearchServiceRule searchServiceRule=new SearchServiceRule();


        srServices.put("esTest",new EsTestSearchService(AB_ABSTRACT_QUERY_SERVICE,searchServiceRule));
    }

    public static void main(String[] args){
        BasedSearchService searchService=srServices.get("esTest");
        String queryStr=null;
        List<Map<String,Object>> results=(List<Map<String,Object>>)searchService.search(queryStr);







    }




}
