package org.openbaton.catalogue.nfvo;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by tbr on 07.07.16.
 */
@Entity(name = "requiresParameters")
public class RequiresParameters {

  @Id private String id;
  @Version private int version;

  @Column
  @ElementCollection(targetClass = String.class)
  private Set<String> parameters;

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

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<String> getParameters() {
    return parameters;
  }

  public void setParameters(Set<String> params) {
    this.parameters = params;
  }

  @Override
  public String toString() {
    return "requiresParameters{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", parameters=["
        + parametersToString()
        + ']'
        + '}';
  }

  private String parametersToString() {
    String returnString = "";
    for (Iterator<String> iterator = parameters.iterator(); iterator.hasNext(); ) {
      returnString += "\'" + iterator.next() + "\'";
      if (iterator.hasNext()) returnString += ", ";
    }
    return returnString;
  }
}
