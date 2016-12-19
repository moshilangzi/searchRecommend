/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.entity;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jasstion
 */
public class AbstractQuery implements Serializable {


    protected int pageNum = -1;
    protected int pageSize = 18;
    protected int offset =-1;
    protected int  limit=0;

    protected List<SortItem> sortItems = Lists.newArrayList();
    protected List<String> fls = Lists.newArrayList();
    protected String cacheStrategy=null;
    

    public AbstractQuery(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public AbstractQuery() {
        super();
    }

    public AbstractQuery(int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.sortItems = sortItems;
        this.fls = fls;

    }
    
    public String getCacheStrategy() {
        return cacheStrategy;
    }

    public void setCacheStrategy(String cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }
    
    public List<String> getFls() {
        return fls;
    }




    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<SortItem> getSortItems() {
        return sortItems;
    }

    public void setFls(List<String> fls) {
        this.fls = fls;
    }


    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setSortItems(List<SortItem> sortItems) {
        this.sortItems = sortItems;
    }

    

    
    
    
}
