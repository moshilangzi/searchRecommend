/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.comm.sr.common.entity;

import java.io.Serializable;

/**
 * @author jasstion
 */
public class ServiceRule implements Serializable {
  private String serviceId = null;
  private String serviceName = null;

  public ServiceRule(String serviceId, String serviceName) {
    this.serviceId = serviceName;
    this.serviceId = serviceId;
  }

  public ServiceRule() {
    super();
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 17 * hash + (this.serviceId != null ? this.serviceId.hashCode() : 0);
    hash = 17 * hash + (this.serviceName != null ? this.serviceName.hashCode() : 0);
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
    final ServiceRule other = (ServiceRule) obj;
    if ((this.serviceId == null) ? (other.serviceId != null)
        : !this.serviceId.equals(other.serviceId)) {
      return false;
    }
    if ((this.serviceName == null) ? (other.serviceName != null)
        : !this.serviceName.equals(other.serviceName)) {
      return false;
    }
    return true;
  }

}
