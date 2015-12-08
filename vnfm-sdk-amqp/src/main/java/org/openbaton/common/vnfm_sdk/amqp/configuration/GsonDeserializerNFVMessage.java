package org.openbaton.common.vnfm_sdk.amqp.configuration;

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
    public NFVMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String action = json.getAsJsonObject().get("action").getAsString();
        NFVMessage result;
        switch (action){
            case "INSTANTIATE":

                result = gson.fromJson(json, OrVnfmInstantiateMessage.class);
                break;
            case "SCALING":
                result = gson.fromJson(json, OrVnfmScalingMessage.class);
                break;
            case "HEAL":
                result = gson.fromJson(json, OrVnfmHealVNFRequestMessage.class);
                break;
            case "ERROR":
                result = gson.fromJson(json, OrVnfmErrorMessage.class);
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
