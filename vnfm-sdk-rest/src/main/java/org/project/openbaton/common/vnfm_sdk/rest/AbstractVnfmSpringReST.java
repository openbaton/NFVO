package org.project.openbaton.common.vnfm_sdk.rest;

import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
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
@RequestMapping("/core-vnfm-actions")
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

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestBody /*@Valid*/ NFVMessage message) {
        log.debug("Received: " + message);
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
