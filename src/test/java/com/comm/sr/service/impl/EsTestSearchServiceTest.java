package com.comm.sr.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comm.sr.common.entity.*;
import com.comm.sr.common.utils.Constants;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.SearchServiceFactory;
import com.comm.sr.service.search.EsTestSearchService;
import com.google.common.collect.Lists;

/**
 * Created by jasstion on 24/10/2016.
 */
public class EsTestSearchServiceTest {
  protected final static Logger m_log = LoggerFactory.getLogger(EsTestSearchServiceTest.class);

  public static void main(String[] args) throws Exception {

    String indexName = "com";
    String typeName = "user";

    // search query: (des: basket or des: football) AND (-des:boss)
    List<QueryItem> items = Lists.newArrayList();

    SubQuery subQuery = new SubQuery();
    subQuery.setLogic("AND");
    QueryItem payloadQueryItem = new QueryItem("des", Lists.newArrayList("basket", "football"));
    payloadQueryItem.setIsPayload(true);
    SubQuery payloadQuery = new SubQuery("AND", payloadQueryItem);

    List<SubQuery> subQueries = Lists.newArrayList(payloadQuery);
    QueryItem queryItem1 = new QueryItem("des", Lists.newArrayList("boss"));
    SubQuery subQuery1 = new SubQuery("NOT", queryItem1);
    subQuery1.setSubQuerys(Lists
        .newArrayList(new SubQuery("AND", new QueryItem("age", Lists.newArrayList("1220TO1230")))));
    // subQueries.add(subQuery1);
    subQuery.setSubQuerys(subQueries);

    final List<String> fls = Lists.newArrayList("userId", "des", "name", "age");

    List<SortItem> sortItems = Lists.newArrayList();
    // logstash-2015.12.10 log4j
    // EsCommonQuery commQuery = new EsCommonQuery(items, 1, 18, sortItems, fls, "comm_user",
    // "user");
    EsCommonQuery commQuery = new EsCommonQuery(1, 5, sortItems, fls, indexName, typeName);
    commQuery.setSubQuery(subQuery);
    commQuery.setScoreScript("1.0");

    UUID uuid = UUID.randomUUID();
    // 每次服务请求对应的唯一id
    String uuidStr = uuid.toString();
    ThreadShardEntity threadShardEntity_ = new ThreadShardEntity(uuidStr);
    Constants.threadShardEntity.set(threadShardEntity_);

    EsTestSearchService esTestSearchService =
        (EsTestSearchService) SearchServiceFactory.srServices.get("esTest");
    String queryStr = GsonHelper.objToJson(commQuery);
    m_log.info(queryStr);

    List<Map<String, Object>> results = esTestSearchService.search(queryStr);
    for (Map<String, Object> user : results) {
      Object content = user.get("userId");
      // String c1=new String(content.getBytes(),"utf8");
      System.out.print(content + "\n");
      System.out.print(user.get("score") + "\n");
    }

  }
}
