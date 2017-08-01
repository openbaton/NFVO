package org.openbaton.catalogue.nfvo;

import java.util.List;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mob on 03.05.17. */
@Entity
public class AdditionalRepoInfo {
  private PackageType packageType;
  private String keyUrl;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> configuration;

  @Id private String id;
  @Version private int hb_version = 0;

  public AdditionalRepoInfo() {}

  public AdditionalRepoInfo(PackageType packageType) {
    this.packageType = packageType;
  }

  public PackageType getPackageType() {
    return packageType;
  }

  public void setPackageType(PackageType packageType) {
    this.packageType = packageType;
  }

  public String getKeyUrl() {
    return keyUrl;
  }

  public void setKeyUrl(String keyUrl) {
    if (keyUrl == null) throw new NullPointerException("KeyUrl is null");
    this.keyUrl = keyUrl;
  }

  @PrePersist
  public void ensureId() {
    setId(IdGenerator.createUUID());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getHb_version() {
    return hb_version;
  }

  public void setHb_version(int hb_version) {
    this.hb_version = hb_version;
  }

  public List<String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(List<String> configuration) {
    if (configuration == null) throw new NullPointerException("The configuration list is null");
    this.configuration = configuration;
  }

  @Override
  public String toString() {
    return "AdditionalRepoInfo{"
        + "packageType="
        + packageType
        + ", keyUrl='"
        + keyUrl
        + '\''
        + ", configuration="
        + configuration
        + ", id='"
        + id
        + '\''
        + ", hb_version="
        + hb_version
        + '}';
  }
}
