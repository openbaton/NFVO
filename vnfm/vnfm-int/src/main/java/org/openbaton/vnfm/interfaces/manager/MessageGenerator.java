package org.openbaton.vnfm.interfaces.manager;

import java.util.List;
import java.util.Map;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.openbaton.vnfm.interfaces.tasks.AbstractTask;
import org.springframework.beans.BeansException;

/** Created by lto on 29.05.17. */
public interface MessageGenerator {
  VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException;

  VnfmSender getVnfmSender(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException;

  Map<String, String> getExtension();

  NFVMessage getNextMessage(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

  OrVnfmInstantiateMessage getNextMessage(
      VirtualNetworkFunctionDescriptor vnfd,
      Map<String, List<String>> vduVimInstances,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body)
      throws NotFoundException;

  VnfmManagerEndpoint getEndpoint(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException;

  VnfmManagerEndpoint getVnfm(String endpoint) throws NotFoundException;

  VirtualNetworkFunctionRecord setupTask(NFVMessage nfvMessage, AbstractTask task);
}
