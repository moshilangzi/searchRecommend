/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import com.yufei.searchrecommend.entity.AdvancedBaiheQuery;
import com.yufei.searchrecommend.service.AbstractQueryService;
import com.yufei.searchrecommend.service.QueryService;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public class AdvancedSolrQueryService extends AbstractQueryService<AdvancedBaiheQuery> implements QueryService<AdvancedBaiheQuery>{

    @Override
    public List<Map<String, Object>> processQuery(AdvancedBaiheQuery yufeiQuery) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Map<String, Object>> query(AdvancedBaiheQuery yufeiQuery) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
