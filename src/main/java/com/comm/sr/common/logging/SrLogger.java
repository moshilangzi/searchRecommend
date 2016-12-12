package com.comm.sr.common.logging;

/**
 * Created by jasstion on 29/10/2016.
 */
public interface SrLogger {
  String getPrefix();

  String getName();

  /**
   * Returns the current logger level If the level is null, it means that the logger inherits its
   * level from its nearest ancestor with a specific (non-null) level value.
   * @return the logger level
   */
  String getLevel();

  /**
   * Allows to set the logger level If the new level is null, the logger will inherit its level from
   * its nearest ancestor with a specific (non-null) level value.
   * @param level the new level
   */
  void setLevel(String level);

  /**
   * Returns {@code true} if a TRACE level message is logged.
   */
  boolean isTraceEnabled();

  /**
   * Returns {@code true} if a DEBUG level message is logged.
   */
  boolean isDebugEnabled();

  /**
   * Returns {@code true} if an INFO level message is logged.
   */
  boolean isInfoEnabled();

  /**
   * Returns {@code true} if a WARN level message is logged.
   */
  boolean isWarnEnabled();

  /**
   * Returns {@code true} if an ERROR level message is logged.
   */
  boolean isErrorEnabled();

  /**
   * Logs a DEBUG level message.
   */
  void trace(String msg, Object... params);

  /**
   * Logs a DEBUG level message.
   */
  void trace(String msg, Throwable cause, Object... params);

  /**
   * Logs a DEBUG level message.
   */
  void debug(String msg, Object... params);

  /**
   * Logs a DEBUG level message.
   */
  void debug(String msg, Throwable cause, Object... params);

  /**
   * Logs an INFO level message.
   */
  void info(String msg, Object... params);

  /**
   * Logs an INFO level message.
   */
  void info(String msg, Throwable cause, Object... params);

  /**
   * Logs a WARN level message.
   */
  void warn(String msg, Object... params);

  /**
   * Logs a WARN level message.
   */
  void warn(String msg, Throwable cause, Object... params);

  /**
   * Logs an ERROR level message.
   */
  void error(String msg, Object... params);

  /**
   * Logs an ERROR level message.
   */
  void error(String msg, Throwable cause, Object... params);
}
