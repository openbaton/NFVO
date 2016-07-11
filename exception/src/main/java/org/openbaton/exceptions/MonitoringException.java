package org.openbaton.exceptions;

/**
 * Created by lto on 26/11/15.
 */
public class MonitoringException extends Exception {
  public MonitoringException(Throwable cause) {
    super(cause);
  }

  public MonitoringException(String message) {
    super(message);
  }

  public MonitoringException(String message, Throwable cause) {
    super(message, cause);
  }
}
