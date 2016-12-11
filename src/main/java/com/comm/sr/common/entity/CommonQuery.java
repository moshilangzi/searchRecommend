/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.entity;

import java.util.List;

/**
 * meet most of baihe query. 仅仅支持f1:v1 AND (f2:v2 OR f2:V3) AND....
 * 不支持不同字段查询组合成一个子查询语句， 例如： (f1:v1 OR f2:v2) AND f3:v3 如果有更复杂的查询语句可以考虑使用
 * AdvancedQuery class
 *
 * @author jasstion
 */
public class CommonQuery extends AbstractQuery {
    protected String clusterIdentity=null;

    protected String queryStr=null;

    protected SubQuery subQuery=new SubQuery();


    //经维度，  两个数值，逗号隔开， 默认字段: longitudeDimension
    private String locationPoint = null;
    //km
    private Double distance = null;

    public CommonQuery() {
        super();
    }

    public CommonQuery(int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls) {
        super(pageNum, pageSize, sortItems, fls);

    }

    public SubQuery getSubQuery() {
        return subQuery;
    }

    public void setSubQuery(SubQuery subQuery) {
        this.subQuery = subQuery;
    }






    @Override
    public String toString() {
        return "CommonQuery{" +
                "distance=" + distance +
                ", subQuery=" + subQuery +
            ", queryStr=" + queryStr +
                ", locationPoint='" + locationPoint + '\'' +
                "} " + super.toString();
    }

    public String getClusterIdentity() {
        return clusterIdentity;
    }

    public void setClusterIdentity(String clusterIdentity) {
        this.clusterIdentity = clusterIdentity;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonQuery)) return false;

        CommonQuery that = (CommonQuery) o;

        if (getClusterIdentity() != null ?
            !getClusterIdentity().equals(that.getClusterIdentity()) :
            that.getClusterIdentity() != null) return false;
        if (getQueryStr() != null ?
            !getQueryStr().equals(that.getQueryStr()) :
            that.getQueryStr() != null) return false;
        if (getSubQuery() != null ?
            !getSubQuery().equals(that.getSubQuery()) :
            that.getSubQuery() != null) return false;
        if (getLocationPoint() != null ?
            !getLocationPoint().equals(that.getLocationPoint()) :
            that.getLocationPoint() != null) return false;
        return !(getDistance() != null ?
            !getDistance().equals(that.getDistance()) :
            that.getDistance() != null);

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.pageNum;
        hash = 37 * hash + this.pageSize;
        hash = 37 * hash + (this.fls != null ? this.fls.hashCode() : 0);
        hash = 37 * hash + (this.queryStr != null ? this.queryStr.hashCode() : 0);
        StringBuffer sb = new StringBuffer();
        for (SortItem sortItem : sortItems) {
            sb.append(sortItem.fieldName);
            sb.append(sortItem.sort);
        }
        hash = 37 * hash + sb.toString().hashCode();
        return hash;
    }

    public String getLocationPoint() {
        return locationPoint;
    }

    public void setLocationPoint(String locationPoint) {
        this.locationPoint = locationPoint;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }
}
