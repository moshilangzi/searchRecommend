/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.entity;

import com.yufei.searchrecommend.utils.Instances;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jasstion
 */
public class SubQuery implements Serializable {

    //AND or OR
    private String logic = "AND";
    private List<SubQuery> subQuerys = Lists.newArrayList();
    private String fieldName = null;
    // range: * TO 100, 2015-07-06T08:52:48Z TO 2015-07-06T08:52:48Z,  disperse: 20, "married"...    -means not match following query
    private List<String> matchedValues = Lists.newArrayList();

    public SubQuery() {
        super();
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<String> getMatchedValues() {
        return matchedValues;
    }

    public void setMatchedValues(List<String> matchedValues) {
        this.matchedValues = matchedValues;
    }

    @Override
    public String toString() {
        return "SubQuery{" + "logic=" + logic + ", subQuerys=" + subQuerys + ", fieldName=" + fieldName + ", matchedValues=" + matchedValues + '}';
    }

    public static void main(String[] args) {
        SubQuery ageQuery = new SubQuery();
        ageQuery.setFieldName("age");
        ageQuery.setMatchedValues(Lists.newArrayList("1988"));
        SubQuery heightQuery = new SubQuery();
        heightQuery.setFieldName("height");
        heightQuery.setMatchedValues(Lists.newArrayList("180"));
        SubQuery finalQuery = new SubQuery();
        finalQuery.setLogic("OR");
        finalQuery.getSubQuerys().add(ageQuery);
        finalQuery.getSubQuerys().add(heightQuery);
        System.out.print(Instances.gson.toJson(finalQuery) + "\n");
        SubQuery sexQuery = new SubQuery();
        sexQuery.setFieldName("sex");
        sexQuery.setMatchedValues(Lists.newArrayList("1"));

        SubQuery finalQuery_ = new SubQuery();
        finalQuery_.setLogic("AND");
        finalQuery_.getSubQuerys().add(finalQuery);
        finalQuery_.getSubQuerys().add(sexQuery);
        System.out.print(Instances.gson.toJson(finalQuery_) + "\n");
        String json = "{\"logic\":\"OR\",\"subQuerys\":[{\"logic\":\"AND\",\"fieldName\":\"age\",\"matchedValues\":[\"1988\"]},{\"logic\":\"AND\",\"fieldName\":\"height\",\"matchedValues\":[\"180\"]}]}";
        SubQuery subQuery_final = Instances.gson.fromJson(json, SubQuery.class);
        System.out.print(subQuery_final.toString());
    }

}
