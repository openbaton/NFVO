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

package org.openbaton.nfvo.vnfm_reg;

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmRegister implements org.openbaton.vnfm.interfaces.register.VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VnfmEndpointRepository vnfmManagerEndpointRepository;

    @Override
    public Iterable<VnfmManagerEndpoint> listVnfm() {
        return this.vnfmManagerEndpointRepository.findAll();
    }


    protected void register(String type, String endpoint, EndpointType endpointType) {
        this.vnfmManagerEndpointRepository.save(new VnfmManagerEndpoint(type, endpoint, endpointType));
    }

    protected void register(VnfmManagerEndpoint endpoint) {
        log.debug("Perisisting: " + endpoint);
        this.vnfmManagerEndpointRepository.save(endpoint);
    }

    @Override
    public void addManagerEndpoint(VnfmManagerEndpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    public void removeManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VnfmManagerEndpoint getVnfm(String endpoint) throws NotFoundException {
        log.trace("Looking for vnfmEndpoint endpoint: " + endpoint);
        for (VnfmManagerEndpoint vnfmManagerEndpoint : this.vnfmManagerEndpointRepository.findAll()) {
            log.trace("" + vnfmManagerEndpoint);
            if (vnfmManagerEndpoint.getType().equals(endpoint)) {
                return vnfmManagerEndpoint;
            }
        }
        throw new NotFoundException("VnfManager of endpoint " + endpoint + " is not registered");
    }

    public void unregister(VnfmManagerEndpoint endpoint) {
        Iterable<VnfmManagerEndpoint> vnfmManagerEndpoints = vnfmManagerEndpointRepository.findAll();
        for (VnfmManagerEndpoint vnfmManagerEndpoint : vnfmManagerEndpoints) {
            if (vnfmManagerEndpoint.getEndpoint() != null && vnfmManagerEndpoint.getEndpoint().equals(endpoint.getEndpoint()) && vnfmManagerEndpoint.getEndpointType().equals(endpoint.getEndpointType()) && vnfmManagerEndpoint.getType() != null && vnfmManagerEndpoint.getType().equals(endpoint.getType())) {
                this.vnfmManagerEndpointRepository.delete(endpoint);
                return;
            }
        }
        log.error("no VNFM found for endpoint: " + endpoint);
    }
}
