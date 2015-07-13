package org.project.openbaton.nfvo.vnfm_reg.impl.register;

import org.project.openbaton.common.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by lto on 27/05/15.
 */
@RestController
@RequestMapping("/admin/v1")
public class RestRegister extends VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @RequestMapping(value = "/vnfm-register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addManagerEndpoint(@RequestBody @Valid VnfmManagerEndpoint endpoint) {
        log.debug("Received: " + endpoint);
        this.register(endpoint);
    }

    @Override
    @RequestMapping(value = "/vnfm-unregister", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeManagerEndpoint(@RequestBody @Valid VnfmManagerEndpoint endpoint) {
        log.debug("Unregistering endpoint: " + endpoint);
        this.unregister(endpoint);
    }
}
