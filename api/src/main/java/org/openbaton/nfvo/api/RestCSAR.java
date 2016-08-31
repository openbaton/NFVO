package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.tosca.parser.CSARParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by rvl on 29.08.16.
 */
@RestController
public class RestCSAR {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
  @Autowired private CSARParser csarParser;

  @RequestMapping(value = "/api/v1/csar-nsd", method = RequestMethod.POST)
  @ResponseBody
  public NetworkServiceDescriptor onboardNSD(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception, BadFormatException {

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      NetworkServiceDescriptor nsd = csarParser.parseNSDCSARFromByte(bytes, projectId);

      return networkServiceDescriptorManagement.onboard(nsd, projectId);
    } else throw new IOException("File is empty!");
  }

  @RequestMapping(value = "/api/v1/csar-vnfd", method = RequestMethod.POST)
  @ResponseBody
  public String onboardVNFD(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception {

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      VirtualNetworkFunctionDescriptor vnfd = csarParser.parseVNFDCSARFromByte(bytes, projectId);

      return "{ \"id\": \"" + vnfd.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }
}
