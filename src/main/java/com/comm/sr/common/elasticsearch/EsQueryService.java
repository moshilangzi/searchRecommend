/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.elasticsearch;

import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.service.cache.CacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yufei.utils.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jasstion
 */
public class EsQueryService extends AbstractQueryService<EsCommonQuery> {

    private Properties settings=null;
    protected final Map<String,TransportClient> clientMap= Maps.newHashMap();



   public TransportClient getEsClient(String clusterIndentity){
       return clientMap.get(clusterIndentity);

   }


    public EsQueryService(Properties settings,CacheService<String,String> cacheService) {
        super(cacheService,settings);
        try{


            Set<String> properyNames=settings.stringPropertyNames();
            Set<String> esClusterIdentitys= Sets.newHashSet();
            for(String propertyName:properyNames){
                if(propertyName.startsWith("elastic")){
                    esClusterIdentitys.add(propertyName.split("\\.")[1]);


                }

            }

            for(String identity:esClusterIdentitys){
                String hosts=null;
                String clusterName=null;
                for(String propertyName:properyNames){
                    if(propertyName.startsWith("elastic."+identity+".clusterName")){
                        clusterName=settings.getProperty(propertyName);
                    }
                    if(propertyName.startsWith("elastic."+identity+".hosts")){
                        hosts=settings.getProperty(propertyName);
                    }

                }
                Settings esSettings = Settings.settingsBuilder().put("cluster.name", clusterName).build();

                TransportClient client=TransportClient.builder().settings(esSettings).build();

                for(String host:hosts.split(";")){

                    try {
                        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host.split(":")[0]), Integer.parseInt(host.split(":")[1])));
                    } catch (Exception e) {
                        logger.error("error to get elasticsearch client!");
                    }

                }
                clientMap.put(identity,client);

            }



        }catch (Exception e){
            throw  new RuntimeException("can not connect to elastic cluster! program will exit!");
        }









    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

    }



    @Override
    public List<Map<String, Object>> query(EsCommonQuery baiheQuery) throws Exception {
        String clusteridentity=baiheQuery.getClusterIdentity();
        TransportClient client=clientMap.get(clusteridentity);
        List<Map<String, Object>> results = Lists.newArrayList();
        EsQueryGenerator.EsQueryWrapper  esQueryWrapper=new EsQueryGenerator().generateFinalQuery(baiheQuery);
        SearchRequestBuilder searchRequestBuilder=client.prepareSearch().setSource(esQueryWrapper.getSearchSourceBuilder().toString()).setIndices(esQueryWrapper.getIndexName());
        if(!StringUtils.isEmpty(baiheQuery.getRoutings())){
            searchRequestBuilder.setRouting(baiheQuery.getRoutings());
            logger.debug("routing values:"+baiheQuery.getRoutings().toString()+"");

        }
        logger.debug(searchRequestBuilder.toString());

        SearchResponse searchResponse=searchRequestBuilder.execute().actionGet();
        SearchHits searchHits=searchResponse.getHits();
        long totalCount=searchHits.getTotalHits();
        for (SearchHit hit : searchHits.getHits()) {
            Map<String, Object> values = hit.getSource();
            float score=hit.getScore();

            if(score>=0f){
                values.put("score",score);
            }





            results.add(values);

        }






        return results;

    }

    @Override public Map<String, Object> queryAll(EsCommonQuery baiheQuery) throws Exception {
        String clusteridentity=baiheQuery.getClusterIdentity();
        TransportClient client=clientMap.get(clusteridentity);
        Map<String, Object> finalResults=Maps.newHashMap();
        List<Map<String, Object>> results = Lists.newArrayList();
        EsQueryGenerator.EsQueryWrapper  esQueryWrapper=new EsQueryGenerator().generateFinalQuery(baiheQuery);
        SearchResponse searchResponse=client.prepareSearch().setSource(esQueryWrapper.getSearchSourceBuilder().toString()).setIndices(esQueryWrapper.getIndexName())
            .execute().actionGet();
        SearchHits searchHits=searchResponse.getHits();
        long totalCount=searchHits.getTotalHits();
        for (SearchHit hit : searchHits.getHits()) {
            Map<String, Object> values = hit.getSource();
            values.put("score",hit.getScore());




            results.add(values);

        }

        Map<String, Object> exInfo=Maps.newHashMap();
        exInfo.put("totalCount",totalCount);


        finalResults.put("exInfo",exInfo);
        finalResults.put("conInfo",results);


        return finalResults;
    }

}
