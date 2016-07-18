/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.api;

import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.nfvo.core.interfaces.VNFManagerManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vnfmanagers")
public class RestVNFManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFManagerManagement vnfManagerManagement;

  @RequestMapping(method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public Iterable<VnfmManagerEndpoint> findAll() {
    return vnfManagerManagement.query();
  }

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public VnfmManagerEndpoint findById(@PathVariable("id") String id) {
    return vnfManagerManagement.query(id);
  }
}
