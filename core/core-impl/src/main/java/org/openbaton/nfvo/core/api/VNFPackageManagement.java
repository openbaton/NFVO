/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.openbaton.nfvo.core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.ScriptRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by lto on 22/07/15.
 */
@Service
@Scope
@ConfigurationProperties
public class VNFPackageManagement
    implements org.openbaton.nfvo.core.interfaces.VNFPackageManagement {

  @Value("${vnfd.vnfp.cascade.delete:false}")
  private boolean cascadeDelete;

  private Logger log = LoggerFactory.getLogger(this.getClass());
  private Gson mapper = new GsonBuilder().create();
  @Autowired private NSDUtils nsdUtils;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VimBroker vimBroker;
  @Autowired private ScriptRepository scriptRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public void setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
  }

  @Override
  public VirtualNetworkFunctionDescriptor onboard(byte[] pack, String projectId)
      throws IOException, VimException, NotFoundException, PluginException {
    VNFPackage vnfPackage = new VNFPackage();
    vnfPackage.setScripts(new HashSet<Script>());
    Map<String, Object> metadata = null;
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;
    byte[] imageFile = null;
    NFVImage image = new NFVImage();

    InputStream tarStream;
    ArchiveInputStream myTarFile;
    try {
      tarStream = new ByteArrayInputStream(pack);
      myTarFile = new ArchiveStreamFactory().createArchiveInputStream("tar", tarStream);
    } catch (ArchiveException e) {
      e.printStackTrace();
      throw new IOException();
    }
    TarArchiveEntry entry;
    Map<String, Object> imageDetails = new HashMap<>();
    while ((entry = (TarArchiveEntry) myTarFile.getNextEntry()) != null) {
      /* Get the name of the file */
      if (entry.isFile() && !entry.getName().startsWith("./._")) {
        log.debug("file inside tar: " + entry.getName());
        byte[] content = new byte[(int) entry.getSize()];
        myTarFile.read(content, 0, content.length);
        if (entry.getName().equals("Metadata.yaml")) {
          YamlJsonParser yaml = new YamlJsonParser();
          metadata = yaml.parseMap(new String(content));
          //Get configuration for NFVImage
          String[] REQUIRED_PACKAGE_KEYS = new String[] {"name", "image"};
          for (String requiredKey : REQUIRED_PACKAGE_KEYS) {
            if (!metadata.containsKey(requiredKey)) {
              throw new NotFoundException(
                  "Not found " + requiredKey + " of VNFPackage in Metadata.yaml");
            }
            if (metadata.get(requiredKey) == null) {
              throw new NullPointerException(
                  "Not defined " + requiredKey + " of VNFPackage in Metadata.yaml");
            }
          }
          vnfPackage.setName((String) metadata.get("name"));
          if (metadata.containsKey("scripts-link"))
            vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
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
            if (imageDetails.get("upload").equals("true")
                || imageDetails.get("upload").equals("check")) {
              vnfPackage.setImageLink((String) imageDetails.get("link"));
              if (metadata.containsKey("image-config")) {
                log.debug("image-config: " + metadata.get("image-config"));
                Map<String, Object> imageConfig =
                    (Map<String, Object>) metadata.get("image-config");
                //Check if all required keys are available
                String[] REQUIRED_IMAGE_CONFIG =
                    new String[] {
                      "name",
                      "diskFormat",
                      "containerFormat",
                      "minCPU",
                      "minDisk",
                      "minRam",
                      "isPublic"
                    };
                for (String requiredKey : REQUIRED_IMAGE_CONFIG) {
                  if (!imageConfig.containsKey(requiredKey)) {
                    throw new NotFoundException(
                        "Not found key: " + requiredKey + " of image-config in Metadata.yaml");
                  }
                  if (imageConfig.get(requiredKey) == null) {
                    throw new NullPointerException(
                        "Not defined value of key: "
                            + requiredKey
                            + " of image-config in Metadata.yaml");
                  }
                }
                image.setName((String) imageConfig.get("name"));
                image.setDiskFormat(((String) imageConfig.get("diskFormat")).toUpperCase());
                image.setContainerFormat(
                    ((String) imageConfig.get("containerFormat")).toUpperCase());
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
        } else if (!entry.getName().startsWith("scripts/") && entry.getName().endsWith(".json")) {
          //this must be the vnfd
          //and has to be onboarded in the catalogue
          String json = new String(content);
          log.trace("Content of json is: " + json);
          try {
            virtualNetworkFunctionDescriptor =
                mapper.fromJson(json, VirtualNetworkFunctionDescriptor.class);
          } catch (Exception e) {
            e.printStackTrace();
          }
          log.trace("Created VNFD: " + virtualNetworkFunctionDescriptor);
          nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor, projectId);
        } else if (entry.getName().endsWith(".img")) {
          //this must be the image
          //and has to be upladed to the RIGHT vim
          imageFile = content;
          log.debug("imageFile is: " + entry.getName());
          throw new VimException(
              "Uploading an image file from the VNFPackage is not supported at this moment. Please use the image link.");
        } else if (entry.getName().startsWith("scripts/")) {
          Script script = new Script();
          script.setName(entry.getName().substring(8));
          script.setPayload(content);
          vnfPackage.getScripts().add(script);
        }
      }
    }
    if (metadata == null) {
      throw new NotFoundException("VNFPackageManagement: Not found Metadata.yaml");
    }
    if (vnfPackage.getScriptsLink() != null) {
      if (!vnfPackage.getScripts().isEmpty()) {
        log.debug(
            "VNFPackageManagement: Remove scripts got by scripts/ because the scripts-link is defined");
        vnfPackage.setScripts(new HashSet<Script>());
      }
    }
    List<String> vimInstances = new ArrayList<>();
    if (imageDetails.get("upload").equals("check")) {
      if (vnfPackage.getImageLink() == null && imageFile == null) {
        throw new NotFoundException(
            "VNFPackageManagement: For option upload=check you must define an image. Neither the image link is defined nor the image file is available. Please define at least one if you want to upload a new image");
      }
    }

    if (imageDetails.get("upload").equals("true")) {
      log.debug("VNFPackageManagement: Uploading a new Image");
      if (vnfPackage.getImageLink() == null && imageFile == null) {
        throw new NotFoundException(
            "VNFPackageManagement: Neither the image link is defined nor the image file is available. Please define at least one if you want to upload a new image");
      } else if (vnfPackage.getImageLink() != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image link");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
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
      } else if (imageFile != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image file");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
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
    } else {
      if (!imageDetails.containsKey("ids") && !imageDetails.containsKey("names")) {
        throw new NotFoundException(
            "VNFPackageManagement: Upload option 'false' or 'check' requires at least a list of ids or names to find the right image.");
      }
      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
        for (String vimName : vdu.getVimInstanceName()) {

          VimInstance vimInstance = null;

          for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
            if (vimName.equals(vi.getName())) vimInstance = vi;
          }

          if (vimInstance == null) {
            throw new NotFoundException(
                "Vim Instance with name " + vimName + " was not found in project: " + projectId);
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
                      "VNFPackageManagement: Multiple images found with the defined list of IDs. Do not know which one to choose");
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
                        "VNFPackageManagement: Multiple images found with the same name. Do not know which one to choose. To avoid this, define the id");
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
                    "VNFPackageManagement: Neither the image link is defined nor the image file is available. Please define at least one if you want to upload a new image");
              } else if (vnfPackage.getImageLink() != null) {
                log.debug("VNFPackageManagement: Uploading a new Image by using the image link");
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
              } else if (imageFile != null) {
                log.debug("VNFPackageManagement: Uploading a new Image by using the image file");
                if (!vimInstances.contains(
                    vimInstance.getId())) { // check if we didn't already upload it
                  Vim vim = vimBroker.getVim(vimInstance.getType());
                  log.debug(
                      "VNFPackageManagement: Uploading a new Image to VimInstance "
                          + vimInstance.getName());
                  image = vim.add(vimInstance, image, imageFile);
                  if (vdu.getVm_image() == null) vdu.setVm_image(new HashSet<String>());
                  vimInstances.add(vimInstance.getId());
                  vdu.getVm_image().add(image.getExtId());
                }
              }
            } else {
              throw new NotFoundException(
                  "VNFPackageManagement: Neither the defined ids nor the names were found. Use upload option 'check' to get sure that the image will be available");
            }
          } else {
            log.debug("VNFPackageManagement: Found image");
          }
        }
      }
    }
    vnfPackage.setImage(image);
    myTarFile.close();
    vnfPackage.setProjectId(projectId);
    vnfPackageRepository.save(vnfPackage);
    virtualNetworkFunctionDescriptor.setVnfPackageLocation(vnfPackage.getId());
    virtualNetworkFunctionDescriptor.setProjectId(projectId);
    vnfdRepository.save(virtualNetworkFunctionDescriptor);
    log.trace("Persisted " + virtualNetworkFunctionDescriptor);
    log.trace(
        "Onboarded VNFPackage ("
            + virtualNetworkFunctionDescriptor.getVnfPackageLocation()
            + ") successfully");
    return virtualNetworkFunctionDescriptor;
  }

  @Override
  public void disable() {}

  @Override
  public void enable() {}

  @Override
  public VNFPackage update(String id, VNFPackage pack_new, String projectId) {
    VNFPackage old = vnfPackageRepository.findFirstById(id);
    if (!old.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFPackage not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    old.setName(pack_new.getName());
    old.setImage(pack_new.getImage());
    return old;
  }

  @Override
  public VNFPackage query(String id, String projectId) {
    VNFPackage vnfPackage = vnfPackageRepository.findFirstById(id);
    if (!vnfPackage.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFPackage not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return vnfPackage;
  }

  @Override
  public Iterable<VNFPackage> query() {
    return vnfPackageRepository.findAll();
  }

  @Override
  public void delete(String id, String projectId) throws WrongAction {
    log.info("Removing VNFPackage: " + id);
    if (!vnfPackageRepository.findFirstById(id).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFPackage not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    //TODO remove image in the VIM
    Iterable<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors =
        vnfdRepository.findAll();
    if (cascadeDelete)
      for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
          virtualNetworkFunctionDescriptors)
        if (virtualNetworkFunctionDescriptor.getVnfPackageLocation().equals(id)) {
          if (!vnfdBelongsToNSD(virtualNetworkFunctionDescriptor)) {
            log.info("Removing VNFDescriptor: " + virtualNetworkFunctionDescriptor.getName());
            vnfdRepository.delete(virtualNetworkFunctionDescriptor.getId());
            break;
          } else
            throw new WrongAction(
                "It is not possible to remove a vnfPackage --> vnfdescriptor if the NSD is still onboarded");
        }
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        virtualNetworkFunctionDescriptors)
      if (virtualNetworkFunctionDescriptor.getVnfPackageLocation().equals(id)) {
        throw new WrongAction(
            "It is not possible to remove the vnfPackage with id "
                + id
                + ", a VNFD referencing it is still onboarded");
      }
    vnfPackageRepository.delete(id);
  }

  private boolean vnfdBelongsToNSD(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
    for (NetworkServiceDescriptor networkServiceDescriptor : nsdRepository.findAll()) {
      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        if (vnfd.getId().equals(virtualNetworkFunctionDescriptor.getId())) return true;
      }
    }
    return false;
  }

  @Override
  public Script updateScript(Script script) {
    return scriptRepository.save(script);
  }

  @Override
  public Iterable<VNFPackage> queryByProjectId(String projectId) {
    return vnfPackageRepository.findByProjectId(projectId);
  }
}
