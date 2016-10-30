package com.comm.sr.service;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.entity.CommonQuery;
import com.comm.sr.common.entity.SearchServiceRule;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.utils.GsonHelper;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jasstion on 23/10/2016.
 */
public abstract class BasedSearchService<A extends AbstractQueryService,Q extends CommonQuery, SR extends SearchServiceRule, R2> extends AbstractComponent implements Serializable {
    protected final static Logger m_log = LoggerFactory.getLogger(BasedSearchService.class);
    protected AbstractQueryService queryService=null;
    protected SR serviceRule=null;



    public BasedSearchService(AbstractQueryService queryService, SR serviceRule,Properties settings) {
        super(settings);
        this.queryService = queryService;
        this.serviceRule = serviceRule;
    }

    public BasedSearchService(A queryService,Properties settings) {

        super(settings);
        this.queryService = queryService;
    }

    public abstract void customizableFinalQuery(Q query);

    public abstract R2 postOperationAfterSearch(List<Map<String,Object>> results);
    public   void makeFinalQuery(Q query){
        doMakeFinalQuery(query);
        customizableFinalQuery(query);

    }
    private void  doMakeFinalQuery(Q query){
        //simple add comm setting like sort, filter to query
        String cacheStr = null;
        String sortStr = null;
        if (serviceRule != null) {
            cacheStr = serviceRule.getCacheStrategy();
            sortStr = serviceRule.getSort();
        }
        if (cacheStr != null && cacheStr.trim().length() > 0) {
            query.setCacheStrategy(cacheStr);
        }
        if (sortStr != null && sortStr.trim().length() > 0) {
            List<SortItem> sortItems_ = (List<SortItem>) GsonHelper.jsonToObj(sortStr, new TypeToken<ArrayList<SortItem>>() {
            }.getType());
            query.setSortItems(sortItems_);
        }





    }


    public R2 search(String queryStr){



        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class aClass = (Class) params[1];
        Q query = (Q) GsonHelper.jsonToObj(queryStr,aClass);

        makeFinalQuery(query);

        List<Map<String,Object>> results= null;
        try {
            results = doSearch(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        R2 finalResults=postOperationAfterSearch(results);
        return finalResults;



    }
    public  List<Map<String,Object>> doSearch(Q query) throws Exception {
        return queryService.processQuery(query);
    }





}
