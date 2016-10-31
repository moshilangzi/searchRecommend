/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.service.solr;


import com.comm.sr.common.entity.QueryItem;
import com.comm.sr.common.entity.SubQuery;
import com.comm.sr.common.utils.Instances;
import com.comm.sr.service.QueryGenerator;
import com.google.common.collect.Lists;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * @author jasstion
 */
public class AdvancedSolrQueryGenerator implements QueryGenerator<SolrQuery, SubQuery> {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AdvancedSolrQueryGenerator.class);

    @Override
    public SolrQuery generateFinalQuery(SubQuery query) {
        SolrQuery solrQuery = new SolrQuery();
        if (query == null) {
            return solrQuery;
        }
        StringBuffer solrQueryBuffer = new StringBuffer();
        makeFinalSolrQuery(query, solrQueryBuffer);
        LOGGER.info(solrQueryBuffer.toString());
        return solrQuery;
    }

    private void makeFinalSolrQuery(SubQuery query, final StringBuffer solrQueryBuffer) {
        QueryItem queryItem=query.getQueryItem();
        String logic = query.getLogic();
        List<SubQuery> subQuerys = query.getSubQuerys();

        if (subQuerys!=null&&!subQuerys.isEmpty()) {
            solrQueryBuffer.append("( ");
            int length = subQuerys.size();
            for (int i = 0; i < subQuerys.size(); i++) {
                makeFinalSolrQuery(subQuerys.get(i), solrQueryBuffer);
                if (i < (length - 1)) {
                    solrQueryBuffer.append(" " + logic + " ");
                }

            }

            solrQueryBuffer.append(" )");

        } else {
            if(queryItem!=null){
                solrQueryBuffer.append(generateQueryStrFromQueryItem(queryItem.getFieldName(), queryItem.getMatchedValues()));

            }
        }

        //finally 
    }

    private String generateQueryStrFromQueryItem(String fieldName, List<String> matchedValues) {
        StringBuilder queryStr = new StringBuilder();

        boolean ifNotEqual = false;
        if (fieldName.contains("--")) {
            ifNotEqual = true;
            fieldName = fieldName.replace("--", "");

        }
        if (ifNotEqual) {
            queryStr.append("-");
        }
        queryStr.append("( ");

        if (matchedValues.size() > 0) {
            for (String matchValue : matchedValues) {
                if (matchValue == null || matchValue.trim().length() < 1) {
                    continue;
                }

                if (matchValue.contains("TO")) {
                    String[] vs = matchValue.split("#");
                    if (vs.length < 3) {
                        continue;
                    }
                    String bv = vs[0];
                    String ev = vs[2];

                    queryStr.append(fieldName + " : " + "[" + bv + " TO " + ev + "]" + " OR ");

                } else {

                    queryStr.append(fieldName + " : " + matchValue + " OR ");

                }

            }
            if (queryStr.toString().contains("OR")) {
                int or = queryStr.lastIndexOf("OR");
                queryStr.replace(or, or + 2, "");
            }

        }
        queryStr.append(" )");
        if (queryStr.length() < 5) {
            return null;
        }

        return queryStr.toString();

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
        new AdvancedSolrQueryGenerator().generateFinalQuery(finalQuery_);

    }
}
