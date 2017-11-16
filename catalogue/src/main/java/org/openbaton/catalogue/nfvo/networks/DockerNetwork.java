package org.openbaton.catalogue.nfvo.networks;

import javax.persistence.Entity;

@Entity
public class DockerNetwork extends BaseNetwork {
  private String scope;
  private String driver;
  private String gateway;
  private String subnet;

  @Override
  public String toString() {
    return "DockerNetwork{"
        + "scope='"
        + scope
        + '\''
        + ", driver='"
        + driver
        + '\''
        + ", gateway='"
        + gateway
        + '\''
        + ", subnet='"
        + subnet
        + '\''
        + "} "
        + super.toString();
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public String getSubnet() {
    return subnet;
  }

  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }
}
