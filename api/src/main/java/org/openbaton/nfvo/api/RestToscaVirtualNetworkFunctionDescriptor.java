/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement;
import org.openbaton.tosca.parser.TOSCAParser;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.openbaton.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by rvl on 26.08.16.
 */
@RestController
@RequestMapping("/api/v1/vnfd-tosca")
public class RestToscaVirtualNetworkFunctionDescriptor {

  @Autowired private VirtualNetworkFunctionManagement vnfdManagement;
  @Autowired private TOSCAParser toscaParser;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.ACCEPTED)
  private VirtualNetworkFunctionDescriptor postTosca(
      @RequestBody String vnfd_yaml, @RequestHeader(value = "project-id") String projectId)
      throws NetworkServiceIntegrityException, BadFormatException, NotFoundException,
          CyclicDependenciesException {

    VNFDTemplate vnfdTemplate = Utils.stringToVNFDTemplate(vnfd_yaml);
    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);

    return vnfdManagement.add(vnfd, projectId);
  }
}
