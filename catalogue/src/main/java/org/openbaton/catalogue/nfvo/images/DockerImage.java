package org.openbaton.catalogue.nfvo.images;

import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

@Entity
public class DockerImage extends BaseNfvImage {

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> tags;

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  @Override
  public String toString() {
    return "DockerImage{" + "tags=" + tags + "} " + super.toString();
  }
}
