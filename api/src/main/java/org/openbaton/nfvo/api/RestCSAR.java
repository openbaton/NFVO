package org.openbaton.nfvo.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.openbaton.tosca.parser.CSARParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by rvl on 29.08.16.
 */
@RestController
public class RestCSAR {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
  @Autowired private CSARParser csarParser;
  @Autowired private VNFPackageManagement vnfPackageManagement;

  @RequestMapping(value = "/api/v1/csar-nsd", method = RequestMethod.POST)
  @ResponseBody
  public NetworkServiceDescriptor onboardNSD(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws Exception, BadFormatException {

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();

      NetworkServiceDescriptor nsd = csarParser.onboardNSD(bytes, projectId);
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

      VirtualNetworkFunctionDescriptor vnfd = csarParser.onboardVNFD(bytes, projectId);

      return "{ \"id\": \"" + vnfd.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }

  @RequestMapping(
    value = "csar-vnf/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String marketDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    String downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    log.debug("This is download link" + downloadlink);
    URL packageLink = new URL(downloadlink);

    InputStream in = new BufferedInputStream(packageLink.openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int n = 0;
    while (-1 != (n = in.read(bytes))) {
      out.write(bytes, 0, n);
    }
    out.close();
    in.close();
    byte[] csarOnboard = out.toByteArray();

    CSARParser csarParser = new CSARParser();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        csarParser.onboardVNFD(csarOnboard, projectId);
    return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
  }
}
