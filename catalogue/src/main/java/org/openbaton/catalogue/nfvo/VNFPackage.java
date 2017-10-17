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

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 22/07/15. */
@Entity
public class VNFPackage extends BaseEntity {

  private String name;

  private String nfvo_version;

  private String vnfPackageVersion;

  public Set<String> getVimTypes() {
    return vimTypes;
  }

  public void setVimTypes(Set<String> vimTypes) {
    this.vimTypes = vimTypes;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> vimTypes;

  //URL to the image's location
  private String imageLink;

  //URL to the scripts' location
  private String scriptsLink;

  //NFVImage used by this VNFPackage
  @OneToOne(
    fetch = FetchType.EAGER,
    cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST},
    orphanRemoval = true
  )
  private NFVImage image;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private VNFPackageMetadata vnfPackageMetadata;

  //Set of scripts to execute
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<Script> scripts;

  public VNFPackage() {}

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }

  public String getScriptsLink() {
    return scriptsLink;
  }

  public void setScriptsLink(String scriptsLink) {
    this.scriptsLink = scriptsLink;
  }

  public Set<Script> getScripts() {
    return scripts;
  }

  public void setScripts(Set<Script> scripts) {
    this.scripts = scripts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NFVImage getImage() {
    return image;
  }

  public void setImage(NFVImage image) {
    this.image = image;
  }

  public String getNfvo_version() {
    return nfvo_version;
  }

  public void setNfvo_version(String nfvo_version) {
    this.nfvo_version = nfvo_version;
  }

  public String getVnfPackageVersion() {
    return vnfPackageVersion;
  }

  public void setVnfPackageVersion(String vnfPackageVersion) {
    this.vnfPackageVersion = vnfPackageVersion;
  }

  public VNFPackageMetadata getVnfPackageMetadata() {
    return vnfPackageMetadata;
  }

  public void setVnfPackageMetadata(VNFPackageMetadata vnfPackageMetadata) {
    this.vnfPackageMetadata = vnfPackageMetadata;
  }

  @Override
  public String toString() {
    return "VNFPackage{"
        + "name='"
        + name
        + '\''
        + ", nfvo_version='"
        + nfvo_version
        + '\''
        + ", vnfPackageVersion='"
        + vnfPackageVersion
        + '\''
        + ", vimTypes="
        + vimTypes
        + ", imageLink='"
        + imageLink
        + '\''
        + ", scriptsLink='"
        + scriptsLink
        + '\''
        + ", image="
        + image
        + ", vnfPackageMetadata="
        + vnfPackageMetadata
        + ", scripts="
        + scripts
        + '\''
        + "} "
        + super.toString();
  }
}
