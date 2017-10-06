package org.openbaton.catalogue.mano.descriptor;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mah on 4/28/16. */
@Entity
public class ACLMatchingCriteria implements Serializable {
  @Id private String id;
  @Version private int version = 0;

  private String source_ip;

  private String destination_ip;

  private int source_port;

  private int destination_port;

  private int protocol;

  public ACLMatchingCriteria() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceIP() {
    return source_ip;
  }

  public void setSourceIP(String src_ip) {
    this.source_ip = src_ip;
  }

  public String getDestinationIP() {
    return destination_ip;
  }

  public void setDestinationIP(String dest_ip) {
    this.destination_ip = dest_ip;
  }

  public int getSourcePort() {
    return source_port;
  }

  public void setSourcePort(int src_port) {
    this.source_port = src_port;
  }

  public int getDestinationPort() {
    return destination_port;
  }

  public void setDestinationPort(int dest_port) {
    this.destination_port = dest_port;
  }

  public int getProtocol() {
    return protocol;
  }

  public void setProtocol(int protocol) {
    this.protocol = protocol;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }
}
