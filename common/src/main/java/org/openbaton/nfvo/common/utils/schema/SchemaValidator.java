package org.openbaton.nfvo.common.utils.schema;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.util.Set;
import org.openbaton.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaValidator {

  private static Logger log = LoggerFactory.getLogger(SchemaValidator.class);

  private static JsonNode getJsonNodeFromStringContent(String content)
      throws BadRequestException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node;
    try {
      node = mapper.readTree(content);
    } catch (JsonParseException e) {
      log.error(e.getMessage());
      throw new BadRequestException(e);
    }
    return node;
  }

  private static JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    return factory.getSchema(schemaContent);
  }

  public static Set<ValidationMessage> validateSchema(String jsonClassSchema, String jsonInstance)
      throws BadRequestException, IOException {

    JsonSchema finalSchema = getJsonSchemaFromStringContent(jsonClassSchema);
    JsonNode node = getJsonNodeFromStringContent(jsonInstance);

    return finalSchema.validate(node);
  }
}
