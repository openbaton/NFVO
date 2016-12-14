package org.openbaton.nfvo.security.authorization.utils;

import org.openbaton.exceptions.PasswordWeakException;

/** Created by gca on 14/12/16. */
public class Utils {

  public static void checkPasswordIntegrity(String password) throws PasswordWeakException {
    if (password.length() < 8
        || !(password.matches("(?=.*[A-Z]).*")
            && password.matches("(?=.*[a-z]).*")
            && password.matches("(?=.*[0-9]).*"))) {
      throw new PasswordWeakException(
          "The chosen password is too weak. Password must be at least 8 chars and contain one lower case letter, "
              + "one "
              + "upper case letter and one digit");
    }
  }
}
