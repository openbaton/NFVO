/*
 * #
 * # Copyright (c) 2015 Fraunhofer FOKUS
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #     http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * #
 *
 */

package org.openbaton.catalogue.nfvo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.util.BaseEntity;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mpa on 22/05/16. */
@Entity
public class VNFPackageMetadata extends BaseEntity {

  //Name of the Package
  private String name;

  @Column(length = 1000)
  private String description;

  private String vendor;

  private String version;

  private String vnfmType;

  private String vnfPackageFatherId;

  private boolean defaultFlag;

  private String tag;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> requirements;

  //URL to the scripts' location
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private VirtualNetworkFunctionDescriptor vnfd;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private NFVImage nfvImage;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private ImageMetadata imageMetadata;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> vimTypes;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AdditionalRepoInfo> additionalRepoInfoList;

  private String vnfPackageFileName;

  @Lob private byte[] vnfPackageFile;

  @Lob private byte[] csarFile;

  private String type;

  private String md5sum;

  private String iconRepoId;

  private String nfvoVersion;

  private int downloadCounter = 0;

  private String osId;
  private String osVersion;
  private String osArchitecture;

  private static final String[] OS_ID_LIST = new String[] {"Ubuntu", "CentOS"};
  private static final String[] UBUNTU_VERSION_LIST = new String[] {"Trusty", "Xenial"};
  private static final String[] CENTOS_VERSION_LIST = new String[] {"6.5", "7.0"};
  private static final String[] OS_ARCH_LIST = new String[] {"x86", "x64", "x86_64"};

  @PrePersist
  public void ensureId() {
    setId(IdGenerator.createUUID());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isEmpty())
      throw new NullPointerException("VNFPackageMetadata name is null or empty");
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getRequirements() {
    return requirements;
  }

  public void setRequirements(Map<String, String> requirements) {
    this.requirements = requirements;
  }

  public String getMd5sum() {
    return md5sum;
  }

  public void setMd5sum(String md5sum) {
    this.md5sum = md5sum;
  }

  public String getVnfPackageFileName() {
    return vnfPackageFileName;
  }

  public void setVnfPackageFileName(String vnfPackageFileName) {
    this.vnfPackageFileName = vnfPackageFileName;
  }

  public VirtualNetworkFunctionDescriptor getVnfd() {
    return vnfd;
  }

  public void setVnfd(VirtualNetworkFunctionDescriptor vnfd) {
    this.vnfd = vnfd;
  }

  public NFVImage getNfvImage() {
    return nfvImage;
  }

  public void setNfvImage(NFVImage nfvImage) {
    this.nfvImage = nfvImage;
  }

  public ImageMetadata getImageMetadata() {
    return imageMetadata;
  }

  public void setImageMetadata(ImageMetadata imageMetadata) {
    this.imageMetadata = imageMetadata;
  }

  public byte[] getVnfPackageFile() {
    return vnfPackageFile;
  }

  public void setVnfPackageFile(byte[] vnfPackageFile) {
    this.vnfPackageFile = vnfPackageFile;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    if (vendor == null || vendor.isEmpty())
      throw new NullPointerException("'vendor' is null or empty");
    this.vendor = vendor;
  }

  public void setNfvo_version(String nfvo_version) {
    this.nfvoVersion = nfvo_version;
  }

  public String getIconRepoId() {
    return iconRepoId;
  }

