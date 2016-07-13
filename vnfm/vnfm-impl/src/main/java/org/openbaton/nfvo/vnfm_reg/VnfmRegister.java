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

import com.google.gson.Gson;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.AlreadyExistingException;
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

  @Autowired protected Gson gson;
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired protected VnfmEndpointRepository vnfmEndpointRepository;

  @Override
  public Iterable<VnfmManagerEndpoint> listVnfm() {
    return this.vnfmEndpointRepository.findAll();
  }

  protected void register(String type, String endpoint, EndpointType endpointType) {
    this.vnfmEndpointRepository.save(new VnfmManagerEndpoint(type, endpoint, endpointType));
  }

  protected void register(VnfmManagerEndpoint endpoint) throws AlreadyExistingException {
    log.debug("Perisisting: " + endpoint);
    for (VnfmManagerEndpoint endpointExisting : vnfmEndpointRepository.findAll()) {
      if (endpointExisting.getEndpoint().equals(endpoint.getEndpoint())
          && endpointExisting.getType().equals(endpoint.getType())
          && endpointExisting.getEndpointType().equals(endpoint.getEndpointType()))
        throw new AlreadyExistingException(
            "VnfmManagerEndpoint " + endpoint + " already exists in the DB");
    }
    endpoint.setActive(true);
    this.vnfmEndpointRepository.save(endpoint);
  }

  @Override
  public void addManagerEndpoint(String endpoint) {
    throw new UnsupportedOperationException();
  }

  public void removeManagerEndpoint(@Payload String endpoint) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VnfmManagerEndpoint getVnfm(String endpoint) throws NotFoundException {
    log.trace("Looking for vnfmEndpoint endpoint: " + endpoint);
    for (VnfmManagerEndpoint vnfmManagerEndpoint : this.vnfmEndpointRepository.findAll()) {
      log.trace("" + vnfmManagerEndpoint);
      if (vnfmManagerEndpoint.getType().equals(endpoint)) {
        return vnfmManagerEndpoint;
      }
    }
    throw new NotFoundException("VnfManager of endpoint " + endpoint + " is not registered");
  }

  protected void unregister(VnfmManagerEndpoint endpoint) {
    Iterable<VnfmManagerEndpoint> vnfmManagerEndpoints = vnfmEndpointRepository.findAll();
    for (VnfmManagerEndpoint vnfmManagerEndpoint : vnfmManagerEndpoints) {
      if (endpoint.getType().equals(vnfmManagerEndpoint.getType())) {
        log.info("Unregistered vnfm: " + endpoint.getType());
        this.vnfmEndpointRepository.delete(vnfmManagerEndpoint.getId());
        return;
      }
    }
    log.error("no VNFM found for endpoint: " + endpoint);
  }
}
