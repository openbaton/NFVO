package org.openbaton.vnfm.interfaces.tasks;

import java.util.concurrent.Callable;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.springframework.context.ApplicationEventPublisherAware;

/** Created by lto on 29.05.17. */
public interface AbstractTask extends Callable<NFVMessage>, ApplicationEventPublisherAware {
  Action getAction();

  void setAction(Action action);

  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord();

  void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);
}
