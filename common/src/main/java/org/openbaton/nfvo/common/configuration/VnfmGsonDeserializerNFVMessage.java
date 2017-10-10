package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGrantLifecycleOperationMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmLogMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmScalingMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmStartStopMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VnfmGsonDeserializerNFVMessage implements JsonDeserializer<NFVMessage> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public NFVMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String action = json.getAsJsonObject().get("action").getAsString();
    NFVMessage result;
    switch (action) {
      case "INSTANTIATE":
        result = gson.fromJson(json, OrVnfmInstantiateMessage.class);
        break;
      case "GRANT_OPERATION":
        result = gson.fromJson(json, OrVnfmGrantLifecycleOperationMessage.class);
        break;
      case "SCALING":
        result = gson.fromJson(json, OrVnfmScalingMessage.class);
        break;
      case "SCALE_OUT":
        result = gson.fromJson(json, OrVnfmScalingMessage.class);
        break;
      case "SCALE_IN":
        result = gson.fromJson(json, OrVnfmScalingMessage.class);
        break;
      case "HEAL":
        result = gson.fromJson(json, OrVnfmHealVNFRequestMessage.class);
        break;
      case "UPDATE":
        result = gson.fromJson(json, OrVnfmUpdateMessage.class);
        break;
      case "START":
        result = gson.fromJson(json, OrVnfmStartStopMessage.class);
        break;
      case "STOP":
        result = gson.fromJson(json, OrVnfmStartStopMessage.class);
        break;
      case "ERROR":
        result = gson.fromJson(json, OrVnfmErrorMessage.class);
        break;
      case "LOG_REQUEST":
        result = gson.fromJson(json, OrVnfmLogMessage.class);
        break;
      default:
        result = gson.fromJson(json, OrVnfmGenericMessage.class);
        break;
    }
    result.setAction(Action.valueOf(action));
    log.trace("Deserialized message is " + result);
    return result;
  }
}
