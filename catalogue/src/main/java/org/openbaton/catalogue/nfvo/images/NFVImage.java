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

/** Created by lto on 11/05/15. */
@Entity
public class NFVImage extends BaseNfvImage {

  private String name;
  private Long minRam = 0L;; // in MB
  private Long minDiskSpace = 0L; // in GB
  private String minCPU;
  private Boolean isPublic = false;
  private String diskFormat;
  private String containerFormat;

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
        + "} "
        + super.toString();
  }
}
