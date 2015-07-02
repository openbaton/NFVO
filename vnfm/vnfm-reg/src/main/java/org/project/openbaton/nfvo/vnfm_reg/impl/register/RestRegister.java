package org.project.openbaton.nfvo.vnfm_reg.impl.register;

import org.project.openbaton.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lto on 27/05/15.
 */
@RestController
@RequestMapping("/vnfm_reg-register")
public class RestRegister extends VnfmRegister {

    @Override
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addManagerEndpoint(VnfmManagerEndpoint endpoint) {
        this.register(endpoint);
    }
}
