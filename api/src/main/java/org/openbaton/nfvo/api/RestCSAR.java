package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.openbaton.tosca.parser.CSARParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by rvl on 29.08.16.
 */
@RestController
@RequestMapping("/api/v1/csar-nsd")
public class RestCSAR {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFPackageManagement vnfPackageManagement;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public String onboard(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception {

    CSARParser csarParser = new CSARParser();

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      ByteArrayOutputStream byteArray = csarParser.parseNSDCSARFromByte(bytes);

      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
          vnfPackageManagement.onboard(byteArray.toByteArray(), projectId);

      return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }
}
