/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.service;

import com.yufei.searchrecommend.solr.SolrQueryService;

/**
 *
 * @author jasstion
 */
public class SearchFactory {
    private final static AbstractQueryService AB_ABSTRACT_QUERY_SERVICE=null;
    public static AbstractQueryService createQueryService(){
          return AB_ABSTRACT_QUERY_SERVICE==null?new SolrQueryService():AB_ABSTRACT_QUERY_SERVICE;
    }
    
}
