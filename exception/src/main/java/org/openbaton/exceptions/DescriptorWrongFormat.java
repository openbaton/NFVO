package org.openbaton.exceptions;

/** Created by rvl on 29.09.16. */
public class DescriptorWrongFormat extends Exception {

  public DescriptorWrongFormat(String message) {
    super(message);
  }

  public DescriptorWrongFormat(String message, Throwable cause) {
    super(message, cause);
  }
}
