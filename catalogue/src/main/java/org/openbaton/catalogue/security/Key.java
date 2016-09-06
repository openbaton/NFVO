/*
 *
 *  * Copyright (c) 2016 Fraunhofer FOKUS
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.openbaton.catalogue.security;

import org.openbaton.catalogue.util.IdGenerator;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by mpa on 09.08.16.
 */
@Entity
@Table(
  name = "PublicKeys",
  uniqueConstraints = @UniqueConstraint(columnNames = {"name", "projectId"})
)
public class Key implements Serializable {

  @Id private String id;

  private String name;

  private String projectId;

  @Column(length = 500)
  private String publicKey;

  @Override
  public String toString() {
    return "Key{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", projectId='"
        + projectId
        + '\''
        + ", publicKey='"
        + publicKey
        + '\''
        + ", fingerprint='"
        + fingerprint
        + '\''
        + '}';
  }

  public String getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  private String fingerprint;

  public String getId() {
    return id;
  }

  public Key() {}

  public void setId(String id) {
    this.id = id;
  }

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

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }
}
