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

package org.openbaton.nfvo.api.tosca;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.annotations.ApiOperation;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.openbaton.tosca.parser.CSARParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Created by rvl on 29.08.16. */
@RestController
public class RestCSAR {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
  @Autowired private CSARParser csarParser;
  @Autowired private VNFPackageManagement vnfPackageManagement;

  @RequestMapping(value = "/api/v1/csar-nsd", method = RequestMethod.POST)
  @ResponseBody
  @ApiOperation(
    value = "Add a CSAR Network Service",
    notes =
        "The request parameter 'file' specifies an archive which is needed to onboard a Network Service as CSAR. "
            + "On how to create such an archive refer to: http://openbaton.github.io/documentation/tosca-CSAR-onboarding/"
  )
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

  @ApiOperation(value = "", notes = "", hidden = true)
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

  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "/api/v1/csar-vnf/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String marketDownloadVNF(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage,
          org.openbaton.tosca.exceptions.NotFoundException, BadRequestException,
          AlreadyExistingException, InterruptedException, EntityUnreachableException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    String downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    log.debug("This is download link" + downloadlink);
    URL packageLink = new URL(downloadlink);

    InputStream in = new BufferedInputStream(packageLink.openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int n = in.read(bytes);
    while (-1 != n) {
      out.write(bytes, 0, n);
    }

    byte[] csarOnboard = out.toByteArray();
    out.close();
    in.close();

    log.debug(String.valueOf(csarOnboard.length));

    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        csarParser.onboardVNFD(csarOnboard, projectId);
    return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
  }

  @ApiOperation(
    value = "Download a Network Service CSAR from the Open Baton Marketplace",
    notes =
        "The Request Body takes a JSON with the parameter link which holds the URL to the CSAR on the Open Baton Marketplace"
  )
  @RequestMapping(
    value = "/api/v1/csar-ns/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String marketDownloadNS(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage,
          NetworkServiceIntegrityException, BadFormatException, CyclicDependenciesException,
          EntityInUseException, org.openbaton.tosca.exceptions.NotFoundException,
          BadRequestException, AlreadyExistingException, InterruptedException,
          EntityUnreachableException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    String downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    log.debug("This is download link" + downloadlink);
    URL packageLink = new URL(downloadlink);

    InputStream in = new BufferedInputStream(packageLink.openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int n;
    while (-1 != (n = in.read(bytes))) {
      out.write(bytes, 0, n);
    }
    out.close();
    in.close();
    byte[] csarOnboard = out.toByteArray();

    log.debug(String.valueOf(csarOnboard.length));

    NetworkServiceDescriptor networkServiceDescriptor =
        csarParser.onboardNSD(csarOnboard, projectId);
    networkServiceDescriptorManagement.onboard(networkServiceDescriptor, projectId);
    return "{ \"id\": \"" + networkServiceDescriptor.getId() + "\"}";
  }
}
