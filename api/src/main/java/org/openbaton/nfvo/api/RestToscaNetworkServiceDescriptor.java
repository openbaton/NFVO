package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.tosca.parser.TOSCAParser;
import org.openbaton.tosca.templates.NSDTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Created by rvl on 25.08.16.
 */
@RestController
@RequestMapping("/api/v1/nsd-tosca")
public class RestToscaNetworkServiceDescriptor {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.ACCEPTED)
  private NetworkServiceDescriptor postTosca(
      @RequestBody String nsd_yaml, @RequestHeader(value = "project-id") String projectId)
      throws NetworkServiceIntegrityException, BadFormatException, NotFoundException,
          CyclicDependenciesException {

    log.debug(nsd_yaml.toString());

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(NSDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    NSDTemplate nsdTemplate = yaml.loadAs(nsd_yaml, NSDTemplate.class);
    log.debug(NSDTemplate.class.toString());

    TOSCAParser toscaParser = new TOSCAParser();
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    return networkServiceDescriptorManagement.onboard(nsd, projectId);
  }
}
