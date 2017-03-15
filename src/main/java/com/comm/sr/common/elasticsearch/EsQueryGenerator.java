/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.elasticsearch;

import com.comm.sr.common.core.QueryGenerator;
import com.comm.sr.common.entity.EsCommonQuery;
import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SortItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.Instances;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbib.elasticsearch.index.query.PayloadTermQueryBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public class EsQueryGenerator implements QueryGenerator<EsQueryGenerator.EsQueryWrapper, EsCommonQuery> {

    protected final static Logger LOGGER = LoggerFactory.getLogger(EsQueryGenerator.class);

    @Override
    public EsQueryGenerator.EsQueryWrapper generateFinalQuery(EsCommonQuery query) {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String index = query.getIndex();
        String type = query.getType();
        String queryStr=query.getQueryStr();
        if(queryStr!=null&&!queryStr.isEmpty()){
            searchSourceBuilder.query(queryStr);
            EsQueryWrapper esQueryWrapper=new EsQueryWrapper(index,searchSourceBuilder,type);
            return esQueryWrapper;


        }

        List<String> fls = query.getFls();
        List<SortItem> sortItems = query.getSortItems();
        int pageNum = query.getPageNum();
        if(pageNum>0){
            int pageSize = query.getPageSize();
            if (pageNum < 1) {
                //default first page
                pageNum = 1;
            } else {
                pageNum = query.getPageNum();
            }



            searchSourceBuilder.from((pageNum - 1) * pageSize).size(pageSize);

        }
        if(pageNum<0){
            searchSourceBuilder.from((query.getOffset()) ).size(query.getLimit());

        }

        if(sortItems!=null){
            for (SortItem sortItem : sortItems) {
                searchSourceBuilder.sort(sortItem.getFieldName(), sortItem.getSort().trim().equals("asc") ? SortOrder.ASC : SortOrder.DESC);

            }
        }



        String location = query.getLocationPoint();
        Double distance = query.getDistance();
        QueryBuilder distanceQueryBuilder=null;
        if (location != null && distance != null) {
            distanceQueryBuilder= new GeoDistanceQueryBuilder("location")
                    .geohash(location)
                    .distance(distance, DistanceUnit.KILOMETERS)
                    .optimizeBbox("memory")
                    .geoDistance(GeoDistance.ARC);



        }
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();



        //解析subQuery
        SubQuery subQuery=query.getSubQuery();

        if(subQuery!=null){
            makeFinalBoolQuery(subQuery,boolQueryBuilder);


        }
        //添加filter

        BoolQueryBuilder filterQuery = new BoolQueryBuilder();

        List<SubQuery> subQueries=subQuery.getSubQuerys();
        if(subQueries!=null){

            for(SubQuery query1:subQueries){
                if(query1.getQueryItem()!=null&&query1.getQueryItem().isIsFilterType()){
                    //makeFilterBoolQuery(query1.getQueryItem(),filterQuery);
                    //makeFinalBoolQuery(query1,filterQuery);
                    makeBoolQuery(query1.getQueryItem(),filterQuery);



                }

            }
            if(filterQuery.hasClauses()){
                boolQueryBuilder.filter(filterQuery);
            }


        }



        //deal with function score query
//        if(StringUtils.isNotEmpty(query.getFunctionScoreQuery())){
//
//
//        }


        if(distanceQueryBuilder!=null){
            boolQueryBuilder.must(distanceQueryBuilder);

        }
        //deal with score script
        FunctionScoreQueryBuilder functionScoreQueryBuilder=null;
        if(!StringUtils.isEmpty(query.getScoreScript())||!StringUtils.isEmpty(query.getScript())){

           // String inlineScript = "((_score+1)*1.0 + 2.0/((1.469097882-(doc['uploadTime'].value as double)/1000000000)*17000+1.0))/_score";
            String inlineScript=query.getScoreScript();
            Map<String, Object> params = query.getScriptParams();
            String scriptLangtype=query.getScriptLangType();
            if(scriptLangtype==null){
                scriptLangtype=ScriptService.DEFAULT_LANG;

            }
            String script=query.getScript();
            if(script==null){
                script=inlineScript;
            }


            Script script_ = new Script(script, ScriptService.ScriptType.INLINE, scriptLangtype, params);

            ScriptScoreFunctionBuilder scriptBuilder = ScoreFunctionBuilders.scriptFunction(script_);






            functionScoreQueryBuilder = new FunctionScoreQueryBuilder(boolQueryBuilder);

            functionScoreQueryBuilder.add(scriptBuilder);
            functionScoreQueryBuilder.boostMode(CombineFunction.REPLACE);
            //functionScoreQueryBuilder.scoreMode("sum");



        }
        if(functionScoreQueryBuilder!=null){
            searchSourceBuilder.query(functionScoreQueryBuilder);


        }
        else{
            searchSourceBuilder.query(boolQueryBuilder);

        }
        //apply source field filter
        //searchSourceBuilder.fields(fls);
        String[] includeFields = new String[fls.size()];
        int i = 0;
        for (String includeField : fls) {
            includeFields[i++] = includeField;
        }
        searchSourceBuilder.fetchSource(includeFields, null);

        String finalQuery = searchSourceBuilder.toString();
        LOGGER.debug(finalQuery);
        EsQueryWrapper esQueryWrapper=new EsQueryWrapper(index,searchSourceBuilder,type);
        return esQueryWrapper;
    }


    private void makeBoolQuery(QueryItem queryItem, BoolQueryBuilder tmpBoolQueryBuilder) {
        String fieldName = queryItem.getFieldName();
        List<String> matchValues = queryItem.getMatchedValues();

        if (matchValues.size() > 0) {
            for (String matchValue : matchValues) {
                if (matchValue == null || matchValue.trim().length() < 1) {
                    continue;
                }

                if (matchValue.contains("TO")) {
                    String bv = null;
                    String ev = null;
                    if (matchValue.contains("#")) {
                        String[] vs = matchValue.split("#");
                        if (vs.length < 3) {
                            continue;
                        }
                        bv = vs[0];
                        ev = vs[2];
                    } else {
                        String[] vs = matchValue.split("TO");
                        if (vs.length < 2) {
                            continue;
                        }
                        bv = vs[0];
                        ev = vs[1];
                    }
//                        String[] vs = matchValue.split("#");
//                        if (vs.length < 3) {
//                            continue;
//                        }
//                        String bv = vs[0];
//                        String ev = vs[2];
                    RangeQueryBuilder termQueryBuilder = new RangeQueryBuilder(fieldName);
                    //range query date query or number query
                    //2014-02-15T18:59:51Z
                    if (bv.contains("T")) {
                        //date range query

                        termQueryBuilder.format("yyyy-MM-dd'T'HH:mm:ss'Z'").gte(bv).lte(ev);
                        tmpBoolQueryBuilder.should(termQueryBuilder);

                    } else {
                        termQueryBuilder.gte(bv).lte(ev);

                    }
                    tmpBoolQueryBuilder.should(termQueryBuilder);

                } else {

                    //not range query
                    //if payload term query
                    if(queryItem.isPayload()){
                        QueryBuilder termQueryBuilder = new PayloadTermQueryBuilder(fieldName, matchValue);


                        tmpBoolQueryBuilder.should(termQueryBuilder);


                    }
                    else{
                        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(fieldName, matchValue);
                        if(queryItem.isIsFilterType()){
                            tmpBoolQueryBuilder.must(termQueryBuilder);
                        }
                        else tmpBoolQueryBuilder.should(termQueryBuilder);

                    }


                }

            }

        }
    }

    public static class  EsQueryWrapper implements Serializable{
    private SearchSourceBuilder searchSourceBuilder=null;
    private String indexName=null;
    private String typeName=null;

    public EsQueryWrapper(String indexName, SearchSourceBuilder searchSourceBuilder, String typeName) {
        this.indexName = indexName;
        this.searchSourceBuilder = searchSourceBuilder;
        this.typeName = typeName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public SearchSourceBuilder getSearchSourceBuilder() {
        return searchSourceBuilder;
    }

    public void setSearchSourceBuilder(SearchSourceBuilder searchSourceBuilder) {
        this.searchSourceBuilder = searchSourceBuilder;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}

    private void makeFinalBoolQuery(SubQuery query, final BoolQueryBuilder boolQueryBuilder) {
        QueryItem queryItem=query.getQueryItem();
        List<SubQuery> subQuerys = query.getSubQuerys();



        if (subQuerys!=null&&!subQuerys.isEmpty()) {
            int length = subQuerys.size();





            for (int i = 0; i < subQuerys.size(); i++) {

                BoolQueryBuilder tempBoolQueryBuilder=new BoolQueryBuilder();
                    makeFinalBoolQuery(subQuerys.get(i),tempBoolQueryBuilder);
                if(tempBoolQueryBuilder.hasClauses()) {
                    if (query.getLogic().equals("AND")) {

                        boolQueryBuilder.must(tempBoolQueryBuilder);

                    }
                    if (query.getLogic().equals("OR")) {
                        boolQueryBuilder.should(tempBoolQueryBuilder);

                    }
                    if (query.getLogic().equals("NOT")) {
                        boolQueryBuilder.mustNot(tempBoolQueryBuilder);

                    }

                }
            }



        } else {
           // BoolQueryBuilder tempBoolQueryBuilder=new BoolQueryBuilder();
            if(queryItem!=null){
                if(!queryItem.isIsFilterType()){
                    makeBoolQuery(queryItem, boolQueryBuilder);
                }

            }


           // boolQueryBuilder.must(tempBoolQueryBuilder);





        }

        //finally
    }
    public static void main(String[] args) {
        SubQuery ageQuery = new SubQuery();
        QueryItem ageQueryItem=new QueryItem();
        ageQueryItem.setFieldName("age");
        ageQueryItem.setMatchedValues(Lists.newArrayList("1988"));
        ageQuery.setQueryItem(ageQueryItem);
        SubQuery heightQuery = new SubQuery();
        QueryItem heightQueryItem=new QueryItem();

        heightQueryItem.setFieldName("height");
        heightQueryItem.setMatchedValues(Lists.newArrayList("180"));
        heightQuery.setQueryItem(heightQueryItem);
        SubQuery finalQuery = new SubQuery();
        finalQuery.setLogic("OR");
        List<SubQuery> subQueries=Lists.newArrayList();
        finalQuery.setSubQuerys(subQueries);
        finalQuery.getSubQuerys().add(ageQuery);
        finalQuery.getSubQuerys().add(heightQuery);
        SubQuery sexQuery = new SubQuery();
        QueryItem sexQueryItem=new QueryItem();

        sexQueryItem.setFieldName("sex");
        sexQueryItem.setMatchedValues(Lists.newArrayList("1"));
        sexQuery.setQueryItem(sexQueryItem);

        SubQuery finalQuery_ = new SubQuery();
        finalQuery_.setLogic("AND");
        List<SubQuery> subQueries_=Lists.newArrayList();
        finalQuery_.setSubQuerys(subQueries_);
        finalQuery_.getSubQuerys().add(finalQuery);
        finalQuery_.getSubQuerys().add(sexQuery);
        LOGGER.info(Instances.gson.toJson(finalQuery_) + "\n");

        EsCommonQuery esCommonQuery = new EsCommonQuery(1, 5, null, Lists.newArrayList(), "test", "test");
        esCommonQuery.setSubQuery(finalQuery_);

        String query=new EsQueryGenerator().generateFinalQuery(esCommonQuery).getSearchSourceBuilder().toString();
        System.out.print(query);
    }
}
