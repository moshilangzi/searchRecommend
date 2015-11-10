/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.solr;

import com.yufei.searchrecommend.entity.BaiheQuery;
import com.yufei.searchrecommend.entity.QueryItem;
import com.yufei.searchrecommend.entity.SortItem;
import com.yufei.searchrecommend.service.QueryGenerator;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jasstion
 */
public class SolrQueryGenerator implements QueryGenerator<SolrQuery, BaiheQuery> {

    protected final static Logger LOGGER = LoggerFactory.getLogger(SolrQueryGenerator.class);

    @Override
    public SolrQuery generateFinalQuery(BaiheQuery query) {
        SolrQuery solrQuery = new SolrQuery();
        StringBuilder queryStr = new StringBuilder();
        StringBuilder filterQueryStr = new StringBuilder();

        int pageNum = query.getPageNum();
        if (pageNum < 1) {
            //default first page
            pageNum = 1;
        } else {
            pageNum = query.getPageNum();
        }
        List<QueryItem> queryItems = query.getQueryItems();
        if (queryItems == null) {
            queryItems = Lists.newArrayList();
        }

        List<SortItem> sortItems = query.getSortItems();
        if (sortItems == null) {
            sortItems = Lists.newArrayList();
        }
        for (QueryItem queryItem : queryItems) {
            if (queryItem.isIsFilterType()) {
                String temp = generateQueryStrFromQueryItem(queryItem);
                if (temp != null) {
                    filterQueryStr.append(temp + " AND ");

                }
            }
            if (!queryItem.isIsFilterType()) {
                String temp = generateQueryStrFromQueryItem(queryItem);
                if (temp != null) {
                    queryStr.append(temp + " AND ");

                }
            }

        }
        for (String functionQueryString : query.getFunctionQuerysList()) {

            queryStr.append("_query_:\"{!func}" + functionQueryString + "\"" + " AND ");

        }

        //delete last AND
        if (queryStr.toString().contains("AND")) {

            int and = queryStr.lastIndexOf("AND");
            queryStr.replace(and, and + 3, "");
        }
        if (filterQueryStr.toString().contains("AND")) {
            int and = filterQueryStr.lastIndexOf("AND");
            filterQueryStr.replace(and, and + 3, "");
        }
        for (SortItem sortItem : sortItems) {
            String fieldName = sortItem.getFieldName();
            String order = sortItem.getSort();

            if (order == null) {
                LOGGER.info("ingore sort fieldName:" + fieldName + ", please configure it.");
                continue;
            }
            if (order.trim().equals("asc")) {
                solrQuery.setSort(fieldName, SolrQuery.ORDER.asc);
            }
            if (order.trim().equals("desc")) {
                solrQuery.addSort(fieldName, SolrQuery.ORDER.desc);

            }

        }

        solrQuery.setQuery(queryStr.toString());
        solrQuery.setFilterQueries(filterQueryStr.toString());
        LOGGER.debug("final queryStr is:" + queryStr.toString() + "");
        LOGGER.debug("filter queryStr is:" + filterQueryStr.toString() + "");
//        //only response userID,score
//        solrQuery.setFields("userID","score");
        for (String fl : query.getFls()) {
            solrQuery.addField(fl);

        }
        int pageSize = query.getPageSize();
        solrQuery.add("start", String.valueOf((pageNum - 1) * pageSize));
        solrQuery.add("rows", String.valueOf(pageSize));
        return solrQuery;
    }

    private String generateQueryStrFromQueryItem(QueryItem queryItem) {
        StringBuilder queryStr = new StringBuilder();

        String fieldName = queryItem.getFieldName();
        boolean ifNotEqual = false;
        if (fieldName.contains("--")) {
            ifNotEqual = true;
            fieldName = fieldName.replace("--", "");

        }
        if (ifNotEqual) {
            queryStr.append("-");
        }
        queryStr.append("( ");

        List<String> matchValues = queryItem.getMatchedValues();
        if (matchValues.size() > 0) {
            for (String matchValue : matchValues) {
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

}
