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

package org.openbaton.tosca.parser;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.openbaton.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by rvl on 12.09.16.
 */
@Service
public class CSARParser {

  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFPackageManagement vnfPackageManagement;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private VimBroker vimBroker;
  @Autowired private NSDUtils nsdUtils;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private TOSCAParser toscaParser;

  private Set<Script> scripts = new HashSet<>();
  private ByteArrayOutputStream metadata;
  private ByteArrayOutputStream template;
  private ArrayList<String> imageNames = new ArrayList<>();
  private ByteArrayOutputStream vnfMetadata;
  private ArrayList<String> folderNames = new ArrayList<>();

  private String entryDefinitions = null;

  public CSARParser() {
    this.toscaParser = new TOSCAParser();
  }

  /*
   *
   * Helper functions - Reading a csar and creating a proper vnf package
   *
   */

  private void readFiles(InputStream csar_file) throws IOException, NotFoundException {

    ZipInputStream zipStream = new ZipInputStream(csar_file);
    ZipEntry entry;
    this.scripts.clear();
    this.folderNames.clear();
    this.template = new ByteArrayOutputStream();
    this.metadata = new ByteArrayOutputStream();

    while ((entry = zipStream.getNextEntry()) != null) {

      if (!entry.isDirectory()) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int count;
        byte[] buffer = new byte[1024];
        while ((count = zipStream.read(buffer)) != -1) {
          baos.write(buffer, 0, count);
        }

        String fileName = entry.getName();

        if (fileName.toLowerCase().endsWith(".meta")) {
          this.metadata = baos;
        } else if (fileName.toLowerCase().endsWith(".yaml")) {
          if (fileName.toLowerCase().endsWith("metadata.yaml")) {
            this.vnfMetadata = baos;
          } else {
            this.template = baos;
          }
        } else {

          Script script = new Script();
          String[] splittedName = fileName.split("/");
          if (splittedName.length > 2) {
            String scriptName = splittedName[1] + "!_!" + splittedName[splittedName.length - 1];
            folderNames.add(splittedName[1]);
            script.setName(scriptName);

          } else script.setName(splittedName[splittedName.length - 1]);

          script.setPayload(baos.toByteArray());
          this.scripts.add(script);
        }
      }
    }
    if (this.metadata == null) {
      throw new NotFoundException("CSARParser: Not found TOSCA.meta");
    }
    if (this.vnfMetadata == null) {
      throw new NotFoundException("CSARParser: Not found Metadata.yaml");
    }
    if (this.template == null) {
      throw new NotFoundException("CSARParser: Not found VNFD / NSD Template");
    }

