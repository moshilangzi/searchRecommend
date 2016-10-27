package com.comm.searchrecommend.entity;

import java.io.Serializable;

/**
 * Created by jasstion on 27/10/2016.
 */
public class ThreadShardEntity implements Serializable{

    private String searchId=null;

    public ThreadShardEntity(String searchId) {
        this.searchId = searchId;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

}
