package org.openbaton.nfvo.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.VNFPackageFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unsafe", "unchecked"})
public class CheckVNFPackage {
  // Identification required keys
  private static final String[] REQUIRED_VNF_PACKAGE_IDENTIFIER_KEYS = new String[] {"name"};
  // Required common keys between vnf package and vnfd
  // Basically, those keys must be present and be equals in the vnf package and the relative vnfd
  private static final String[] REQUIRED_VNF_PACKAGE_AND_VNFD_COMMON_KEYS =
      new String[] {"name", "vendor"};
  // Required keys for Open Baton
  private static final String[] REQUIRED_VNF_PACKAGE_KEYS_FOR_OPENBATON = new String[] {"image"};

  // Required keys for image
  private static final String[] REQUIRED_IMAGE_DETAILS = new String[] {"upload"};
  // Required keys for image-config
  private static final String[] REQUIRED_IMAGE_CONFIG =
      new String[] {
        "name", "diskFormat", "containerFormat", "minCPU", "minDisk", "minRam", "isPublic"
      };
  private static final Logger log = LoggerFactory.getLogger(CheckVNFPackage.class);

  private static void checkRequiredFirstLevelMetadataKeys(
      Map<String, Object> metadata, String[] keys) throws NotFoundException {
    for (String requiredKey : keys) {
      if (metadata.get(requiredKey) == null) {
        throw new NullPointerException(
            "Not defined " + requiredKey + " of VNFPackage in Metadata.yaml, this is mandatory");
      }
    }
  }

  private static void checkRequiredImageDetailsKeys(Map<String, Object> imageDetails)
      throws NotFoundException {
    for (String requiredKey : REQUIRED_IMAGE_DETAILS) {
      if (imageDetails.get(requiredKey) == null) {
        throw new NullPointerException(
            "Not defined " + requiredKey + " of image-config in Metadata.yaml");
      }
    }
  }

  private static void checkRequiredImageConfigKeys(Map<String, Object> imageConfig)
      throws NotFoundException {
    for (String requiredKey : REQUIRED_IMAGE_CONFIG) {
      if (imageConfig.get(requiredKey) == null) {
        throw new NullPointerException("Not defined " + requiredKey + " of image in Metadata.yaml");
      }
    }
  }

  public static void checkCommonParametersWithVNFD(
      Map<String, Object> vnfdParameters, Map<String, Object> vnfPackageMetadataParameters)
      throws NotFoundException, VNFPackageFormatException {
    for (String commonKey : REQUIRED_VNF_PACKAGE_AND_VNFD_COMMON_KEYS) {
      String vnfdCommonKey = (String) vnfdParameters.get(commonKey);
      String vnfPackageCommonKey = (String) vnfPackageMetadataParameters.get(commonKey);
      if (vnfdCommonKey == null || vnfPackageCommonKey == null)
        throw new NullPointerException(
            "Not defined " + commonKey + " in VNFD or VNF package metadata");
      if (!vnfdCommonKey.equals(vnfPackageCommonKey))
        throw new VNFPackageFormatException(
            "VNFD and VNF package has different '" + commonKey + "', it must be the same");
    }
  }

