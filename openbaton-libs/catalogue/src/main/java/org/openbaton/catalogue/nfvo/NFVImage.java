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

package org.openbaton.catalogue.nfvo;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;

/** Created by lto on 11/05/15. */
@Entity
public class NFVImage implements Serializable {
  @Id private String id;
  @Version private int version = 0;

  private String extId;
  private String name;
  private long minRam; //in MB
  private long minDiskSpace; //in GB
  private String minCPU;

  private boolean isPublic;
  private String diskFormat;
  private String containerFormat;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private ImageStatus status;

  public NFVImage() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
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

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setIsPublic(boolean isPublic) {
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
    return "Image{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", version="
        + version
        + ", extId='"
        + extId
        + '\''
        + ", minRam='"
        + minRam
        + '\''
        + ", minDiskSpace='"
        + minDiskSpace
        + '\''
        + ", minCPU='"
        + minCPU
        + '\''
        + ", public='"
        + isPublic
        + '\''
        + ", diskFormat='"
        + diskFormat
        + '\''
        + ", containerFormat='"
        + containerFormat
        + '\''
        + ", status='"
        + status
        + '\''
        + ", created='"
        + created
        + '\''
        + ", updated='"
        + updated
        + '\''
        + '}';
  }
}
