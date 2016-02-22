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

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 27/05/15.
 */
@Service
public class RabbitRegister extends VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VnfmEndpointRepository vnfmEndpointRepositry;


    @Override
    public void addManagerEndpoint(String endpoint_json) {

        VnfmManagerEndpoint endpoint = gson.fromJson(endpoint_json, VnfmManagerEndpoint.class);

        if (endpoint.getEndpointType() == null) {
            endpoint.setEndpointType(EndpointType.RABBIT);
        }
        log.info("Registering endpoint of type: " + endpoint.getType());
        try {
            this.register(endpoint);
        } catch (AlreadyExistingException e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    @Override
    public void removeManagerEndpoint(String endpoint_json) {

        VnfmManagerEndpoint endpoint = gson.fromJson(endpoint_json, VnfmManagerEndpoint.class);
        log.debug("Unregistering: " + endpoint);
        for (VnfmManagerEndpoint vnfmManagerEndpoint : vnfmEndpointRepositry.findAll())
            if (vnfmManagerEndpoint.getEndpoint().equals(endpoint.getEndpoint())) {
                this.unregister(vnfmManagerEndpoint);
                break;
            }
    }
}
