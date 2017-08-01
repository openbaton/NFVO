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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mpa on 22/05/16. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version", "vendor"}))
public class ImageMetadata implements Serializable {

  @Id private String id;

  private String name;

  private String vendor;

  private String version;

  @JsonIgnore @Version private int hb_version = 0;

  private String upload;

  private String filePath;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @JsonIgnore private String username;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> ids;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> names;

  private String link;

  public String getImageRepoId() {
    return imageRepoId;
  }

  @JsonIgnore private String imageRepoId;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public int getHb_version() {
    return hb_version;
  }

  public void setHb_version(int hb_version) {
    this.hb_version = hb_version;
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
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", vendor='"
        + vendor
        + '\''
        + ", version='"
        + version
        + '\''
        + ", hb_version="
        + hb_version
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
        + '}';
  }
}
