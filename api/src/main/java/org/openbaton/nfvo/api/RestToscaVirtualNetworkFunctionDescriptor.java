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