  public void setIconRepoId(String iconRepoId) {
    this.iconRepoId = iconRepoId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public byte[] getCsarFile() {
    return csarFile;
  }

  public void setCsarFile(byte[] csarFile) {
    this.csarFile = csarFile;
  }

  public Set<AdditionalRepoInfo> getAdditionalRepoInfoList() {
    return additionalRepoInfoList;
  }

  public void setAdditionalRepoInfoList(Set<AdditionalRepoInfo> additionalRepoInfoList) {
    this.additionalRepoInfoList = additionalRepoInfoList;
  }

  public Set<String> getVimTypes() {
    return vimTypes;
  }

  public void setVimTypes(Set<String> vimTypes) {
    this.vimTypes = vimTypes;
  }

  public String getNfvoVersion() {
    return nfvoVersion;
  }

  public void setNfvoVersion(String nfvoVersion) {
    this.nfvoVersion = nfvoVersion;
  }

  public int getDownloadCounter() {
    return downloadCounter;
  }

  public void setDownloadCounter(int downloadCounter) {
    this.downloadCounter = downloadCounter;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    if (version == null || version.isEmpty())
      throw new NullPointerException("'version' is null or empty");
    this.version = version;
  }

  public String getVnfmType() {
    return vnfmType;
  }

  public void setVnfmType(String vnfmType) {
    this.vnfmType = vnfmType;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getOsId() {
    return osId;
  }

  public String getVnfPackageFatherId() {
    return vnfPackageFatherId;
  }

  public void setVnfPackageFatherId(String vnfPackageFatherId) {
    this.vnfPackageFatherId = vnfPackageFatherId;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String getOsArchitecture() {
    return osArchitecture;
  }

  public boolean isOsIdAndVersionCompatible() {
    boolean result = false;
    if (getOsId() != null && getOsVersion() != null) {
      for (String supportedOsId : OS_ID_LIST) {
        if (supportedOsId.equals("Ubuntu")) {
          for (String supportedOsVersion : UBUNTU_VERSION_LIST)
            if (supportedOsVersion.equals(osVersion)) result = true;
        } else if (supportedOsId.equals("CentOs")) {
          for (String supportedOsVersion : CENTOS_VERSION_LIST)
            if (supportedOsVersion.equals(osVersion)) result = true;
        }
      }
    } else result = true;
    return result;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public void setOsId(String osId) {
    if (osId != null) {
      for (String supportedOsId : OS_ID_LIST)
        if (osId.equalsIgnoreCase(supportedOsId)) {
          this.osId = supportedOsId;
          return;
        }
      throw new IllegalArgumentException(
          "'osId' is not correct. Choose one of the following: " + Arrays.toString(OS_ID_LIST));
    }
  }

  public void setOsArchitecture(String osArchitecture) {
    if (osArchitecture != null) {
      for (String supportedArchitecture : OS_ARCH_LIST) {
        if (osArchitecture.equalsIgnoreCase(supportedArchitecture)) {
          this.osArchitecture = supportedArchitecture;
          return;
        }
      }
      throw new IllegalArgumentException(
          "'osArchitecture' is not correct. Choose one of the following: "
              + Arrays.toString(OS_ARCH_LIST));
    }
  }

  public boolean isDefaultFlag() {
    return defaultFlag;
  }

  public void addRepoConfigurationInfo(AdditionalRepoInfo additionalRepoInfo) {
    if (additionalRepoInfo == null) throw new NullPointerException("AdditionalRepoInfo is null");
    if (getAdditionalRepoInfoList() == null) {
      Set<AdditionalRepoInfo> additionalRepoInfoList = new HashSet<>();
      additionalRepoInfoList.add(additionalRepoInfo);
      setAdditionalRepoInfoList(additionalRepoInfoList);
    } else getAdditionalRepoInfoList().add(additionalRepoInfo);
  }

  public void setDefaultFlag(boolean defaultFlag) {
    this.defaultFlag = defaultFlag;
  }

  @Override
  public String toString() {
    return "VNFPackageMetadata{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", vendor='"
        + vendor
        + '\''
        + ", version='"
        + version
        + '\''
        + ", vnfmType='"
        + vnfmType
        + '\''
        + ", vnfPackageFatherId='"
        + vnfPackageFatherId
        + '\''
        + ", defaultFlag="
        + defaultFlag
        + ", tag='"
        + tag
        + '\''
        + ", requirements="
        + requirements
        + ", vnfd="
        + vnfd
        + ", nfvImage="
        + nfvImage
        + ", imageMetadata="
        + imageMetadata
        + ", vimTypes="
        + vimTypes
        + ", additionalRepoInfoList="
        + additionalRepoInfoList
        + ", vnfPackageFileName='"
        + vnfPackageFileName
        + '\''
        + ", type='"
        + type
        + '\''
        + ", md5sum='"
        + md5sum
        + '\''
        + ", iconRepoId='"
        + iconRepoId
        + '\''
        + ", nfvoVersion='"
        + nfvoVersion
        + '\''
        + ", downloadCounter="
        + downloadCounter
        + ", osId='"
        + osId
        + '\''
        + ", osVersion='"
        + osVersion
        + '\''
        + ", osArchitecture='"
        + osArchitecture
        + '\''
        + "} "
        + super.toString();
  }
}
