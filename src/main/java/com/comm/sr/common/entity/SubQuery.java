/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.entity;

import com.comm.sr.common.utils.GsonHelper;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jasstion
 */
public class SubQuery implements Serializable {

    //AND or OR or NOT
    private String logic = null;
    private List<SubQuery> subQuerys =null;
    private QueryItem queryItem=null;

    public QueryItem getQueryItem() {
        return queryItem;
    }

    public void setQueryItem(QueryItem queryItem) {
        this.queryItem = queryItem;
    }

    public SubQuery() {
        super();
    }

    public SubQuery(String logic, QueryItem queryItem, List<SubQuery> subQuerys) {

        this.logic = logic;
        this.queryItem = queryItem;
        this.subQuerys = subQuerys;
    }

    public SubQuery(String logic, QueryItem queryItem) {
        this.logic = logic;
        this.queryItem = queryItem;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public List<SubQuery> getSubQuerys() {
        return subQuerys;
    }

    public void setSubQuerys(List<SubQuery> subQuerys) {
        this.subQuerys = subQuerys;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubQuery subQuery = (SubQuery) o;

        if (logic != null ? !logic.equals(subQuery.logic) : subQuery.logic != null) return false;
        if (subQuerys != null ? !subQuerys.equals(subQuery.subQuerys) : subQuery.subQuerys != null) return false;
        return !(queryItem != null ? !queryItem.equals(subQuery.queryItem) : subQuery.queryItem != null);

    }

    @Override
    public int hashCode() {
        int result = logic != null ? logic.hashCode() : 0;
        result = 31 * result + (subQuerys != null ? subQuerys.hashCode() : 0);
        result = 31 * result + (queryItem != null ? queryItem.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubQuery{" +
                "logic='" + logic + '\'' +
                ", subQuerys=" + subQuerys +
                ", queryItem=" + queryItem +
                '}';
    }

    public static void main(String[] args) {
        SubQuery finalQuery=new SubQuery();
        finalQuery.setLogic("AND");
        List<SubQuery> subQueries=Lists.newArrayList();
        SubQuery ageQuery = new SubQuery();
        QueryItem age=new QueryItem();
        age.setFieldName("age");
        age.setMatchedValues(Lists.newArrayList("1988"));
        ageQuery.setQueryItem(age);
        subQueries.add(ageQuery);
        finalQuery.setSubQuerys(subQueries);
        System.out.print(GsonHelper.objToJson(finalQuery));



    }

}