  public static void checkStructure(byte[] archive, boolean imageIncluded, boolean fromMarketPlace)
      throws VNFPackageFormatException {

    if (archive == null || archive.length == 0)
      throw new VNFPackageFormatException("VnfPackage null or empty");

    boolean vnfdFound = false;
    boolean metadataFound = false;

    try (ArchiveInputStream tarFile =
        new ArchiveStreamFactory()
            .createArchiveInputStream("tar", new ByteArrayInputStream(archive))) {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarFile.getNextEntry()) != null) {
        if (entry.isFile() && !entry.getName().startsWith("./._")) {
          log.debug("file inside tar: " + entry.getName());

          byte[] content = new byte[(int) entry.getSize()];
          tarFile.read(content, 0, content.length);

          if (entry.getName().equals("Metadata.yaml")) {
            metadataFound = true;

            Map<String, Object> metadata = Utils.getMapFromYamlFile(content);

            CheckVNFPackage.checkRequiredFirstLevelMetadataKeys(
                metadata, REQUIRED_VNF_PACKAGE_IDENTIFIER_KEYS);
            CheckVNFPackage.checkRequiredFirstLevelMetadataKeys(
                metadata, REQUIRED_VNF_PACKAGE_KEYS_FOR_OPENBATON);
            // throw an exception if either vim-types and vim_types are not provided, or both are provided together.
            if (metadata.containsKey("vim-types") == metadata.containsKey("vim_types"))
              throw new VNFPackageFormatException("vim-types is not provided");

            if (metadata.containsKey("vendor") == metadata.containsKey("provider"))
              throw new VNFPackageFormatException(
                  "\"vendor\" or \"provider\" is not specified in the VNF Package Metadata");

            if (metadata.containsKey("nfvo-version") == metadata.containsKey("nfvo_version"))
              throw new VNFPackageFormatException(
                  "nfvo-version and nfvo_version are both provided or missing. Please specify only one nfvo-version");

            Map<String, Object> imageDetails = (Map<String, Object>) metadata.get("image");

            checkRequiredImageDetailsKeys(imageDetails);

            if (imageDetails.get("upload").equals("true")
                || imageDetails.get("upload").equals("check")) {
              if (!metadata.containsKey("image-config"))
                throw new VNFPackageFormatException(
                    "The image-config is not defined. Please define it to upload a new image");
              log.debug("image-config: " + metadata.get("image-config"));
              Map<String, Object> imageConfig = (Map<String, Object>) metadata.get("image-config");
              checkRequiredImageConfigKeys(imageConfig);

              try {
                Integer.toString((Integer) imageConfig.get("minCPU"));
              } catch (ClassCastException e) {
                throw new VNFPackageFormatException("minCPU is not an integer");
              }

              try {
                Long.parseLong(imageConfig.get("minDisk").toString());
              } catch (NumberFormatException e) {
                throw new VNFPackageFormatException("minDisk is not a number");
              }
              try {
                Long.parseLong(imageConfig.get("minRam").toString());
              } catch (NumberFormatException e) {
                throw new VNFPackageFormatException("minRam is not a number");
              }

              String imageLink =
                  imageDetails.get("link") == null ? "" : (String) imageDetails.get("link");
              if (!imageIncluded && imageLink.isEmpty()) {
                throw new NotFoundException(
                    "VNFPackageManagement: For option upload=check you must define an image. Neither the image link is "
                        + "defined nor the image file is available. Please define at least one if you want to upload a new image");
              }
            }

            if (metadata.containsKey("additional-repos")) {
              List<Map<String, Object>> repoConfigurationInfoList;
              try {
                repoConfigurationInfoList =
                    (List<Map<String, Object>>) metadata.get("additional-repos");
              } catch (Exception e) {
                throw new VNFPackageFormatException(
                    "additional-repos value must be a list of maps");
              }

              for (Map<String, Object> rci : repoConfigurationInfoList) {
                if (!rci.containsKey("type"))
                  throw new VNFPackageFormatException(
                      "Each additional repo must contain the key \"type\"");
                if (!rci.containsKey("configuration"))
                  throw new VNFPackageFormatException(
                      "Each additional repo must contain the key \"configuration\"");
                try {
                  List<String> configurationInfo = (List<String>) rci.get("configuration");
                } catch (Exception e) {
                  throw new VNFPackageFormatException(
                      "The value of the key \"configuration\" must be a list of string");
                }
              }
            }

            if (!imageDetails.get("upload").equals("true"))
              if (!imageDetails.containsKey("ids") && !imageDetails.containsKey("names")) {
                throw new NotFoundException(
                    "VNFPackageManagement: Upload option 'false' or 'check' requires at least a list of ids or names to find "
                        + "the right image.");
              }
          } else if (!entry.getName().startsWith("scripts/") && entry.getName().endsWith(".json")) {
            //this must be the vnfd
            vnfdFound = true;
            String json = new String(content);
            log.trace("Content of vnfd is: " + json);

          } else if (entry.getName().endsWith(".img")) {
            throw new VNFPackageFormatException(
                "Uploading an image file from the VNFPackage is not supported, please use the image link");
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
    } catch (NotFoundException e) {
      throw new VNFPackageFormatException(e.getMessage(), e);
    }

    if (!vnfdFound)
      throw new VNFPackageFormatException("There is no VNF descriptor in the VNF Package");
    if (!metadataFound)
      throw new VNFPackageFormatException("There is no Metadata.yaml in the VNF Package");
  }

  private static boolean areNFVOVersionsCompatible(String nfvoVersion, String vnfPackageNFVOVersion)
      throws IncompatibleVNFPackage, NotFoundException {

    if (nfvoVersion == null) throw new NullPointerException("The nfvo version in null");
    if (vnfPackageNFVOVersion.isEmpty())
      throw new NullPointerException("The nfvo version in the Metadata is empty");

    //    // If the user does not specify the nfvo_version field, we suppose it is compatible (best effort)
    //    if (vnfPackageNFVOVersion == null || vnfPackageNFVOVersion.isEmpty()) return true;
    //
    //    int compatible = vnfPackageNFVOVersion.compareTo(nfvoVersion);
    //
    //    return compatible <= 0;
    String[] nfvoVersionSplitted = nfvoVersion.split(Pattern.quote("."));
    String[] vnfPackageNFVOVersionSplitted = vnfPackageNFVOVersion.split(Pattern.quote("."));

    return nfvoVersionSplitted[0].equals(vnfPackageNFVOVersionSplitted[0]);
  }

  public static void compareNFVOVersions(String vnfPackageNfvoVersion, String actualNfvoVersion)
      throws IncompatibleVNFPackage, NotFoundException {
    if (!areNFVOVersionsCompatible(actualNfvoVersion, vnfPackageNfvoVersion))
      throw new IncompatibleVNFPackage(
          "The NFVO Version: "
              + vnfPackageNfvoVersion
              + " specified in the Metadata"
              + " is not compatible with this NFVO version: "
              + actualNfvoVersion
              + ". The first digit of the version must be the same.");
  }
}
