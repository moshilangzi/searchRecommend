/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.entity;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 *
 * @author jasstion
 */
public class EsCommonQuery extends CommonQuery {
    
    
    private String index = null;
    private String type = null;
    private String[] routings=null;






    /* elasticsearch script score string */
    private String scoreScript=null;
    private String scriptLangType=null;
    private String script=null;

    private Map<String,Object> scriptParams=Maps.newHashMap();

    public String getScoreScript() {
        return scoreScript;
    }

    public void setScoreScript(String scoreScript) {
        this.scoreScript = scoreScript;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EsCommonQuery() {
        super();
    }

    public EsCommonQuery( int pageNum, int pageSize, List<SortItem> sortItems, List<String> fls, String index, String type) {
        super( pageNum, pageSize, sortItems, fls);
        this.index = index;
        this.type = type;

    }

    public String getScriptLangType() {
        return scriptLangType;
    }

    public void setScriptLangType(String scriptLangType) {
        this.scriptLangType = scriptLangType;
    }

    public Map<String, Object> getScriptParams() {
        return scriptParams;
    }

    public void setScriptParams(Map<String, Object> scriptParams) {
        this.scriptParams = scriptParams;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String[] getRoutings() {
        return routings;
    }

    public void setRoutings(String[] routings) {
        this.routings = routings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EsCommonQuery that = (EsCommonQuery) o;

        if (index != null ? !index.equals(that.index) : that.index != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return !(scoreScript != null ? !scoreScript.equals(that.scoreScript) : that.scoreScript != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (index != null ? index.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (scoreScript != null ? scoreScript.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EsCommonQuery{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", scoreScript='" + scoreScript + '\'' +
                "} " + super.toString();
    }
}
