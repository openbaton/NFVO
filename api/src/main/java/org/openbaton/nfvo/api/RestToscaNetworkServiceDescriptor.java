package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.tosca.parser.TOSCAParser;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by rvl on 25.08.16.
 */
@RestController
@RequestMapping("/api/v1/nsd-tosca")
public class RestToscaNetworkServiceDescriptor {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
  @Autowired private TOSCAParser toscaParser;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.ACCEPTED)
  private NetworkServiceDescriptor postTosca(
      @RequestBody String nsd_yaml, @RequestHeader(value = "project-id") String projectId)
      throws NetworkServiceIntegrityException, BadFormatException, NotFoundException,
          CyclicDependenciesException {

    NSDTemplate nsdTemplate = Utils.stringToNSDTemplate(nsd_yaml);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    return networkServiceDescriptorManagement.onboard(nsd, projectId);
  }
}
