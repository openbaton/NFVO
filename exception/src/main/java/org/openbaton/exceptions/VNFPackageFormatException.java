package org.openbaton.exceptions;

/** Created by mob on 05/04/2017. */
public class VNFPackageFormatException extends Exception {

  public VNFPackageFormatException() {}

  public VNFPackageFormatException(String message) {
    super(message);
  }

  public VNFPackageFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public VNFPackageFormatException(Throwable cause) {
    super(cause);
  }
}
