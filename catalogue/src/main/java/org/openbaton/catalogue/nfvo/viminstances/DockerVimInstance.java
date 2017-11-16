package org.openbaton.catalogue.nfvo.viminstances;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.DockerImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.DockerNetwork;

@Entity
public class DockerVimInstance extends BaseVimInstance {

  private String ca;
  private String dockerKey;
  private String cert;

  public String getCa() {
    return ca;
  }

  public void setCa(String ca) {
    this.ca = ca;
  }

  public String getDockerKey() {
    return dockerKey;
  }

  public void setDockerKey(String dockerKey) {
    this.dockerKey = dockerKey;
  }

  public String getCert() {
    return cert;
  }

  public void setCert(String cert) {
    this.cert = cert;
  }

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
  private Set<DockerImage> images;

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
  private Set<DockerNetwork> networks;

  @Override
  public Set<? extends BaseNfvImage> getImages() {
    return images;
  }

  public void setImages(Set<DockerImage> images) {
    this.images = images;
  }

  public Set<? extends BaseNetwork> getNetworks() {
    return networks;
  }

  @Override
  public void addAllNetworks(Collection<BaseNetwork> networks) {
    if (this.networks == null) this.networks = new HashSet<>();
    networks.forEach(n -> this.networks.add((DockerNetwork) n));
  }

  @Override
  public void addAllImages(Collection<BaseNfvImage> images) {
    if (this.images == null) this.images = new HashSet<>();
    images.forEach(n -> this.images.add((DockerImage) n));
  }

  @Override
  public void removeAllNetworks(Collection<BaseNetwork> networks) {
    this.networks.removeAll(networks);
  }

  @Override
  public void removeAllImages(Collection<BaseNfvImage> images) {
    this.images.removeAll(images);
  }

  @Override
  public void addImage(BaseNfvImage image) {
    //TODO check cast
    this.images.add((DockerImage) image);
  }

  @Override
  public void addNetwork(BaseNetwork network) {
    //TODO check cast
    this.networks.add((DockerNetwork) network);
  }

  public void setNetworks(Set<DockerNetwork> networks) {
    this.networks = networks;
  }
}
