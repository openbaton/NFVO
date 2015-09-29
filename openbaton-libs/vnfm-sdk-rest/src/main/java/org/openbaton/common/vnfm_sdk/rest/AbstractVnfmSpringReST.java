package org.openbaton.common.vnfm_sdk.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

//import javax.validation.Valid;

/**
 * Created by lto on 08/07/15.
 */
@SpringBootApplication
@RestController
public abstract class AbstractVnfmSpringReST extends AbstractVnfm {

    private VnfmRestHelper vnfmRestHelper;

    @Override
    protected void setup() {
        this.vnfmRestHelper = (VnfmRestHelper) vnfmHelper;
        super.setup();
    }

    @Override
    protected void unregister(){
        vnfmRestHelper.unregister(vnfmManagerEndpoint);
    }

    @Override
    protected void register(){
        vnfmRestHelper.register(vnfmManagerEndpoint);
    }

    @RequestMapping(value = "/core-dummy-actions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestBody /*@Valid*/ String jsonNfvMessage) {
        log.debug("Received: " + jsonNfvMessage);
        NFVMessage message;

        JsonElement action = vnfmRestHelper.getMapper().fromJson(jsonNfvMessage, JsonObject.class).get("action");
        log.debug("json Action is: " + action.getAsString());
        if (action.getAsString().equals("INSTANTIATE"))
            message = vnfmRestHelper.getMapper().fromJson(jsonNfvMessage,OrVnfmInstantiateMessage.class);
        else
            message = vnfmRestHelper.getMapper().fromJson(jsonNfvMessage,OrVnfmGenericMessage.class);
        try {
            this.onAction(message);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BadFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
