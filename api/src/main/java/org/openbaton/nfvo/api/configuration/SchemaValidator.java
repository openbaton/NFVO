package org.openbaton.nfvo.api.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

public class SchemaValidator {

  public static JsonNode getJsonNodeFromStringContent(String content) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(content);
    return node;
  }

  public static JsonNode getJsonNodeFromUrl(String url) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(new URL(url));
    return node;
  }

  public static JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  public static JsonSchema getJsonSchemaFromStringContent(String schemaContent) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(schemaContent);
    return schema;
  }

  public static JsonSchema getJsonSchemaFromUrl(String url) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(new URL(url));
    return schema;
  }

  public static JsonSchema getJsonSchemaFromJsonNode(JsonNode jsonNode) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(jsonNode);
    return schema;
  }

  public static Set<ValidationMessage> validateSchema(String jsonClassSchema, String jsonInstance)
      throws Exception {

    JsonSchema finalSchema = getJsonSchemaFromStringContent(jsonClassSchema);
    JsonNode node = getJsonNodeFromStringContent(jsonInstance);

    Set<ValidationMessage> errors = finalSchema.validate(node);
    return errors;
  }
}
