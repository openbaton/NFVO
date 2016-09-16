package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by lto on 16/09/16.
 */
public class OrVnfmUpdateMessage extends OrVnfmMessage {
  private Script script;
  private VirtualNetworkFunctionRecord vnfr;

  public OrVnfmUpdateMessage() {
    this.action = Action.UPDATE;
  }

  public Script getScript() {
    return script;
  }

  public void setScript(Script script) {
    this.script = script;
  }

  public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
    this.vnfr = vnfr;
  }

  public VirtualNetworkFunctionRecord getVnfr() {
    return vnfr;
  }
}
