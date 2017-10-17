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

import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version", "vendor"}))
public class ImageMetadata extends BaseEntity {

  private String name;

  private String vendor;

  private String version;

  private String upload;

  private String filePath;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  private String username;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> ids;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> names;

  private String link;

  public String getImageRepoId() {
    return imageRepoId;
  }

  private String imageRepoId;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getUpload() {
    return upload;
  }

  public void setUpload(String upload) {
    this.upload = upload;
  }

  public void setIds(Set<String> ids) {
    this.ids = ids;
  }

  public void setNames(Set<String> names) {
    this.names = names;
  }

  public Set<String> getIds() {
    return ids;
  }

  public Set<String> getNames() {
    return names;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setImageRepoId(String id) {
    this.imageRepoId = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ImageMetadata{"
        + "name='"
        + name
        + '\''
        + ", vendor='"
        + vendor
        + '\''
        + ", version='"
        + version
        + '\''
        + ", upload='"
        + upload
        + '\''
        + ", filePath='"
        + filePath
        + '\''
        + ", username='"
        + username
        + '\''
        + ", ids="
        + ids
        + ", names="
        + names
        + ", link='"
        + link
        + '\''
        + ", imageRepoId='"
        + imageRepoId
        + '\''
        + "} "
        + super.toString();
  }
}
