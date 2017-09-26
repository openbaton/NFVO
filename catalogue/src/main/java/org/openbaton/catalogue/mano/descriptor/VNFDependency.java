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

package org.openbaton.catalogue.mano.descriptor;

/**
 * Created by lto on 05/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openbaton.catalogue.util.IdGenerator;

/**
 * Describe dependencies between VNF. Defined in terms of source and target VNF i.e. target VNF
 * "depends on" source VNF. In other words a source VNF shall exist and connect to the service
 * before target VNF can be initiated/deployed and connected. This element would be used, for
 * example, to define the sequence in which various numbered network nodes and links within a VNF FG
 * should be instantiated by the NFV Orchestrator.
 */
@Entity
public class VNFDependency implements Serializable {

  @Id private String id;

  @Version private Integer version = 0;

  @NotNull
  @Size(min = 1)
  private String source;

  public String getSource_id() {
    return source_id;
  }

  public void setSource_id(String source_id) {
    this.source_id = source_id;
  }

  public String getTarget_id() {
    return target_id;
  }

  public void setTarget_id(String target_id) {
    this.target_id = target_id;
  }

  private String source_id;

  @NotNull
  @Size(min = 1)
  private String target;

  private String target_id;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> parameters;

  public VNFDependency() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Set<String> getParameters() {
    return parameters;
  }

  public void setParameters(Set<String> parameters) {
    this.parameters = parameters;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "VNFDependency{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", source='"
        + source
        + '\''
        + ", source_id='"
        + source_id
        + '\''
        + ", target='"
        + target
        + '\''
        + ", target_id='"
        + target_id
        + '\''
        + ", parameters="
        + parameters
        + '}';
  }
}
