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
import java.util.ArrayList;

/**
 * Created by rvl on 29.08.16.
 */
@RestController
public class RestCSAR {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFPackageManagement vnfPackageManagement;

  @RequestMapping(value = "/api/v1/csar-nsd", method = RequestMethod.POST)
  @ResponseBody
  public String onboardNSD(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception {

    CSARParser csarParser = new CSARParser();

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      ArrayList<ByteArrayOutputStream> byteArrayList = csarParser.parseNSDCSARFromByte(bytes);
      String output = "";

      for (ByteArrayOutputStream byteArray : byteArrayList) {
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
            vnfPackageManagement.onboard(byteArray.toByteArray(), projectId);
        output +=
            "{ \"id\": \""
                + virtualNetworkFunctionDescriptor.getVnfPackageLocation()
                + "\"}"
                + "\n";
      }

      return output;
    } else throw new IOException("File is empty!");
  }

  @RequestMapping(value = "/api/v1/csar-vnfd", method = RequestMethod.POST)
  @ResponseBody
  public String onboardVNFD(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception {

    CSARParser csarParser = new CSARParser();

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      ByteArrayOutputStream byteArray = csarParser.parseVNFDCSARFromByte(bytes);

      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
          vnfPackageManagement.onboard(byteArray.toByteArray(), projectId);

      return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }
}
