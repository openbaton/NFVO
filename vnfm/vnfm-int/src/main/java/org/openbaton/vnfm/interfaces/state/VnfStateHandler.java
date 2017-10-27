package org.openbaton.vnfm.interfaces.state;

import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** Created by lto on 29.05.17. */
public interface VnfStateHandler {
  Future<Void> handleVNF(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances,
      VirtualNetworkFunctionDescriptor vnfd)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  @Async
  Future<NFVMessage> executeAction(Future<NFVMessage> nfvMessage)
      throws ExecutionException, InterruptedException;

  @Async
  Future<NFVMessage> executeAction(NFVMessage nfvMessage)
      throws ExecutionException, InterruptedException;

  void terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

  @Async
  Future<Void> sendMessageToVNFR(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, NFVMessage nfvMessage)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;
}
