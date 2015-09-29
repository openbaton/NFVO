/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.vnfm_reg.impl.register;

import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
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
