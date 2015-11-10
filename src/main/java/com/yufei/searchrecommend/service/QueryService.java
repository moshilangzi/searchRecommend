/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.service;

import com.yufei.searchrecommend.entity.AbstractQuery;
import com.yufei.searchrecommend.entity.BaiheQuery;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public interface QueryService<Q extends AbstractQuery> {
    public List<Map<String,Object>> query(Q yufeiQuery) throws Exception;
    
}
