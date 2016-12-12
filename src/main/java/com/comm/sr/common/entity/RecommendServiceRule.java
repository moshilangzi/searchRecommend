/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.comm.sr.common.entity;

/**
 * @author jasstion
 */
public class RecommendServiceRule extends SearchServiceRule {

  private boolean ifWriteIntoUserRelations = false;
  private boolean ifDuplicateFromUserRelations = false;

  public RecommendServiceRule() {
    super();
  }

  public RecommendServiceRule(String appId, String appName, String filter, String sort,
      String cacheStrstegy, boolean ifWriteIntoUserRelations,
      boolean ifDuplicateFromUserRelations) {
    super(appId, appName, filter, sort, cacheStrstegy);
    this.ifDuplicateFromUserRelations = ifDuplicateFromUserRelations;
    this.ifWriteIntoUserRelations = ifWriteIntoUserRelations;
  }

  public boolean isIfWriteIntoUserRelations() {
    return ifWriteIntoUserRelations;
  }

  public void setIfWriteIntoUserRelations(boolean ifWriteIntoUserRelations) {
    this.ifWriteIntoUserRelations = ifWriteIntoUserRelations;
  }

  public boolean isIfDuplicateFromUserRelations() {
    return ifDuplicateFromUserRelations;
  }

  public void setIfDuplicateFromUserRelations(boolean ifDuplicateFromUserRelations) {
    this.ifDuplicateFromUserRelations = ifDuplicateFromUserRelations;
  }

}
