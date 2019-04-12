/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
