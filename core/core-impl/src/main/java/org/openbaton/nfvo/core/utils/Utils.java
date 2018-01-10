package org.openbaton.nfvo.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.openbaton.exceptions.PasswordWeakException;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings({"unsafe", "unchecked"})
public class Utils {

  public static Map<String, Object> getMapFromYamlFile(byte[] file) throws IOException {
    if (file == null) throw new NullPointerException("File yaml is null");
    Map<String, Object> result;
    try (InputStream ios = new ByteArrayInputStream(file)) {
      Yaml yaml = new Yaml();
      result = (Map<String, Object>) yaml.load(ios);
    }
    return result;
  }

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
