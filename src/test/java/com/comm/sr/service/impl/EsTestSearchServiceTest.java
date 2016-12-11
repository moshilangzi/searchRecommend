package com.comm.sr.service.impl;

import com.comm.sr.common.elasticsearch.EsQueryGenerator;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.entity.ThreadShardEntity;
import com.comm.sr.common.utils.Constants;
import com.comm.sr.common.utils.DateTimeUtil;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.service.SearchServiceFactory;
import com.comm.sr.service.search.EsTestSearchService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jasstion on 24/10/2016.
 */
public class EsTestSearchServiceTest {
    protected final static Logger m_log = LoggerFactory.getLogger(EsTestSearchServiceTest.class);

    public static void main(String[] args) throws Exception{

      String indexName="vcg_creative";
      String typeName="vcgcsdn";
      SubQuery subQuery2=new SubQuery();
      subQuery2.setLogic("AND");
      SubQuery item1=new SubQuery();
      QueryItem qi=new QueryItem("onlineState",Lists.newArrayList("1"),false);

      qi.setIsFilterType(true);
      item1.setQueryItem(qi);
      SubQuery item2=new SubQuery();
      item2.setQueryItem(new QueryItem("prekey3", Lists.newArrayList("4165"), true));
      subQuery2.setSubQuerys(Lists.newArrayList(item1,item2));
      EsCommonQuery query = new EsCommonQuery(1, 5, null, Lists.newArrayList("createTime","uploadTime","prekey3","resId","_id","keywords"), indexName, typeName);
      query.setScoreScript("long uploadTime=doc['uploadTime'].value;def now = new Date() ;long comparedTime=now.getTime()-uploadTime; int hours=comparedTime/1000/60/60/24/1000000;if (hours>3) _score; else _score+100.0");
      query.setSubQuery(subQuery2);
      System.out.print(GsonHelper.objToJson(query) + "\n");
      EsQueryGenerator.EsQueryWrapper esQueryWrapper= new EsQueryGenerator().generateFinalQuery(query);


      UUID uuid = UUID.randomUUID();
      //每次服务请求对应的唯一id
      String uuidStr = uuid.toString();
      ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
      Constants.threadShardEntity.set(threadShardEntity_);


        EsTestSearchService esTestSearchService=
            (EsTestSearchService) SearchServiceFactory.srServices.get("esTest");
        String queryStr= GsonHelper.objToJson(query);
        m_log.info(queryStr);


        List<Map<String, Object>> results= esTestSearchService.search(queryStr);
        for (Map<String,Object> user:results) {

          long uploadTime=Long.parseLong((String)user.get("uploadTime"));
          Date uploadDate=DateTimeUtil.getDateFromTimeMillis((long)uploadTime);
          int hours=DateTimeUtil.getIntCompareToCurrDateHour(uploadDate);
          if(hours>3){
            user.put("new",true);
          }
          m_log.info(user.toString());



        }






    }
}
