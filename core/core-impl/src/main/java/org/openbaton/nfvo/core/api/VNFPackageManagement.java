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

package org.openbaton.nfvo.core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.core.utils.CheckVNFDescriptor;
import org.openbaton.nfvo.core.utils.CheckVNFPackage;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.core.utils.Utils;
import org.openbaton.nfvo.repositories.*;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/** Created by lto on 22/07/15. */
@Service
@Scope
@ConfigurationProperties
@EnableAsync
public class VNFPackageManagement
    implements org.openbaton.nfvo.core.interfaces.VNFPackageManagement {

  @Value("${vnfd.vnfp.cascade.delete:false}")
  private boolean cascadeDelete;
  // This is only in case you run the NFVO from IDE
  @Value("${nfvo.version:null}")
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
  @Autowired private VnfPlacementManagement vnfPlacementManagement;
  @Autowired private VNFPackageMetadataRepository vnfPackageMetadataRepository;

  @Autowired private org.openbaton.nfvo.core.interfaces.VimManagement vimManagement;

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public void setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
  }

  @Override
  public synchronized VirtualNetworkFunctionDescriptor onboard(byte[] pack, String projectId)
      throws IOException, VimException, NotFoundException, PluginException, IncompatibleVNFPackage,
          AlreadyExistingException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, EntityUnreachableException {
    log.info("Onboarding VNF Package...");
    for (VimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId))
      vimManagement.refresh(vimInstance);
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
          imageDetails = handleMetadata(metadata, vnfPackage, imageDetails, image);

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
          log.trace("Created VNFD: " + virtualNetworkFunctionDescriptor);
          nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor, projectId);
        } else if (entry.getName().endsWith(".img")) {
          //this must be the image
          //and has to be upladed to the RIGHT vim
          imageFile = content;
          log.debug("imageFile is: " + entry.getName());
          throw new VimException(
              "Uploading an image file from the VNFPackage is not supported at this moment. Please use the image link"
                  + ".");
        } else if (entry.getName().startsWith("scripts/")) {
          Script script = new Script();
          script.setName(entry.getName().substring(8));
          script.setPayload(content);
          vnfPackage.getScripts().add(script);
        }
      }
    }

    handleImage(
        vnfPackage,
        imageFile,
        virtualNetworkFunctionDescriptor,
        metadata,
        image,
        imageDetails,
        projectId);

    vnfPackage.setImage(image);
    myTarFile.close();
    virtualNetworkFunctionDescriptor.setProjectId(projectId);
    vnfPackage.setProjectId(projectId);
    for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findByProjectId(projectId)) {
      if (vnfd.getVendor().equals(virtualNetworkFunctionDescriptor.getVendor())
          && vnfd.getName().equals(virtualNetworkFunctionDescriptor.getName())
          && vnfd.getVersion().equals(virtualNetworkFunctionDescriptor.getVersion())) {
        throw new AlreadyExistingException(
            "A VNF with this vendor, name and version is already existing");
      }
    }

    nsdUtils.checkIntegrity(virtualNetworkFunctionDescriptor);

    vnfPackageRepository.save(vnfPackage);
    virtualNetworkFunctionDescriptor.setVnfPackageLocation(vnfPackage.getId());
    virtualNetworkFunctionDescriptor = vnfdRepository.save(virtualNetworkFunctionDescriptor);
    log.trace("Persisted " + virtualNetworkFunctionDescriptor);
    log.trace(
        "Onboarded VNFPackage ("
            + virtualNetworkFunctionDescriptor.getVnfPackageLocation()
            + ") successfully");
    return virtualNetworkFunctionDescriptor;
  }

  @Override
  public VirtualNetworkFunctionDescriptor add(
      byte[] pack, boolean isImageIncluded, String projectId, boolean fromMarketPlace)
      throws IOException, VimException, NotFoundException, SQLException, PluginException,
          ExistingVNFPackage, DescriptorWrongFormat, VNFPackageFormatException,
          IncompatibleVNFPackage, BadRequestException, AlreadyExistingException,
          NetworkServiceIntegrityException {

    CheckVNFPackage.checkStructure(pack, isImageIncluded, fromMarketPlace);

    VNFPackage vnfPackage = new VNFPackage();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;
    VNFPackageMetadata vnfPackageMetadata = null;
    NFVImage image = new NFVImage();
    ImageMetadata imageMetadata = new ImageMetadata();

    Map<String, Object> imageDetails = null;
    Map<String, Object> metadata = null;

    try (ArchiveInputStream tarFile =
        new ArchiveStreamFactory()
            .createArchiveInputStream("tar", new ByteArrayInputStream(pack))) {
      TarArchiveEntry entry;
      // Here there are almost no checks whether keys exists or not, since the check has been done in the CheckVNFPackage class
      while ((entry = (TarArchiveEntry) tarFile.getNextEntry()) != null) {
        if (entry.isFile() && !entry.getName().startsWith("./._")) {
          byte[] content = new byte[(int) entry.getSize()];
          tarFile.read(content, 0, content.length);
          if (entry.getName().equals("Metadata.yaml")) {
            metadata = Utils.getMapFromYamlFile(content);
            vnfPackage.setName((String) metadata.get("name"));

            vnfPackageMetadata = new VNFPackageMetadata();

            vnfPackageMetadata.setName((String) metadata.get("name"));
            vnfPackage.setName((String) metadata.get("name"));

            //Check version compatibility between VNF Package and actual NFVO
            String vnfPackageNFVOVersion = null;
            if (metadata.containsKey("nfvo-version")) {
              vnfPackageNFVOVersion = (String) metadata.get("nfvo-version");
              CheckVNFPackage.compareNFVOVersions(
                  vnfPackageNFVOVersion, getNfvoVersionWithoutSNAPSHOT());
            }
            if (metadata.containsKey("nfvo_version")) {
              vnfPackageNFVOVersion = (String) metadata.get("nfvo_version");
              CheckVNFPackage.compareNFVOVersions(
                  vnfPackageNFVOVersion, getNfvoVersionWithoutSNAPSHOT());
            }
            vnfPackage.setNfvo_version(vnfPackageNFVOVersion);
            vnfPackageMetadata.setNfvoVersion(vnfPackageNFVOVersion);

            if (metadata.containsKey("vendor")) {
              vnfPackageMetadata.setVendor((String) metadata.get("vendor"));
            } else {
              vnfPackageMetadata.setVendor((String) metadata.get("provider"));
            }

            if (fromMarketPlace) vnfPackageMetadata.setVersion("1.0");
            else vnfPackageMetadata.setVersion(String.valueOf(metadata.get("version")));

            if (metadata.containsKey("vim-types")) {
              vnfPackageMetadata.setVimTypes(
                  new HashSet<String>((ArrayList) metadata.get("vim-types")));
              vnfPackage.setVimTypes((ArrayList) metadata.get("vim-types"));
            } else {
              vnfPackageMetadata.setVimTypes(
                  new HashSet<String>((ArrayList) metadata.get("vim_types")));
              vnfPackage.setVimTypes((ArrayList) metadata.get("vim_types"));
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
                if (rci.containsKey("key-url"))
                  additionalRepoInfo.setKeyUrl((String) rci.get("key-url"));
                List<String> configurationInfo = (List<String>) rci.get("configuration");
                additionalRepoInfo.setConfiguration(configurationInfo);
                vnfPackageMetadata.addRepoConfigurationInfo(additionalRepoInfo);
              }
            }

            imageDetails = (Map<String, Object>) metadata.get("image");
            //            try {
            //              imageDetails = handleMetadata(metadata, vnfPackage, imageDetails, image);
            //            } catch (IncompatibleVNFPackage incompatibleVNFPackage) {
            //              incompatibleVNFPackage.printStackTrace();
            //            }

            //imageMetadata.setUsername(userManagement.getCurrentUser());
            imageMetadata.setUpload((String) imageDetails.get("option"));

            if (imageDetails.containsKey("ids")) {
              imageMetadata.setIds((Set<String>) imageDetails.get("ids"));
            } else {
              imageMetadata.setIds(new HashSet<String>());
            }
            if (imageDetails.containsKey("names")) {
              imageMetadata.setNames(new HashSet<String>((ArrayList) imageDetails.get("names")));
            } else {
              imageMetadata.setNames(new HashSet<String>());
            }
            if (imageDetails.containsKey("link")) {
              imageMetadata.setLink((String) imageDetails.get("link"));
            } else {
              imageMetadata.setLink(null);
            }

            //If upload==true -> create a new Image
            if (imageDetails.get("upload").equals("true")
                || imageDetails.get("upload").equals("check")) {
              vnfPackage.setImageLink((String) imageDetails.get("link"));
              Map<String, Object> imageConfig = (Map<String, Object>) metadata.get("image-config");

              image.setName((String) imageConfig.get("name"));
              image.setDiskFormat(((String) imageConfig.get("diskFormat")).toUpperCase());
              image.setContainerFormat(((String) imageConfig.get("containerFormat")).toUpperCase());

              image.setMinCPU(Integer.toString((Integer) imageConfig.get("minCPU")));
              image.setMinDiskSpace(Long.parseLong(imageConfig.get("minDisk").toString()));
              image.setMinRam(Long.parseLong(imageConfig.get("minRam").toString()));

              image.setIsPublic((boolean) imageConfig.get("isPublic"));
            }
          } else if (!entry.getName().startsWith("scripts/") && entry.getName().endsWith(".json")) {
            //this must be the vnfd
            String json = new String(content);
            CheckVNFDescriptor.checkIntegrity(json);

            virtualNetworkFunctionDescriptor =
                mapper.fromJson(json, VirtualNetworkFunctionDescriptor.class);

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
            log.trace("Created VNFD: " + virtualNetworkFunctionDescriptor);
            nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor, projectId);

            vnfPackageMetadata.setVnfmType(virtualNetworkFunctionDescriptor.getEndpoint());

            //vnfPackageMetadata.setVnfd(virtualNetworkFunctionDescriptor);

          } else if (entry.getName().startsWith("scripts/")) {
            Script script = new Script();
            script.setName(entry.getName().substring(8));
            script.setPayload(content);
            if (vnfPackage.getScripts() == null) vnfPackage.setScripts(new HashSet<Script>());
            vnfPackage.getScripts().add(script);
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

    handleImage(
        vnfPackage,
        null,
        virtualNetworkFunctionDescriptor,
        metadata,
        image,
        imageDetails,
        projectId);

    if (vnfPackage.getScriptsLink() != null
        && (vnfPackage.getScripts() != null && vnfPackage.getScripts().size() > 0)) {
      log.debug("Remove scripts got by scripts/ because the scripts-link is defined");
      vnfPackage.setScripts(new HashSet<Script>());
    }

    Map<String, Object> vnfPackageMetadataParameters = new HashMap<>();
    vnfPackageMetadataParameters.put("name", vnfPackageMetadata.getName());
    vnfPackageMetadataParameters.put("vendor", vnfPackageMetadata.getVendor());

    Map<String, Object> vnfdParameters = new HashMap<>();
    vnfdParameters.put("name", virtualNetworkFunctionDescriptor.getName());
    vnfdParameters.put("vendor", virtualNetworkFunctionDescriptor.getVendor());
    CheckVNFPackage.checkCommonParametersWithVNFD(vnfPackageMetadataParameters, vnfdParameters);

    //vnfPackageMetadata.setVnfPackage(vnfPackage);
    //vnfPackageMetadata.setNfvImage(image);

    //vnfPackageMetadata.setImageMetadata(imageMetadata);

    //vnfPackageMetadata.setVnfPackageFileName(fileName);
    vnfPackageMetadata.setProjectId(projectId);
    //vnfPackageMetadata.setVnfPackageFile(pack);
    //vnfPackageMetadata.setMd5sum(DigestUtils.md5DigestAsHex(pack));
    vnfPackageMetadata.setType("tar");
    vnfPackage.setVnfPackageMetadata(vnfPackageMetadata);
    vnfPackage.setImage(image);

    virtualNetworkFunctionDescriptor.setProjectId(projectId);
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

    nsdUtils.checkIntegrity(virtualNetworkFunctionDescriptor);
    // check if it is the first and set to default
    //    if(!vnfPackageMetadataRepository.findAllByNameAndVendor(vnfPackageMetadata.getName(),vnfPackageMetadata.getVendor()).iterator().hasNext()){
    //      log.debug("Setting VNF package to default");
    //      vnfPackageMetadata.setDefaultFlag(true);
    //    }
    //    else vnfPackageMetadata.setDefaultFlag(false);
    //vnfPackageMetadataRepository.save(vnfPackageMetadata);
    vnfPackage = vnfPackageRepository.save(vnfPackage);
    log.trace("Persisted " + vnfPackage);
    vnfPackageMetadataRepository.setVNFPackageId(vnfPackage.getId());

    virtualNetworkFunctionDescriptor.setVnfPackageLocation(vnfPackage.getId());
    virtualNetworkFunctionDescriptor = setIPConfigurations(virtualNetworkFunctionDescriptor);
    virtualNetworkFunctionDescriptor = vnfdRepository.save(virtualNetworkFunctionDescriptor);
    log.trace("Persisted " + virtualNetworkFunctionDescriptor);
    log.trace(
        "Onboarded VNFPackage ("
            + virtualNetworkFunctionDescriptor.getVnfPackageLocation()
            + ") successfully");
    return virtualNetworkFunctionDescriptor;
  }

  private VirtualNetworkFunctionDescriptor setIPConfigurations(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor)
      throws DescriptorWrongFormat {
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

  @Override
  public Map<String, Object> handleMetadata(
      Map<String, Object> metadata,
      VNFPackage vnfPackage,
      Map<String, Object> imageDetails,
      NFVImage image)
      throws IncompatibleVNFPackage, NotFoundException {

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
    String[] actualNfvoVersion;
    try {
      actualNfvoVersion = getNfvoVersionSplitted();
    } catch (NotFoundException ne) {
      log.warn(ne.getMessage());
      actualNfvoVersion = null;
    }

    vnfPackage.setName((String) metadata.get("name"));
    if (metadata.containsKey("nfvo_version") && actualNfvoVersion != null) {

      String nfvoVersionString = (String) metadata.get("nfvo_version");
      String[] nfvoVersion = nfvoVersionString.split(Pattern.quote("."));

      if (nfvoVersion[0].equals(actualNfvoVersion[0])
          && nfvoVersion[1].equals(actualNfvoVersion[1])) {
        vnfPackage.setNfvo_version(nfvoVersionString);
      } else {
        throw new IncompatibleVNFPackage(
            "The NFVO Version: "
                + nfvoVersion[0]
                + "."
                + nfvoVersion[1]
                + ".X"
                + " specified in the Metadata"
                + " is not compatible with the this NFVOs version: "
                + actualNfvoVersion[0]
                + "."
                + actualNfvoVersion[1]
                + ".X");
      }
    } else {
      //TODO throw exception
      log.warn(
          "Missing 'nfvo_version' parameter in the vnfpackage, or the orchestrator does not expose its version number");
    }

    if (metadata.containsKey("scripts-link")) {
      vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
    }
    if (metadata.containsKey("vim_types")) {
      List<String> vimTypes = (List<String>) metadata.get("vim_types");
      vnfPackage.setVimTypes(vimTypes);
    } else {
      log.warn("vim_types is not specified! it is not possible to check the vim");
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

    return imageDetails;
  }

  @Override
  public void handleImage(
      VNFPackage vnfPackage,
      byte[] imageFile,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      Map<String, Object> metadata,
      NFVImage image,
      Map<String, Object> imageDetails,
      String projectId)
      throws NotFoundException, PluginException, VimException, BadRequestException, IOException,
          AlreadyExistingException, InterruptedException, EntityUnreachableException {
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
            "VNFPackageManagement: For option upload=check you must define an image. Neither the image link is "
                + "defined nor the image file is available. Please define at least one if you want to upload a new image");
      }
    }

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
      if (vdu.getVimInstanceName() == null || vdu.getVimInstanceName().isEmpty()) {
        vdu.setVimInstanceName(new ArrayList<String>());
        vdu.getVimInstanceName()
            .addAll(
                vnfPlacementManagement.chose(
                    virtualNetworkFunctionDescriptor, vnfPackage.getVimTypes(), projectId));
      } else {
        if (vnfPackage.getVimTypes() != null && !vnfPackage.getVimTypes().isEmpty()) {
          for (String vimInstanceName : vdu.getVimInstanceName()) {
            for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
              if (vi.getName().equals(vimInstanceName)) {
                if (!vnfPackage.getVimTypes().contains(vi.getType())) {
                  throw new BadRequestException(
                      "A VIM defined in VNFD "
                          + virtualNetworkFunctionDescriptor.getName()
                          + " is not supported based on the VIM types provided in the VNF Package");
                }
              }
            }
          }
        }
      }
    }
    if (imageDetails.get("upload").equals("true")) {
      log.debug("VNFPackageManagement: Uploading a new Image");
      if (vnfPackage.getImageLink() == null && imageFile == null) {
        throw new NotFoundException(
            "VNFPackageManagement: Neither the image link is defined nor the image file is available. Please define "
                + "at least one if you want to upload a new image");
      } else if (vnfPackage.getImageLink() != null
          && virtualNetworkFunctionDescriptor.getVdu() != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image link");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
          if (vdu.getVimInstanceName() != null && !vdu.getVimInstanceName().isEmpty()) {
            for (String vimName : vdu.getVimInstanceName()) {
              VimInstance vimInstance = null;

              for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
                if (vimName.equals(vi.getName())) {
                  vimInstance = vi;
                }
              }

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
                vimManagement.refresh(vimInstance);

                imageChecker.checkImageStatus(vimInstance);
              }
            }
          }
        }
      } else if (imageFile != null) {
        log.debug("VNFPackageManagement: Uploading a new Image by using the image file");
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
          if (vdu.getVimInstanceName() != null && !vdu.getVimInstanceName().isEmpty()) {
            for (String vimName : vdu.getVimInstanceName()) {
              VimInstance vimInstance = null;

              for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
                if (vimName.equals(vi.getName())) {
                  vimInstance = vi;
                }
              }

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
                vdu.getVm_image().add(image.getExtId());
                vimInstances.add(vimInstance.getId());
                vimManagement.refresh(vimInstance);

                imageChecker.checkImageStatus(vimInstance);
              }
            }
          }
        }
      }
    } else {
      if (imageDetails.get("upload").equals("check")
          && !imageDetails.containsKey("ids")
          && !imageDetails.containsKey("names")) {
        throw new NotFoundException(
            "VNFPackageManagement: Upload option 'check' requires at least a list of ids or names to find "
                + "the right image.");
      }
      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {

        List<String> vimInstanceNames = vdu.getVimInstanceName();

        for (String vimName : vimInstanceNames) {

          VimInstance vimInstance = null;

          for (VimInstance vi : vimInstanceRepository.findByProjectId(projectId)) {
            if (vimName.equals(vi.getName())) {
              vimInstance = vi;
            }
          }

          if (vimInstance == null) {
            throw new NotFoundException(
                "Vim Instance with name " + vimName + " was not found in project: " + projectId);
          }

          boolean found = false;
          vimManagement.refresh(vimInstance);
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
          if (!found) {
            for (NFVImage nfvImage : vimInstance.getImages()) {
              if (vdu.getVm_image().contains(nfvImage.getName())) {
                if (!found) {
                  found = true;
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
                log.debug("VNFPackageManagement: Uploading a new Image by using the image link");
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
                  vimManagement.refresh(vimInstance);

                  imageChecker.checkImageStatus(vimInstance);
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
                  if (vdu.getVm_image() == null) {
                    vdu.setVm_image(new HashSet<String>());
                  }
                  vimInstances.add(vimInstance.getId());
                  vdu.getVm_image().add(image.getExtId());
                  vimManagement.refresh(vimInstance);

                  imageChecker.checkImageStatus(vimInstance);
                }
              }
            } else {
              throw new NotFoundException(
                  "VNFPackageManagement: Neither the defined image ids nor the names were found. Use upload option "
                      + "'check' to get sure that the image will be available");
            }
          } else {
            log.debug(
                "Image "
                    + image.getName()
                    + " provided in uploaded package "
                    + vnfPackage.getName()
                    + " is available on VIM");
          }

          //        } else { // vimInstanceName is not defined, just put the name into the vdu
          if (imageDetails.containsKey("names")) {
            List names = (List) imageDetails.get("names");
            log.debug("Adding names: " + names);
            vdu.getVm_image().addAll(names);
          } else if (imageDetails.containsKey("ids")) {
            List ids = (List) imageDetails.get("ids");
            log.debug("Adding ids: " + ids);
            vdu.getVm_image().addAll(ids);
          } else {
            log.debug(
                "Neither names or ids are provided in the VNF Package. Just onboard without any images from the package.");
          }
        }
      }
    }
  }

  public VirtualNetworkFunctionDescriptor onboardFromMarket(String link, String projectId)
      throws IOException, AlreadyExistingException, IncompatibleVNFPackage, VimException,
          NotFoundException, PluginException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, EntityUnreachableException {
    log.debug("This is download link" + link);
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
      vnfd = add(packageOnboard, false, projectId, true);
    } catch (SQLException
        | ExistingVNFPackage
        | DescriptorWrongFormat
        | VNFPackageFormatException e) {
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
          NotFoundException, PluginException, NetworkServiceIntegrityException,
          BadRequestException {
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
    } catch (SQLException
        | ExistingVNFPackage
        | DescriptorWrongFormat
        | VNFPackageFormatException e) {
      if (log.isDebugEnabled()) log.error(e.getMessage(), e);
      else log.error(e.getMessage());
      throw new BadRequestException(e.getMessage());
    }
    return vnfd;
  }

  private String[] getNfvoVersionSplitted() throws NotFoundException {
    return getNfvoVersionWithoutSNAPSHOT().split(Pattern.quote("."));
  }

  private String getNfvoVersionWithoutSNAPSHOT() throws NotFoundException {
    String version = VNFPackageManagement.class.getPackage().getImplementationVersion();
    //this is because you are running it into an IDE
    if (version == null) {
      if (nfvoVersion.equals("null"))
        throw new NotFoundException(
            "The NFVO version number is not available, seems you are running the NFVO from the IDE. Set nfvo.version property into the NFVO property file.");
      else version = nfvoVersion;
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
  public VNFPackage update(String id, VNFPackage pack_new, String projectId) {
    VNFPackage old = vnfPackageRepository.findFirstById(id);
    if (!old.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFPackage not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    old.setName(pack_new.getName());
    old.setImage(pack_new.getImage());
    return old;
  }

  @Override
  public VNFPackage query(String id, String projectId) {
    VNFPackage vnfPackage = vnfPackageRepository.findFirstById(id);
    if (!vnfPackage.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFPackage not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    return vnfPackage;
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
        //        throw new WrongAction(
        //            "It is not possible to remove the vnfPackage with id "
        //                + id
        //                + ", a VNFD referencing it is still onboarded");
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
  public Script updateScript(Script script, String vnfPackageId) throws NotFoundException {

    script = scriptRepository.save(script);
    vnfmManager.updateScript(script, vnfPackageId);
    return script;
  }

  @Override
  public Iterable<VNFPackage> queryByProjectId(String projectId) {
    return vnfPackageRepository.findByProjectId(projectId);
  }
}
