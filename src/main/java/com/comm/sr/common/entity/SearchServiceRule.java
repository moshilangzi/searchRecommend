/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.entity;

/**
 *
 * @author jasstion
 */
public class SearchServiceRule extends ServiceRule {
    
    
    private String filter=null;
    private String sort=null;
    //number means how much time the key will be deleted
    private String cacheStrategy=null;

    public SearchServiceRule() {
        super();
    }
    
    public SearchServiceRule(String appId, String appName, String filter, String sort, String cacheStrstegy) {
        super(appId, appName);
        this.cacheStrategy=cacheStrstegy;
        this.filter=filter;
        this.sort=sort;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getCacheStrategy() {
        return cacheStrategy;
    }

    public void setCacheStrategy(String cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Override
    public String toString() {
        return "SearchServiceRule{" + "filter=" + filter + ", sort=" + sort + ", cacheStrategy=" + cacheStrategy + '}';
    }

    public SearchServiceRule(String appId, String appName) {
        super(appId, appName);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.filter != null ? this.filter.hashCode() : 0);
        hash = 97 * hash + (this.sort != null ? this.sort.hashCode() : 0);
        hash = 97 * hash + (this.cacheStrategy != null ? this.cacheStrategy.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchServiceRule other = (SearchServiceRule) obj;
        if ((this.filter == null) ? (other.filter != null) : !this.filter.equals(other.filter)) {
            return false;
        }
        if ((this.sort == null) ? (other.sort != null) : !this.sort.equals(other.sort)) {
            return false;
        }
        if ((this.cacheStrategy == null) ? (other.cacheStrategy != null) : !this.cacheStrategy.equals(other.cacheStrategy)) {
            return false;
        }
        return true;
    }
    
    
    

}
