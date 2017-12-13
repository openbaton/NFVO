package org.openbaton.nfvo.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
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
}
