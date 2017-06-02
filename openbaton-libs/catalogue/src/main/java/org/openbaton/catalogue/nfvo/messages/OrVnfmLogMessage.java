package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/** Used to send a request to a VNFM asking for log files. */
public class OrVnfmLogMessage extends OrVnfmMessage {

  private String vnfrName;
  private String hostname;

  public OrVnfmLogMessage(String vnfrName, String hostname) {
    this.action = Action.LOG_REQUEST;
    this.vnfrName = vnfrName;
    this.hostname = hostname;
  }

  public String getVnfrName() {
    return vnfrName;
  }

  public void setVnfrName(String vnfrName) {
    this.vnfrName = vnfrName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
