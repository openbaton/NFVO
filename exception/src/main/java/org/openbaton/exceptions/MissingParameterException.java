package org.openbaton.exceptions;

/**
 * Created by lto on 26/11/15.
 */
public class MissingParameterException extends Exception {
  public MissingParameterException(Throwable cause) {
    super(cause);
  }

  public MissingParameterException(String message) {
    super(message);
  }

  public MissingParameterException(String message, Throwable cause) {
    super(message, cause);
  }
}
