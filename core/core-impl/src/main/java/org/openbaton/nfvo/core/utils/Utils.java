package org.openbaton.nfvo.core.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by mob on 12.05.17.
 */
public class Utils {

    public static Map<String, Object> getMapFromYamlFile(byte[] file ) throws IOException {
        if(file == null)
            throw new NullPointerException("File yaml is null");
        Map<String, Object> result;
        try (InputStream ios = new ByteArrayInputStream(file)) {
            Yaml yaml = new Yaml();
            result = (Map<String, Object>) yaml.load(ios);
        } catch (IOException e) {
            throw e;
        }
        return result;
    }
}
