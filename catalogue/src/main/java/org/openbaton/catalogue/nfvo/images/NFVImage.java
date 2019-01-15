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

package org.openbaton.catalogue.nfvo.images;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.openbaton.catalogue.nfvo.ImageStatus;

/** This class represents NFV images. This type of image is currently only used with OpenStack. */
@Entity
public class NFVImage extends BaseNfvImage {

  private String name;
  private Long minRam = 0L;; // in MB
  private Long minDiskSpace = 0L; // in GB
  private String minCPU;
  private Boolean isPublic = false;
  private String diskFormat;
  private String containerFormat;

  // null if the image file is stored locally on the file system. Otherwise it points to the
  // location from where the image can be downloaded
  private String url;
  // md5 hash of the image file (currently only set if storedLocally == true)
  private String hash;
  // true if the NFVImage belongs to the image repository. It is false if the NFVImage is associated
  // with a VIM
  private boolean isInImageRepo;
  // true if the image file is stored on the local file system. Only set if isInImageRepo == true
  private boolean storedLocally;

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private ImageStatus status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getMinRam() {
    return minRam;
  }

  public void setMinRam(long minRam) {
    this.minRam = minRam;
  }

  public long getMinDiskSpace() {
    return minDiskSpace;
  }

  public void setMinDiskSpace(long minDiskSpace) {
    this.minDiskSpace = minDiskSpace;
  }

  public String getMinCPU() {
    return minCPU;
  }

  public void setMinCPU(String minCPU) {
    this.minCPU = minCPU;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public Boolean isPublic() {
    return isPublic;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public void setPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getDiskFormat() {
    return diskFormat;
  }

  public void setDiskFormat(String diskFormat) {
    this.diskFormat = diskFormat;
  }

  public String getContainerFormat() {
    return containerFormat;
  }

  public void setContainerFormat(String containerFormat) {
    this.containerFormat = containerFormat;
  }

  public ImageStatus getStatus() {
    return status;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isInImageRepo() {
    return isInImageRepo;
  }

  public void setInImageRepo(boolean inImageRepo) {
    isInImageRepo = inImageRepo;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public void setStatus(String status) {
    if (status == null) this.status = ImageStatus.UNRECOGNIZED;
    else {
      try {
        this.status = ImageStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        this.status = ImageStatus.UNRECOGNIZED;
      }
    }
  }

  public boolean isStoredLocally() {
    return storedLocally;
  }

  public void setStoredLocally(boolean storedLocally) {
    this.storedLocally = storedLocally;
  }

  @Override
  public String toString() {
    return "NFVImage{"
        + "extId='"
        + extId
        + '\''
        + ", name='"
        + name
        + '\''
        + ", minRam="
        + minRam
        + ", minDiskSpace="
        + minDiskSpace
        + ", minCPU='"
        + minCPU
        + '\''
        + ", isPublic="
        + isPublic
        + ", diskFormat='"
        + diskFormat
        + '\''
        + ", containerFormat='"
        + containerFormat
        + '\''
        + ", created="
        + created
        + ", updated="
        + updated
        + ", status="
        + status
        + ", url="
        + url
        + ", hash="
        + hash
        + ", isInImageRepo="
        + isInImageRepo
        + ", storedLocally="
        + storedLocally
        + "} "
        + super.toString();
  }
}
