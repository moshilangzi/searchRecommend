package com.comm.sr.common.entity;

import java.io.Serializable;

/**
 * Created by jasstion on 17/11/2016.
 */
public class LogEvent implements Serializable {

  String eventId;
  String eventIn;
  String eventOut;
  String eventName;
  String eventSt;
  String eventFt;
  String eventHost;

  public LogEvent(String eventFt, String eventHost, String eventId, String eventIn,
      String eventName, String eventOut, String eventSt) {
    super();
    this.eventFt = eventFt;
    this.eventHost = eventHost;
    this.eventId = eventId;
    this.eventIn = eventIn;
    this.eventName = eventName;
    this.eventOut = eventOut;
    this.eventSt = eventSt;
  }

  public LogEvent() {
    super();
  }

  public String getEventFt() {
    return eventFt;
  }

  public void setEventFt(String eventFt) {
    this.eventFt = eventFt;
  }

  public String getEventHost() {
    return eventHost;
  }

  public void setEventHost(String eventHost) {
    this.eventHost = eventHost;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventIn() {
    return eventIn;
  }

  public void setEventIn(String eventIn) {
    this.eventIn = eventIn;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getEventOut() {
    return eventOut;
  }

  public void setEventOut(String eventOut) {
    this.eventOut = eventOut;
  }

  public String getEventSt() {
    return eventSt;
  }

  public void setEventSt(String eventSt) {
    this.eventSt = eventSt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LogEvent)) return false;

    LogEvent logEvent = (LogEvent) o;

    if (getEventId() != null ? !getEventId().equals(logEvent.getEventId())
        : logEvent.getEventId() != null)
      return false;
    if (getEventIn() != null ? !getEventIn().equals(logEvent.getEventIn())
        : logEvent.getEventIn() != null)
      return false;
    if (getEventOut() != null ? !getEventOut().equals(logEvent.getEventOut())
        : logEvent.getEventOut() != null)
      return false;
    if (getEventName() != null ? !getEventName().equals(logEvent.getEventName())
        : logEvent.getEventName() != null)
      return false;
    if (getEventSt() != null ? !getEventSt().equals(logEvent.getEventSt())
        : logEvent.getEventSt() != null)
      return false;
    if (getEventFt() != null ? !getEventFt().equals(logEvent.getEventFt())
        : logEvent.getEventFt() != null)
      return false;
    return !(getEventHost() != null ? !getEventHost().equals(logEvent.getEventHost())
        : logEvent.getEventHost() != null);

  }

  @Override
  public int hashCode() {
    int result = getEventId() != null ? getEventId().hashCode() : 0;
    result = 31 * result + (getEventIn() != null ? getEventIn().hashCode() : 0);
    result = 31 * result + (getEventOut() != null ? getEventOut().hashCode() : 0);
    result = 31 * result + (getEventName() != null ? getEventName().hashCode() : 0);
    result = 31 * result + (getEventSt() != null ? getEventSt().hashCode() : 0);
    result = 31 * result + (getEventFt() != null ? getEventFt().hashCode() : 0);
    result = 31 * result + (getEventHost() != null ? getEventHost().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "LogEvent{" + "eventFt='" + eventFt + '\'' + ", eventId='" + eventId + '\''
        + ", eventIn='" + eventIn + '\'' + ", eventOut='" + eventOut + '\'' + ", eventName='"
        + eventName + '\'' + ", eventSt='" + eventSt + '\'' + ", eventHost='" + eventHost + '\''
        + '}';
  }
}
