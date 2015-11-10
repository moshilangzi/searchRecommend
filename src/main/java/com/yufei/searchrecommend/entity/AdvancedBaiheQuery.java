/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yufei.searchrecommend.entity;

import com.yufei.searchrecommend.entity.AbstractQuery;
import com.yufei.searchrecommend.entity.SubQuery;
import java.util.List;

/**
 *
 * @author jasstion
 */
public class AdvancedBaiheQuery extends AbstractQuery {

    private SubQuery subQuery = null;

    public AdvancedBaiheQuery(int pageNum, int pageSize) {
        super(pageNum, pageSize);
    }

    public AdvancedBaiheQuery() {
        super();
    }

    public AdvancedBaiheQuery(int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls) {
        super(pageNum, pageSize, sortItems, fls);
    }

    @Override
    public String toString() {
        return "AdvancedBaiheQuery{" + "subQuery=" + subQuery + '}';
    }

    public SubQuery getSubQuery() {
        return subQuery;
    }

    public void setSubQuery(SubQuery subQuery) {
        this.subQuery = subQuery;
    }

}
