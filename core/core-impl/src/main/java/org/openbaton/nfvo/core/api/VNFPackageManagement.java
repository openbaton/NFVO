/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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
import com.google.gson.JsonSyntaxException;
import com.networknt.schema.ValidationMessage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.AdditionalRepoInfo;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.PackageType;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VNFPackageMetadata;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.ExistingVNFPackage;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VNFPackageFormatException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;
import org.openbaton.nfvo.common.utils.schema.SchemaValidator;
import org.openbaton.nfvo.core.utils.CheckVNFPackage;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.ScriptRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFPackageMetadataRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
@Scope
@ConfigurationProperties
@EnableAsync
@SuppressWarnings({"unsafe", "unchecked"})
public class VNFPackageManagement
    implements org.openbaton.nfvo.core.interfaces.VNFPackageManagement {

  @Value("${vnfd.vnfp.cascade.delete:false}")
  private boolean cascadeDelete;

  @Value("${nfvo.version.check:true}")
  private boolean checkNfvoVersion;
  // This is only in case you run the NFVO from IDE
  @Value("${nfvo.version:}")
  private String nfvoVersion;

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Gson mapper = new GsonBuilder().create();
  @Autowired private NSDUtils nsdUtils;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VimBroker vimBroker;
  @Autowired private ScriptRepository scriptRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private VnfmManager vnfmManager;
  @Autowired private ImageChecker imageChecker;
  @Autowired private VNFPackageMetadataRepository vnfPackageMetadataRepository;
  @Autowired private org.openbaton.nfvo.core.interfaces.VimManagement vimManagement;

  private static ReentrantLock lock = new ReentrantLock();

  private VirtualNetworkFunctionDescriptor handleVirtualNetworkFunctionDescriptor(byte[] content)
      throws BadRequestException, IOException, BadFormatException {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;
    String json = new String(content);
    log.debug("Content of json is: " + json);
    Set<ValidationMessage> errors =
        SchemaValidator.validateSchema(VirtualNetworkFunctionDescriptor.class, json);
    if (errors.size() > 0) {
      StringBuilder builder = new StringBuilder();
      for (ValidationMessage s : errors) {
        String message = s.getMessage();
        builder.append(message).append(", ");
      }
      throw new BadFormatException(builder.toString());
    }
    try {
      virtualNetworkFunctionDescriptor =
          mapper.fromJson(json, VirtualNetworkFunctionDescriptor.class);
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
      throw new BadFormatException(
          "The VNFPackage's VNFD file contains invalid Json syntax: " + e.getMessage());
    }

    if (virtualNetworkFunctionDescriptor.getVdu() == null
        || virtualNetworkFunctionDescriptor.getVdu().isEmpty())
      throw new BadFormatException("The VNFD defined in the file does not contain any VDUs.");
    if (virtualNetworkFunctionDescriptor.getName() == null
        || virtualNetworkFunctionDescriptor.getName().equals(""))
      throw new BadFormatException("The VNFD defined in the file has no name.");

    int i = 1;
    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
      if (vdu.getName() == null) {
        vdu.setName(virtualNetworkFunctionDescriptor.getName() + "-" + i);
        i++;
      }
    }
    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
      log.debug("vdu name: " + vdu.getName());
    }
    log.debug("Created VNFD: " + virtualNetworkFunctionDescriptor.getName());
    return virtualNetworkFunctionDescriptor;
  }

  @Override
  public synchronized VirtualNetworkFunctionDescriptor add(
      byte[] pack, boolean isImageIncluded, String projectId, boolean fromMarketPlace)
      throws IOException, VimException, NotFoundException, PluginException, ExistingVNFPackage,
          VNFPackageFormatException, IncompatibleVNFPackage, BadRequestException,
          AlreadyExistingException, NetworkServiceIntegrityException, InterruptedException,
          BadFormatException, ExecutionException {

    CheckVNFPackage.checkStructure(pack, isImageIncluded, fromMarketPlace);

    VNFPackage vnfPackage = new VNFPackage();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;

    Map<String, Object> metadata = null;
    byte[] imageFile = new byte[0];
    try (ArchiveInputStream tarFile =
        new ArchiveStreamFactory()
            .createArchiveInputStream("tar", new ByteArrayInputStream(pack))) {
      TarArchiveEntry entry;
      // Here there are almost no checks whether keys exists or not, since the check has been done in the CheckVNFPackage class
      while ((entry = (TarArchiveEntry) tarFile.getNextEntry()) != null) {
        if (entry.isFile() && !entry.getName().startsWith("./._")) {
          byte[] content = new byte[(int) entry.getSize()];
          if (tarFile.read(content, 0, content.length) != content.length) {
            throw new IOException(String.format("Error reading from %s", entry.getName()));
          }
          if (entry.getName().equalsIgnoreCase("metadata.yaml")) {
            Yaml yaml = new Yaml();
            String yamlString = new String(content);
            metadata = (Map<String, Object>) yaml.load(yamlString);
            vnfPackage = handleMetadata(metadata, vnfPackage);
          } else if (!entry.getName().startsWith("scripts/") && entry.getName().endsWith(".json")) {
            //this must be the vnfd
            virtualNetworkFunctionDescriptor = handleVirtualNetworkFunctionDescriptor(content);
          } else if (entry.getName().startsWith("scripts/")) {
            Script script = new Script();
            script.setName(entry.getName().substring(8));
            script.setPayload(content);
            if (vnfPackage.getScripts() == null) vnfPackage.setScripts(new HashSet<>());
            vnfPackage.getScripts().add(script);
          } else if (!entry.getName().startsWith("scripts/") && entry.getName().endsWith(".json")) {
            imageFile = content;
          }
        }
      }
    } catch (ArchiveException e) {
      throw new VNFPackageFormatException(
          "Error opening the VNF package, ensure the extension is .tar and the archive is not corrupted",
          e);
    } catch (IOException e) {
      throw new VNFPackageFormatException(
          "Error reading the VNF package, ensure the archive is not corrupted", e);
    }
    if (virtualNetworkFunctionDescriptor == null)
      throw new BadFormatException("Missing VNFD in package");
    if (metadata == null) throw new BadFormatException("Missing Metadata.yaml in pacakge");

    if (virtualNetworkFunctionDescriptor.getVnfPackageLocation() != null) {
      throw new BadFormatException("VnfPackageLocation must be empty");
    }

    if (vnfPackage.getScriptsLink() != null
        && (vnfPackage.getScripts() != null && vnfPackage.getScripts().size() > 0)) {
      log.debug("Remove scripts got by scripts/ because the scripts-link is defined");
      vnfPackage.setScripts(new HashSet<>());
    }

    if (vnfPackage.getImage() != null) {
      lock.lock();
      try {
        handleImage(vnfPackage, imageFile, virtualNetworkFunctionDescriptor, metadata, projectId);
      } finally {
        lock.unlock();
      }
    }
    VNFPackageMetadata vnfPackageMetadata =
        handleVnfPackageMetadata(
            metadata, vnfPackage, virtualNetworkFunctionDescriptor.getEndpoint(), projectId, "tar");

    virtualNetworkFunctionDescriptor.setProjectId(projectId);

    nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor, projectId);
    nsdUtils.checkIntegrity(virtualNetworkFunctionDescriptor);

    Map<String, Object> vnfPackageMetadataParameters = new HashMap<>();
    vnfPackageMetadataParameters.put("name", metadata.get("name"));
    vnfPackageMetadataParameters.put(
        "vendor",
        metadata.get("vendor") == null ? metadata.get("provider") : metadata.get("vendor"));

    Map<String, Object> vnfdParameters = new HashMap<>();
    vnfdParameters.put("name", virtualNetworkFunctionDescriptor.getName());
    vnfdParameters.put("vendor", virtualNetworkFunctionDescriptor.getVendor());
    CheckVNFPackage.checkCommonParametersWithVNFD(vnfPackageMetadataParameters, vnfdParameters);

    vnfPackage.setProjectId(projectId);
    // check if package already exists

    Iterable<VNFPackageMetadata> vnfPackageMetadataIterable =
        query(
            vnfPackageMetadata.getName(),
            vnfPackageMetadata.getVendor(),
            vnfPackageMetadata.getVersion(),
            vnfPackageMetadata.getNfvoVersion(),
            vnfPackageMetadata.getVnfmType(),
            vnfPackageMetadata.getOsId(),
            vnfPackageMetadata.getOsVersion(),
            vnfPackageMetadata.getOsArchitecture(),
            vnfPackageMetadata.getTag(),
            vnfPackageMetadata.getProjectId());
    if (vnfPackageMetadataIterable != null && vnfPackageMetadataIterable.iterator().hasNext()) {
      for (VNFPackageMetadata vnfpm : vnfPackageMetadataIterable)
        log.trace("Already existing: " + vnfpm);
      throw new ExistingVNFPackage("VNF package already exists.");
    }

    vnfPackage = vnfPackageRepository.save(vnfPackage);
    // TODO understand this
    vnfPackageMetadataRepository.setVNFPackageId(vnfPackage.getId());

    virtualNetworkFunctionDescriptor.setVnfPackageLocation(vnfPackage.getId());
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    virtualNetworkFunctionDescriptor.setCreatedAt(format.format(new Date()));
    virtualNetworkFunctionDescriptor.setUpdatedAt(format.format(new Date()));
    virtualNetworkFunctionDescriptor = setIPConfigurations(virtualNetworkFunctionDescriptor);
    virtualNetworkFunctionDescriptor = vnfdRepository.save(virtualNetworkFunctionDescriptor);
    log.debug(
        "Onboarded VNFPackage ("
            + virtualNetworkFunctionDescriptor.getVnfPackageLocation()
            + ") successfully");
    return virtualNetworkFunctionDescriptor;
  }

  private VirtualNetworkFunctionDescriptor setIPConfigurations(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
    // If the VNF manager is not the fixed-host then skip this part
    if (!virtualNetworkFunctionDescriptor.getEndpoint().equalsIgnoreCase("fixed-host"))
      return virtualNetworkFunctionDescriptor;

    log.info("Adding configuration parameters for the configuration of IP, username and password");

    VNFComponent component =
        virtualNetworkFunctionDescriptor.getVdu().iterator().next().getVnfc().iterator().next();

    // Try to find smartly a floating IP to be used for the ssh_ip property. This IP will be used by the fixed-host VNFM for connecting to the VNF machine.
    // For floating IP we mean an IP which the fixed-host VNFM can connect to.
    // If the VNFC has multiple floating IPs, only one is needed for accessing it..
    String floatingIp = "";
    String virtualLinkName = "";
    for (VNFDConnectionPoint vnfdConnectionPoint : component.getConnection_point()) {
      if (vnfdConnectionPoint.getFloatingIp() != null
          && !vnfdConnectionPoint.getFloatingIp().isEmpty()
          && !vnfdConnectionPoint.getFloatingIp().equals("random")) {
        floatingIp = vnfdConnectionPoint.getFloatingIp();
        virtualLinkName = vnfdConnectionPoint.getVirtual_link_reference();
        break;
      }
    }

    // At this point the floating IP could be empty, find the virtual link with the random floating IP
    if (floatingIp.equals(""))
      for (VNFDConnectionPoint vnfdConnectionPoint : component.getConnection_point()) {
        if (vnfdConnectionPoint.getFloatingIp() != null
            && !vnfdConnectionPoint.getFloatingIp().isEmpty()) {
          floatingIp = vnfdConnectionPoint.getFloatingIp();
          virtualLinkName = vnfdConnectionPoint.getVirtual_link_reference();
          break;
        }
      }

    // Only one IP, username and password shall be set, because here we specify only the information for accessing the VNFC through the fixed-host VNFM.

    Set<ConfigurationParameter> configurationParameters = new HashSet<>();

    String ipConfigKey = "ssh_" + virtualLinkName + "_ip";
    String ipConfigValue = floatingIp;
    ConfigurationParameter ipConfigurationParameter = new ConfigurationParameter();
    ipConfigurationParameter.setConfKey(ipConfigKey);
    ipConfigurationParameter.setValue(ipConfigValue);
    ipConfigurationParameter.setDescription("IP to be used for accessing the VNF machine");
    configurationParameters.add(ipConfigurationParameter);

    String usernameConfigKey = "ssh_username";
    String usernameConfigValue = "";
    ConfigurationParameter usernameConfigurationParameter = new ConfigurationParameter();
    usernameConfigurationParameter.setConfKey(usernameConfigKey);
    usernameConfigurationParameter.setValue(usernameConfigValue);
    usernameConfigurationParameter.setDescription(
        "SSH username for accessing the existing machine of the VNF");
    configurationParameters.add(usernameConfigurationParameter);

    String passwordConfigKey = "ssh_password";
    String passwordConfigValue = "";
    ConfigurationParameter passwordConfigurationParameter = new ConfigurationParameter();
    passwordConfigurationParameter.setConfKey(passwordConfigKey);
    passwordConfigurationParameter.setValue(passwordConfigValue);
    passwordConfigurationParameter.setDescription(
        "SSH password for accessing the existing machine of the VNF");
    configurationParameters.add(passwordConfigurationParameter);

    if (virtualNetworkFunctionDescriptor.getConfigurations() == null
        || virtualNetworkFunctionDescriptor.getConfigurations().getConfigurationParameters() == null
        || virtualNetworkFunctionDescriptor
            .getConfigurations()
            .getConfigurationParameters()
            .isEmpty()) {
      Configuration ipConfiguration = new Configuration();
      ipConfiguration.setName("configuration");
      ipConfiguration.setProjectId(virtualNetworkFunctionDescriptor.getProjectId());
      ipConfiguration.setConfigurationParameters(configurationParameters);
      virtualNetworkFunctionDescriptor.setConfigurations(ipConfiguration);
    } else {
      virtualNetworkFunctionDescriptor
          .getConfigurations()
          .getConfigurationParameters()
          .addAll(configurationParameters);
    }
    return virtualNetworkFunctionDescriptor;
  }

  private VNFPackageMetadata handleVnfPackageMetadata(
      Map<String, Object> metadata,
      VNFPackage vnfPackage,
      String endpoint,
      String projectId,
      String type)
      throws NotFoundException, IncompatibleVNFPackage {
    VNFPackageMetadata vnfPackageMetadata = new VNFPackageMetadata();
    vnfPackageMetadata.setName((String) metadata.get("name"));
    vnfPackage.setName((String) metadata.get("name"));
    vnfPackageMetadata.setVnfmType(endpoint);
    //Check version compatibility between VNF Package and actual NFVO
    String vnfPackageNFVOVersion = null;
    vnfPackageMetadata.setProjectId(projectId);
    vnfPackageMetadata.setType(type);
    if (metadata.containsKey("nfvo-version")) {
      vnfPackageNFVOVersion = (String) metadata.get("nfvo-version");
    }
    if (metadata.containsKey("nfvo_version")) {
      vnfPackageNFVOVersion = (String) metadata.get("nfvo_version");
    }
    if (checkNfvoVersion) {
      CheckVNFPackage.compareNFVOVersions(vnfPackageNFVOVersion, getNfvoVersionWithoutSNAPSHOT());
    }

    vnfPackage.setNfvo_version(vnfPackageNFVOVersion);
    vnfPackageMetadata.setNfvoVersion(vnfPackageNFVOVersion);

    if (metadata.containsKey("vendor")) {
      vnfPackageMetadata.setVendor((String) metadata.get("vendor"));
    } else {
      vnfPackageMetadata.setVendor((String) metadata.get("provider"));
    }

    if (metadata.containsKey("version")) {
      vnfPackageMetadata.setVersion(String.valueOf(metadata.get("version")));
    } else {
      log.warn(
          "The VNF Package Metadata does not contain the field \"version\". Setting version 1.0 by default");
      vnfPackageMetadata.setVersion("1.0");
    }

    if (metadata.containsKey("vim-types")) {
      HashSet<String> vimTypes = new HashSet<String>((ArrayList) metadata.get("vim-types"));
      vnfPackageMetadata.setVimTypes(vimTypes);
      vnfPackage.setVimTypes(vimTypes);
    } else {
      LinkedHashSet<String> vimTypes = new LinkedHashSet<>();
      vnfPackageMetadata.setVimTypes(vimTypes);
      vimTypes.addAll((ArrayList) metadata.get("vim_types"));
      vnfPackage.setVimTypes(vimTypes);
    }

    vnfPackageMetadata.setDescription((String) metadata.get("description"));
    vnfPackageMetadata.setRequirements((Map) metadata.get("requirements"));

    // Optional keys
    vnfPackageMetadata.setTag((String) metadata.get("tag"));
    vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
    vnfPackageMetadata.setOsId((String) metadata.get("os-id"));
    vnfPackageMetadata.setOsVersion(String.valueOf(metadata.get("os-version")));
    vnfPackageMetadata.setOsArchitecture((String) metadata.get("os-architecture"));

    if (metadata.containsKey("additional-repos")) {
      List<Map<String, Object>> repoConfigurationInfoList =
          (List<Map<String, Object>>) metadata.get("additional-repos");
      for (Map<String, Object> rci : repoConfigurationInfoList) {
        String packageTypeString = (String) rci.get("type");
        PackageType packageType =
            packageTypeString.equalsIgnoreCase("rpm") ? PackageType.RPM : PackageType.DEB;
        AdditionalRepoInfo additionalRepoInfo = new AdditionalRepoInfo(packageType);
        if (rci.containsKey("key-url")) additionalRepoInfo.setKeyUrl((String) rci.get("key-url"));
        Set<String> configurationInfo = (Set<String>) rci.get("configuration");
        additionalRepoInfo.setConfiguration(configurationInfo);
        vnfPackageMetadata.addRepoConfigurationInfo(additionalRepoInfo);
      }
    }
    vnfPackage.setVnfPackageMetadata(vnfPackageMetadata);
    return vnfPackageMetadata;
  }

  public VNFPackage handleMetadata(Map<String, Object> metadata, VNFPackage vnfPackage)
      throws BadFormatException {

    //Get configuration for NFVImage
    String[] requiredPackageKeys = new String[] {"name", "vim_types"};
    for (String requiredKey : requiredPackageKeys) {
      if (!metadata.containsKey(requiredKey)) {
        throw new BadFormatException(
            "Not found " + requiredKey + " of VNFPackage in Metadata.yaml");
      }
      if (metadata.get(requiredKey) == null) {
        throw new NullPointerException(
            "Not defined " + requiredKey + " of VNFPackage in Metadata.yaml");
      }
    }
    vnfPackage.setName((String) metadata.get("name"));

    if (metadata.containsKey("scripts-link")) {
      vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
    }
    if (metadata.containsKey("vim_types")) {
      LinkedHashSet<String> vimTypes = new LinkedHashSet<>((ArrayList) metadata.get("vim_types"));
      vnfPackage.setVimTypes(vimTypes);
    } else {
      log.warn("vim_types is not specified! it is not possible to check the vim");
    }
    if (metadata.containsKey("image")) {
      Map<String, Object> imageDetails = (Map<String, Object>) metadata.get("image");
      String[] requiredImageDetails = new String[] {"upload"};
      log.debug("image: " + imageDetails);
      for (String requiredKey : requiredImageDetails) {
        if (!imageDetails.containsKey(requiredKey)) {
          throw new BadFormatException(
              "Not found key: " + requiredKey + "of image in Metadata.yaml");
        }
        if (imageDetails.get(requiredKey) == null) {
          throw new NullPointerException(
              "Not defined value of key: " + requiredKey + " of image in Metadata.yaml");
        }
      }
      if (imageDetails.get("upload").equals("true") || imageDetails.get("upload").equals("check")) {
        vnfPackage.setImageLink((String) imageDetails.get("link"));
        if (metadata.containsKey("image-config")) {
          log.debug("image-config: " + metadata.get("image-config"));
          Map<String, Object> imageConfig = (Map<String, Object>) metadata.get("image-config");
          //Check if all required keys are available
          String[] requiredImageConfig =
              new String[] {
                "name", "diskFormat", "containerFormat", "minCPU", "minDisk", "minRam", "isPublic"
              };
          for (String requiredKey : requiredImageConfig) {
            if (!imageConfig.containsKey(requiredKey)) {
              throw new BadFormatException(
                  "Not found key: " + requiredKey + " of image-config in Metadata.yaml");
            }
            if (imageConfig.get(requiredKey) == null) {
              throw new NullPointerException(
                  "Not defined value of key: " + requiredKey + " of image-config in Metadata.yaml");
            }
          }
          NFVImage image = new NFVImage();
          image.setName((String) imageConfig.get("name"));
          image.setDiskFormat(((String) imageConfig.get("diskFormat")).toUpperCase());
          image.setContainerFormat(((String) imageConfig.get("containerFormat")).toUpperCase());
          image.setMinCPU(Integer.toString((Integer) imageConfig.get("minCPU")));
          image.setMinDiskSpace((Integer) imageConfig.get("minDisk"));
          image.setMinRam((Integer) imageConfig.get("minRam"));
          image.setIsPublic(
              Boolean.parseBoolean(Integer.toString((Integer) imageConfig.get("minRam"))));
          vnfPackage.setImage(image);
        } else {
          throw new BadFormatException(
              "The image-config is not defined. Please define it to upload a new image");
        }
      } else {
        vnfPackage.setImage(new NFVImage());
      }
    }

    return vnfPackage;
  }

  @Override
  public BaseNfvImage handleImage(
      VNFPackage vnfPackage,
      byte[] imageFile,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      Map<String, Object> metadata,
      String projectId)
      throws NotFoundException, PluginException, VimException, BadRequestException, IOException,
          AlreadyExistingException, InterruptedException, ExecutionException, BadFormatException {

    NFVImage image = (NFVImage) vnfPackage.getImage();
    if (vnfPackage.getScriptsLink() != null) {
      if (vnfPackage.getScripts() != null && !vnfPackage.getScripts().isEmpty()) {
        log.debug(
            "VNFPackageManagement: Remove scripts got by scripts/ because the scripts-link is defined");
        vnfPackage.setScripts(new HashSet<>());
      }
    }
    Map<String, Object> imageDetails = (Map<String, Object>) metadata.get("image");

    if (imageDetails.get("upload").equals("check")) {
      throw new BadFormatException("Option 'check' selected is not available use true or false");
    }

    virtualNetworkFunctionDescriptor
        .getVdu()
        .parallelStream()
        .filter(virtualDeploymentUnit -> virtualDeploymentUnit.getVimInstanceName() == null)
        .forEach(
            virtualDeploymentUnit -> virtualDeploymentUnit.setVimInstanceName(new HashSet<>()));

    if (imageDetails.get("upload").equals("true")) {
      log.info("Uploading a new Image");
      if (vnfPackage.getImageLink() == null && imageFile == null) {
        throw new NotFoundException(
            "VNFPackageManagement: Neither the image link is defined nor the image file is available. Please define "
                + "at least one if you want to upload a new image");
      }

      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
        if (vdu.getVimInstanceName() != null && !vdu.getVimInstanceName().isEmpty()) {
          for (String vimName : vdu.getVimInstanceName()) {
            BaseVimInstance vimInstance =
                vimInstanceRepository.findByProjectIdAndName(projectId, vimName);
            Vim vim = vimBroker.getVim(vimInstance.getType());
            if (vnfPackage.getImageLink() != null
                && virtualNetworkFunctionDescriptor.getVdu() != null) {
              log.debug("Uploading a new Image by using the image link");
              image = vim.add(vimInstance, image, vnfPackage.getImageLink());
            } else if (imageFile != null) {
              log.debug("Uploading a new Image by using the image file");
              image = vim.add(vimInstance, image, imageFile);
            }
            if (vdu.getVm_image() == null) {
              vdu.setVm_image(new LinkedHashSet<>());
            }
            vdu.getVm_image().add(image.getExtId());
            vimInstance = vimManagement.refresh(vimInstance, true).get();
            imageChecker.checkImageStatus(vimInstance);
          }
        } else {
          throw new BadRequestException(
              "Upload was specified 'true' but no vimInstanceName was specified in the vdu. Please specify where to upload the image");
        }
      }
    } else {
      List<String> names = (List<String>) imageDetails.get("names");
      List<String> ids = (List<String>) imageDetails.get("ids");
      virtualNetworkFunctionDescriptor
          .getVdu()
          .forEach(
              virtualDeploymentUnit -> {
                if (virtualDeploymentUnit.getVm_image() == null) {
                  virtualDeploymentUnit.setVm_image(new LinkedHashSet<>());
                }
                if (ids != null) virtualDeploymentUnit.getVm_image().addAll(ids);
                if (names != null) virtualDeploymentUnit.getVm_image().addAll(names);
              });
    }
    return image;
  }

  public VirtualNetworkFunctionDescriptor onboardFromMarket(String link, String projectId)
      throws IOException, AlreadyExistingException, IncompatibleVNFPackage, VimException,
          NotFoundException, PluginException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, BadFormatException {
    log.debug("This is download link" + link);
    URL packageLink;
    try {
      packageLink = new URL(link);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new BadFormatException("The provided link " + link + " is not a valid URL.");
    }

    byte[] packageOnboard;
    try (InputStream in = new BufferedInputStream(packageLink.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] bytes = new byte[1024];
      int n;
      while (-1 != (n = in.read(bytes))) {
        out.write(bytes, 0, n);
      }
      packageOnboard = out.toByteArray();
    }
    log.debug("Downloaded " + packageOnboard.length + " bytes");
    VirtualNetworkFunctionDescriptor vnfd;
    try {
      vnfd = add(packageOnboard, false, projectId, true);
    } catch (ExistingVNFPackage | VNFPackageFormatException | ExecutionException e) {
      if (log.isDebugEnabled()) log.error(e.getMessage(), e);
      else log.error(e.getMessage());
      throw new BadRequestException(e.getMessage());
    }
    return vnfd;
  }

  @Override
  public VirtualNetworkFunctionDescriptor onboardFromPackageRepository(
      String link, String projectId)
      throws IOException, AlreadyExistingException, IncompatibleVNFPackage, VimException,
          NotFoundException, PluginException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException {
    log.debug("Onboard from Package Repository, this is the download link: " + link);
    URL packageLink = new URL(link);

    InputStream in = new BufferedInputStream(packageLink.openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int n;
    while (-1 != (n = in.read(bytes))) {
      out.write(bytes, 0, n);
    }
    out.close();
    in.close();
    byte[] packageOnboard = out.toByteArray();
    log.debug("Downloaded " + packageOnboard.length + " bytes");
    VirtualNetworkFunctionDescriptor vnfd;
    try {
      vnfd = add(packageOnboard, false, projectId, false);
    } catch (ExistingVNFPackage
        | BadFormatException
        | VNFPackageFormatException
        | ExecutionException e) {
      if (log.isDebugEnabled()) log.error(e.getMessage(), e);
      else log.error(e.getMessage());
      throw new BadRequestException(e.getMessage());
    }
    return vnfd;
  }

  private String getNfvoVersionWithoutSNAPSHOT() throws NotFoundException {
    String version = VNFPackageManagement.class.getPackage().getImplementationVersion();
    //this is because you are running it into an IDE
    if (version == null) {
      if (nfvoVersion == null || nfvoVersion.equals("null") || nfvoVersion.isEmpty()) {
        throw new NotFoundException(
            "The NFVO version number is not available, seems you are running the NFVO from the IDE. Set nfvo.version "
                + "property into the NFVO property file.");
      } else {
        version = nfvoVersion;
      }
    }
    return version.lastIndexOf("-SNAPSHOT") != -1
        ? version.substring(0, version.lastIndexOf("-SNAPSHOT"))
        : version;
  }

  @Override
  public void disable() {}

  @Override
  public void enable() {}

  @Override
  public VNFPackage update(String id, VNFPackage pack_new, String projectId)
      throws NotFoundException {
    VNFPackage old = vnfPackageRepository.findFirstByIdAndProjectId(id, projectId);
    if (old == null) throw new NotFoundException("No VNFPackage found with ID " + id);
    old.setName(pack_new.getName());
    old.setImage(pack_new.getImage());
    vnfPackageRepository.save(old);
    return old;
  }

  @Override
  public VNFPackage query(String id, String projectId) {
    return vnfPackageRepository.findFirstByIdAndProjectId(id, projectId);
  }

  @Override
  public Iterable<VNFPackage> query() {
    return vnfPackageRepository.findAll();
  }

  @Override
  public Iterable<VNFPackageMetadata> query(
      String name,
      String vendor,
      String version,
      String nfvoVersion,
      String vnfmType,
      String osId,
      String osVersion,
      String osArchitecture,
      String tag,
      String projectId) {
    return vnfPackageMetadataRepository
        .findAllByNameAndVendorAndVersionAndNfvoVersionAndVnfmTypeAndOsIdAndOsVersionAndOsArchitectureAndTagAndProjectId(
            name,
            vendor,
            version,
            nfvoVersion,
            vnfmType,
            osId,
            osVersion,
            osArchitecture,
            tag,
            projectId);
  }

  @Override
  public void delete(String id, String projectId) throws WrongAction {
    log.info("Removing VNFPackage: " + id);
    VNFPackage vnfPackage = vnfPackageRepository.findFirstById(id);
    if (vnfPackage == null || !vnfPackage.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "Not found VNFPackage " + id + ". Either not existing or not under the project chosen.");
    }
    //TODO remove image in the VIM
    Iterable<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors =
        vnfdRepository.findAll();
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        virtualNetworkFunctionDescriptors) {
      if (virtualNetworkFunctionDescriptor.getVnfPackageLocation() == null) {
        log.trace(
            "VNFPackageLocation not defined for VNFD " + virtualNetworkFunctionDescriptor.getId());
      } else if (virtualNetworkFunctionDescriptor.getVnfPackageLocation().equals(id)) {
        log.trace("VNFPackageLocation references VNFD " + virtualNetworkFunctionDescriptor.getId());
        if (cascadeDelete) {
          log.debug("VNFD " + virtualNetworkFunctionDescriptor.getId() + " will be removed");
        } else {
          log.debug("VNFD " + virtualNetworkFunctionDescriptor.getId() + " will not be removed");
        }
      }
    }
    if (cascadeDelete) {
      for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
          virtualNetworkFunctionDescriptors) {
        if (virtualNetworkFunctionDescriptor.getVnfPackageLocation() != null) {
          if (virtualNetworkFunctionDescriptor.getVnfPackageLocation().equals(id)) {
            if (!vnfdBelongsToNSD(virtualNetworkFunctionDescriptor)) {
              log.info("Removing VNFDescriptor: " + virtualNetworkFunctionDescriptor.getName());
              vnfdRepository.delete(virtualNetworkFunctionDescriptor.getId());
              break;
            } else {
              throw new WrongAction(
                  "It is not possible to remove a vnfPackage --> vnfdescriptor if the NSD is still onboarded");
            }
          }
        }
      }
    }
    vnfPackageRepository.delete(id);
  }

  private boolean vnfdBelongsToNSD(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
    for (NetworkServiceDescriptor networkServiceDescriptor : nsdRepository.findAll()) {
      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        if (vnfd.getId().equals(virtualNetworkFunctionDescriptor.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Script updateScript(Script script, String vnfPackageId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {

    script = scriptRepository.save(script);
    vnfmManager.updateScript(script, vnfPackageId);
    return script;
  }

  @Override
  public Iterable<VNFPackage> queryByProjectId(String projectId) {
    return vnfPackageRepository.findByProjectId(projectId);
  }
}
