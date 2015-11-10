/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.entity;

import com.google.common.collect.Lists;
import java.util.List;

/**
 * meet most of yufei query. 仅仅支持f1:v1 AND (f2:v2 OR f2:V3) AND....
 * 不支持不同字段查询组合成一个子查询语句， 例如： (f1:v1 OR f2:v2) AND f3:v3 如果有更复杂的查询语句可以考虑使用
 * AdvancedQuery class
 *
 * @author jasstion
 */
public class BaiheQuery extends AbstractQuery {

    protected List<QueryItem> queryItems = Lists.newArrayList();
    private List<String> functionQuerysList=Lists.newArrayList();

    public BaiheQuery() {
        super();
    }

    public BaiheQuery(List queryItems, int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls) {
        super(pageNum, pageSize, sortItems, fls);
        this.queryItems = queryItems;

    }

    public List<String> getFunctionQuerysList() {
        return functionQuerysList;
    }

    public void setFunctionQuerysList(List<String> functionQuerysList) {
        this.functionQuerysList = functionQuerysList;
    }
    

    public List<QueryItem> getQueryItems() {
        return queryItems;
    }

    public void setQueryItems(List<QueryItem> queryItems) {
        this.queryItems = queryItems;
    }

    @Override
    public String toString() {
        return "BaiheQuery{" + "gender=" + gender + ", queryItems=" + queryItems + ", pageNum=" + pageNum + ", pageSize=" + pageSize + ", sortItems=" + sortItems + ", fls=" + fls + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.gender;
        hash = 37 * hash + this.pageNum;
        hash = 37 * hash + this.pageSize;
        hash = 37 * hash + (this.fls != null ? this.fls.hashCode() : 0);
        hash = 37 * hash + (this.queryItems != null ? this.queryItems.hashCode() : 0);
        StringBuffer sb=new StringBuffer();
        for (SortItem sortItem : sortItems) {
            sb.append(sortItem.fieldName);
            sb.append(sortItem.sort);
        }
        hash=37*hash+sb.toString().hashCode();
        return hash;
    }

}
