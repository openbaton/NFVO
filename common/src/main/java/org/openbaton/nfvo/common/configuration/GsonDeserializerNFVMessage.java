package org.openbaton.nfvo.common.configuration;

import com.google.gson.*;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * Created by lto on 10/11/15.
 */
@Service
public class GsonDeserializerNFVMessage implements JsonDeserializer<NFVMessage> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public NFVMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String action = json.getAsJsonObject().get("action").getAsString();
    NFVMessage result;
    switch (action) {
      case "ALLOCATE_RESOURCES":
        result = gson.fromJson(json, VnfmOrAllocateResourcesMessage.class);
        break;
      case "ERROR":
        result = gson.fromJson(json, VnfmOrErrorMessage.class);
        break;
      case "INSTANTIATE":
        log.trace("gson is: " + gson);
        result = gson.fromJson(json, VnfmOrInstantiateMessage.class);
        break;
      case "SCALED":
        result = gson.fromJson(json, VnfmOrScaledMessage.class);
        break;
      case "SCALING":
        result = gson.fromJson(json, VnfmOrScalingMessage.class);
        break;
      case "HEAL":
        result = gson.fromJson(json, VnfmOrHealedMessage.class);
        break;
      default:
        result = gson.fromJson(json, VnfmOrGenericMessage.class);
        break;
    }
    result.setAction(Action.valueOf(action));
    log.trace("Deserialized message is " + result);
    return result;
  }
}
