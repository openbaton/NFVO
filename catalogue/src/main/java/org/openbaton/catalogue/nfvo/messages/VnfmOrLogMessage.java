package org.openbaton.catalogue.nfvo.messages;

import java.util.List;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/** Used to send back an answer to an NFVO's request asking for log files. */
public class VnfmOrLogMessage extends VnfmOrMessage {

  private List<String> outputLog;
  private List<String> errorLog;

  public VnfmOrLogMessage() {
    this.action = Action.LOG_REQUEST;
  }

  public VnfmOrLogMessage(List<String> outputLog, List<String> errorLog) {
    this.outputLog = outputLog;
    this.errorLog = errorLog;
    this.action = Action.LOG_REQUEST;
  }

  public List<String> getOutputLog() {
    return outputLog;
  }

  public void setOutputLog(List<String> outputLog) {
    this.outputLog = outputLog;
  }

  public List<String> getErrorLog() {
    return errorLog;
  }

  public void setErrorLog(List<String> errorLog) {
    this.errorLog = errorLog;
  }
}
