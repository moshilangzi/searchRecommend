package com.comm.sr.service.search;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryService;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.SearchServiceRule;
import com.comm.sr.service.topic.TopicService;

/**
 * Created by jasstion on 23/10/2016.
 */
public class EsTestSearchService extends
    BasedSearchService<EsQueryService, EsCommonQuery, SearchServiceRule, List<Map<String, Object>>> {

  public EsTestSearchService(AbstractQueryService queryService, SearchServiceRule serviceRule,
      Properties settings, TopicService topicService) {
    super(queryService, serviceRule, settings, topicService);
  }

  @Override
  public void customizableFinalQuery(EsCommonQuery query) {

  }

  @Override
  public List<Map<String, Object>> postOperationAfterSearch(List<Map<String, Object>> results) {
    return results;
  }

}
