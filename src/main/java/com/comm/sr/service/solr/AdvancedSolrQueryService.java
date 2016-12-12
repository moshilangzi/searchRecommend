/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.comm.sr.service.solr;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.comm.sr.common.entity.AdvancedCommQuery;
import com.comm.sr.service.AbstractQueryService;
import com.comm.sr.service.cache.CacheService;

/**
 * @author jasstion
 */
public class AdvancedSolrQueryService extends AbstractQueryService<AdvancedCommQuery> {

  public AdvancedSolrQueryService(CacheService<String, String> cacheService, Properties settings) {
    super(cacheService, settings);
  }

  @Override
  public List<Map<String, Object>> processQuery(AdvancedCommQuery baiheQuery) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  public List<Map<String, Object>> query(AdvancedCommQuery baiheQuery) throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

}