    //zipStream.close();
  }

  private void readMetaData() throws IOException {

    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(this.metadata.toByteArray())));
    String strLine;

    String entryDefinition = "Entry-Definitions:";
    String image = "image:";

    imageNames.clear();

    while ((strLine = br.readLine()) != null) {

      if (strLine.contains(entryDefinition)) {
        this.entryDefinitions =
            strLine.substring(entryDefinition.length(), strLine.length()).trim();
      }
      if (strLine.contains(image)) {
        this.imageNames.add(strLine.substring(image.length(), strLine.length()).trim());
      }
    }

    br.close();
  }

  public void parseVNFCSAR(String vnfd_csar) throws Exception {

    InputStream csar = new FileInputStream(vnfd_csar);
    readFiles(csar);

    readMetaData();

    VNFDTemplate vnfdTemplate = Utils.bytesToVNFDTemplate(this.template);
    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
  }

  public NetworkServiceDescriptor parseNSDCSAR(String nsd_csar) throws Exception {

    InputStream input = new FileInputStream(new File(nsd_csar));
    readFiles(input);

    readMetaData();
    NSDTemplate nsdTemplate = Utils.bytesToNSDTemplate(this.template);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    return nsd;
  }

  private NFVImage getImage(
      VNFPackage vnfPackage,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String projectId)
      throws NotFoundException, PluginException, VimException, IncompatibleVNFPackage {

    Map<String, Object> metadata;
    NFVImage image = new NFVImage();
    Map<String, Object> imageDetails = new HashMap<>();
    List<String> vimInstances = new ArrayList<>();
    byte[] imageFile = null;

    YamlJsonParser yaml = new YamlJsonParser();
    metadata = yaml.parseMap(new String(this.vnfMetadata.toByteArray()));
    //Get configuration for NFVImage
    String[] REQUIRED_PACKAGE_KEYS = new String[] {"name", "image", "vim_types"};
    for (String requiredKey : REQUIRED_PACKAGE_KEYS) {
      if (!metadata.containsKey(requiredKey)) {
        throw new NotFoundException("Not found " + requiredKey + " of VNFPackage in Metadata.yaml");
      }
      if (metadata.get(requiredKey) == null) {
        throw new NullPointerException(
            "Not defined " + requiredKey + " of VNFPackage in Metadata.yaml");
      }
    }
    vnfPackage.setName((String) metadata.get("name"));
    if (metadata.containsKey("nfvo_version")) {
      String nfvo_version = (String) metadata.get("nfvo_version");
      String actualNfvoVersion = getNfvoVersion();
      if (nfvo_version.equals(actualNfvoVersion)) {
        vnfPackage.setNfvo_version(nfvo_version);
      } else {
        throw new IncompatibleVNFPackage(
            "The NFVO Version: "
                + nfvo_version
                + " specified in the Metadata"
                + " is not compatible with the this NFVOs version: "
                + actualNfvoVersion);
      }
    }
    if (metadata.containsKey("scripts-link"))
      vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
    if (metadata.containsKey("vim_types")) {
      List<String> vimTypes = (List<String>) metadata.get("vim_types");
      vnfPackage.setVimTypes(vimTypes);
    }
    if (metadata.containsKey("image")) {
      imageDetails = (Map<String, Object>) metadata.get("image");
      String[] REQUIRED_IMAGE_DETAILS = new String[] {"upload"};
      log.debug("image: " + imageDetails);
      for (String requiredKey : REQUIRED_IMAGE_DETAILS) {
        if (!imageDetails.containsKey(requiredKey)) {
          throw new NotFoundException(
              "Not found key: " + requiredKey + "of image in Metadata.yaml");
        }
        if (imageDetails.get(requiredKey) == null) {
          throw new NullPointerException(
              "Not defined value of key: " + requiredKey + " of image in Metadata.yaml");
        }
      }
      //If upload==true -> create a new Image
      if (imageDetails.get("upload").equals("true") || imageDetails.get("upload").equals("check")) {
        vnfPackage.setImageLink((String) imageDetails.get("link"));
        if (metadata.containsKey("image-config")) {
          log.debug("image-config: " + metadata.get("image-config"));
          Map<String, Object> imageConfig = (Map<String, Object>) metadata.get("image-config");
          //Check if all required keys are available
          String[] REQUIRED_IMAGE_CONFIG =
              new String[] {
                "name", "diskFormat", "containerFormat", "minCPU", "minDisk", "minRam", "isPublic"
              };
          for (String requiredKey : REQUIRED_IMAGE_CONFIG) {
            if (!imageConfig.containsKey(requiredKey)) {
              throw new NotFoundException(
                  "Not found key: " + requiredKey + " of image-config in Metadata.yaml");
            }
            if (imageConfig.get(requiredKey) == null) {
              throw new NullPointerException(
                  "Not defined value of key: " + requiredKey + " of image-config in Metadata.yaml");
            }
          }
          image.setName((String) imageConfig.get("name"));
          image.setDiskFormat(((String) imageConfig.get("diskFormat")).toUpperCase());
          image.setContainerFormat(((String) imageConfig.get("containerFormat")).toUpperCase());
          image.setMinCPU(Integer.toString((Integer) imageConfig.get("minCPU")));
          image.setMinDiskSpace((Integer) imageConfig.get("minDisk"));
          image.setMinRam((Integer) imageConfig.get("minRam"));
          image.setIsPublic(
              Boolean.parseBoolean(Integer.toString((Integer) imageConfig.get("minRam"))));
        } else {
          throw new NotFoundException(
              "The image-config is not defined. Please define it to upload a new image");
        }
      }
    } else {
      throw new NotFoundException(
          "The image details are not defined. Please define it to use the right image");
    }
    nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor, projectId);
    if (imageDetails.get("upload").equals("true")) {
      log.debug("VNFPackageManagement: Uploading a new Image");
      if (vnfPackage.getImageLink() == null && imageFile == null) {
        throw new NotFoundException(
            "VNFPackageManagement: Neither the image link is defined nor the image file is available. Please define at least one if you want to upload a new image");
      } else if (vnfPackage.getImageLink() != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image link");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
          if (vdu.getVimInstanceName() != null) {
            for (String vimName : vdu.getVimInstanceName()) {
              VimInstance vimInstance = null;

              for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
                if (vimName.equals(vi.getName())) vimInstance = vi;
              }

              if (!vimInstances.contains(
                  vimInstance.getId())) { // check if we didn't already upload it
                Vim vim = vimBroker.getVim(vimInstance.getType());
                log.debug(
                    "VNFPackageManagement: Uploading a new Image to VimInstance "
                        + vimInstance.getName());
                image = vim.add(vimInstance, image, vnfPackage.getImageLink());
                if (vdu.getVm_image() == null) vdu.setVm_image(new HashSet<String>());
                vdu.getVm_image().add(image.getExtId());
                vimInstances.add(vimInstance.getId());
              }
            }
          }
        }
      } else if (imageFile != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image file");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
          if (vdu.getVimInstanceName() != null) {
            for (String vimName : vdu.getVimInstanceName()) {
              VimInstance vimInstance = null;

              for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
                if (vimName.equals(vi.getName())) vimInstance = vi;
              }

              if (!vimInstances.contains(
                  vimInstance.getId())) { // check if we didn't already upload it
                Vim vim = vimBroker.getVim(vimInstance.getType());
                log.debug(
                    "VNFPackageManagement: Uploading a new Image to VimInstance "
                        + vimInstance.getName());
                image = vim.add(vimInstance, image, imageFile);
                if (vdu.getVm_image() == null) vdu.setVm_image(new HashSet<String>());
                vdu.getVm_image().add(image.getExtId());
                vimInstances.add(vimInstance.getId());
              }
            }
          }
        }
      }
    } else {
      if (!imageDetails.containsKey("ids") && !imageDetails.containsKey("names")) {
        throw new NotFoundException(
            "VNFPackageManagement: Upload option 'false' or 'check' requires at least a list of ids or names to find "
                + " the right image.");
      }
      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
        if (vdu.getVimInstanceName() != null) {
          if (vdu.getVimInstanceName().size() != 0) {
            for (String vimName : vdu.getVimInstanceName()) {

              VimInstance vimInstance = null;

              for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
                if (vimName.equals(vi.getName())) {
                  vimInstance = vi;
                }
              }

              if (vimInstance == null) {
                throw new NotFoundException(
                    "Vim Instance with name "
                        + vimName
                        + " was not found in project: "
                        + projectId);
              }

              boolean found = false;
              //First, check for image ids
              if (imageDetails.containsKey("ids")) {
                for (NFVImage nfvImage : vimInstance.getImages()) {
                  if (((List) imageDetails.get("ids")).contains(nfvImage.getExtId())) {
                    if (!found) {
                      vdu.getVm_image().add(nfvImage.getExtId());
                      found = true;
                    } else {
                      throw new NotFoundException(
                          "VNFPackageManagement: Multiple images found with the defined list of IDs. Do not know "
                              + "which one to choose");
                    }
                  }
                }
              }

              //If no one was found, check for the names
              if (!found) {
                if (imageDetails.containsKey("names")) {
                  for (NFVImage nfvImage : vimInstance.getImages()) {
                    if (((List) imageDetails.get("names")).contains(nfvImage.getName())) {
                      if (!found) {
                        vdu.getVm_image().add(nfvImage.getExtId());
                        found = true;
                      } else {
                        throw new NotFoundException(
                            "VNFPackageManagement: Multiple images found with the same name. Do not know which one to"
                                + " choose. To avoid this, define the id");
                      }
                    }
                  }
                }
              }
              //if no image was found with the defined ids or names, the image doesn't exist
              if (!found) {
                if (imageDetails.get("upload").equals("check")) {
                  if (vnfPackage.getImageLink() == null && imageFile == null) {
                    throw new NotFoundException(
                        "VNFPackageManagement: Neither the image link is defined nor the image file is available. "
                            + "Please define at least one if you want to upload a new image");
                  } else if (vnfPackage.getImageLink() != null) {
                    log.debug(
                        "VNFPackageManagement: Uploading a new Image by using the image link");
                    if (!vimInstances.contains(
                        vimInstance.getId())) { // check if we didn't already upload it
                      Vim vim = vimBroker.getVim(vimInstance.getType());
                      log.debug(
                          "VNFPackageManagement: Uploading a new Image to VimInstance "
                              + vimInstance.getName());
                      image = vim.add(vimInstance, image, vnfPackage.getImageLink());
                      if (vdu.getVm_image() == null) {
                        vdu.setVm_image(new HashSet<String>());
                      }
                      vdu.getVm_image().add(image.getExtId());
                      vimInstances.add(vimInstance.getId());
                    }
                  } else if (imageFile != null) {
                    log.debug(
                        "VNFPackageManagement: Uploading a new Image by using the image file");
                    if (!vimInstances.contains(
                        vimInstance.getId())) { // check if we didn't already upload it
                      Vim vim = vimBroker.getVim(vimInstance.getType());
                      log.debug(
                          "VNFPackageManagement: Uploading a new Image to VimInstance "
                              + vimInstance.getName());
                      image = vim.add(vimInstance, image, imageFile);
                      if (vdu.getVm_image() == null) {
                        vdu.setVm_image(new HashSet<String>());
                      }
                      vimInstances.add(vimInstance.getId());
                      vdu.getVm_image().add(image.getExtId());
                    }
                  }
                } else {
                  throw new NotFoundException(
                      "VNFPackageManagement: Neither the defined ids nor the names were found. Use upload option "
                          + "'check' to get sure that the image will be available");
                }
              } else {
                log.debug("VNFPackageManagement: Found image");
              }
            }
          } else { // vimInstanceName is not defined, just put the name into the vdu
            List names = (List) imageDetails.get("names");
            log.debug("Adding names: " + names);
            vdu.getVm_image().addAll(names);
          }
        } else { // vimInstanceName is not defined, just put the name into the vdu
          List names = (List) imageDetails.get("names");
          log.debug("Adding names: " + names);
          vdu.getVm_image().addAll(names);
        }
      }
    }

    return image;
  }

  private String getNfvoVersion() {
    String version =
        org.openbaton.nfvo.core.api.VNFPackageManagement.class
            .getPackage()
            .getImplementationVersion();
    if (version.lastIndexOf("_SNAPSHOT") != -1)
      version = version.substring(0, version.lastIndexOf("_SNAPSHOT"));
    return version;
  }

  private String saveVNFD(
      VirtualNetworkFunctionDescriptor vnfd, String projectId, Set<Script> vnfScripts)
      throws PluginException, VimException, NotFoundException, IOException, IncompatibleVNFPackage {

    VNFPackage vnfPackage = new VNFPackage();

    vnfPackage.setImage(getImage(vnfPackage, vnfd, projectId));
    vnfPackage.setScripts(vnfScripts);
    vnfPackage.setName(vnfd.getName());
    vnfPackage.setProjectId(projectId);

    vnfPackageRepository.save(vnfPackage);

    vnfd.setProjectId(projectId);
    vnfd.setVnfPackageLocation(vnfPackage.getId());
    vnfdRepository.save(vnfd);

    return vnfPackage.getId();
  }

  /*
   *
   * MAIN FUNCTIONS
   *
   */

  public VirtualNetworkFunctionDescriptor onboardVNFD(byte[] bytes, String projectId)
      throws NotFoundException, PluginException, VimException, IOException, IncompatibleVNFPackage {

    File temp = File.createTempFile("CSAR", null);
    FileOutputStream fos = new FileOutputStream(temp);
    fos.write(bytes);
    InputStream input = new FileInputStream(temp);

    readFiles(input);

    VNFDTemplate vnfdt = Utils.bytesToVNFDTemplate(this.template);
    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdt);

    saveVNFD(vnfd, projectId, scripts);

    input.close();
    fos.close();
    this.template.close();
    this.metadata.close();

    return vnfd;
  }

  public NetworkServiceDescriptor onboardNSD(byte[] bytes, String projectId)
      throws NotFoundException, PluginException, VimException, IOException, IncompatibleVNFPackage {

    File temp = File.createTempFile("CSAR", null);
    FileOutputStream fos = new FileOutputStream(temp);
    fos.write(bytes);
    InputStream input = new FileInputStream(temp);
    ArrayList<String> ids = new ArrayList<>();

    readFiles(input);

    NSDTemplate nsdTemplate = Utils.bytesToNSDTemplate(this.template);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
      if (!folderNames.contains(vnfd.getType())) {
        throw new NotFoundException("No Scripts specified for the VNFD of type: " + vnfd.getType());
      }
      Set<Script> vnfScripts = new HashSet<>();
      for (Script script : scripts) {
        String[] splitted_name = script.getName().split("!_!");
        log.debug(splitted_name[0]);
        log.debug(script.getName());

        if (splitted_name.length == 2) {
          String folder_name = splitted_name[0];
          if (folder_name.equals(vnfd.getType())) {
            Script s = new Script();
            s.setName(splitted_name[1]);
            s.setPayload(script.getPayload());
            vnfScripts.add(s);
          }
        }
      }
      ids.add(saveVNFD(vnfd, projectId, vnfScripts));
    }
    nsd.getVnfd().clear();

    for (String id : ids) {

      String vnfdId = "";

      Iterable<VirtualNetworkFunctionDescriptor> vnfds = vnfdRepository.findByProjectId(projectId);
      for (VirtualNetworkFunctionDescriptor vnfd : vnfds) {
        if (vnfd.getVnfPackageLocation().equals(id)) {

          vnfdId = vnfd.getId();
        }
      }

      VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
      vnfd.setId(vnfdId);
      nsd.getVnfd().add(vnfd);
    }

    input.close();
    fos.close();
    this.template.close();
    this.metadata.close();

    return nsd;
  }
}
