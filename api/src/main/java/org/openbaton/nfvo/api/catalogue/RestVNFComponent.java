/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.catalogue;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.nfvo.core.interfaces.VNFComponentManagment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vnfcomponents")
public class RestVNFComponent {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFComponentManagment vnfComponentManagment;

  @RequestMapping(method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public Iterable<VNFComponent> findAll() {
    return vnfComponentManagment.query();
  }

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public VNFComponent findById(@PathVariable("id") String id) {
    return vnfComponentManagment.query(id);
  }
}
