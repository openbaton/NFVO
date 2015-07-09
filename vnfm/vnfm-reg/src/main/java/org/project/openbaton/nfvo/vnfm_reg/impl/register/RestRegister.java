package org.project.openbaton.nfvo.vnfm_reg.impl.register;

import org.project.openbaton.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
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
@RequestMapping("/admin/v1/vnfm_reg-register")
public class RestRegister extends VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addManagerEndpoint(@RequestBody @Valid VnfmManagerEndpoint endpoint) {
        log.debug("Received: " + endpoint);
        this.register(endpoint);
    }
}
