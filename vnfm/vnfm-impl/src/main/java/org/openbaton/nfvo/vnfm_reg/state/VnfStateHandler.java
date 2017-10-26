package org.openbaton.nfvo.vnfm_reg.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.ReleaseresourcesTask;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/** Created by lto on 29.05.17. */
@Service
@EnableAsync
public class VnfStateHandler implements org.openbaton.vnfm.interfaces.state.VnfStateHandler {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private org.openbaton.vnfm.interfaces.manager.MessageGenerator generator;
  @Autowired private ConfigurableApplicationContext context;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;

  @Autowired private ThreadPoolTaskExecutor asyncExecutor;

  @Override
  public void handleVNF(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances,
      VirtualNetworkFunctionDescriptor vnfd)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    log.debug(
        "Processing VNFD ("
            + vnfd.getName()
            + ") for NSD ("
            + networkServiceDescriptor.getName()
            + ")");

    VnfmSender vnfmSender = generator.getVnfmSender(vnfd);
    NFVMessage message =
        generator.getNextMessage(vnfd, vduVimInstances, networkServiceRecord, body);
    VnfmManagerEndpoint endpoint = generator.getEndpoint(vnfd);
    log.debug("----------Executing ACTION: " + message.getAction());
    executeAction(vnfmSender.sendCommand(message, endpoint));
    log.info("Sent " + message.getAction() + " to VNF: " + vnfd.getName());
  }

  private void newExecuteAction(NFVMessage nfvMessage) {}

  private boolean isaReturningTask(Action action) {
    switch (action) {
      case ALLOCATE_RESOURCES:
      case GRANT_OPERATION:
      case SCALING:
      case UPDATEVNFR:
        return true;
      default:
        return false;
    }
  }

  @Override
  @Async
  public Future<NFVMessage> executeAction(Future<NFVMessage> nfvMessageFuture)
      throws ExecutionException, InterruptedException {
    NFVMessage nfvMessage = nfvMessageFuture.get();
    return executeActionNotAsync(nfvMessage);
  }

  @Override
  @Async
  public Future<NFVMessage> executeAction(NFVMessage nfvMessage)
      throws ExecutionException, InterruptedException {
    return executeActionNotAsync(nfvMessage);
  }

  private Future<NFVMessage> executeActionNotAsync(NFVMessage nfvMessage)
      throws ExecutionException, InterruptedException {
    //    log.debug("-----------Finished ACTION: " + nfvMessage.getAction());
    String actionName = nfvMessage.getAction().toString().replace("_", "").toLowerCase();
    String beanName = actionName + "Task";
    log.debug("Looking for bean called: " + beanName);
    AbstractTask task = (AbstractTask) this.context.getBean(beanName);
    log.trace("message: " + nfvMessage);
    task.setAction(nfvMessage.getAction());

    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    virtualNetworkFunctionRecord = generator.setupTask(nfvMessage, task);

    if (virtualNetworkFunctionRecord != null) {
      if (virtualNetworkFunctionRecord.getParent_ns_id() != null) {
        if (!nsrRepository.exists(virtualNetworkFunctionRecord.getParent_ns_id())) {
          return null;
        } else {
          virtualNetworkFunctionRecord.setProjectId(
              nsrRepository
                  .findFirstById(virtualNetworkFunctionRecord.getParent_ns_id())
                  .getProjectId());
          for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            vdu.setProjectId(
                nsrRepository
                    .findFirstById(virtualNetworkFunctionRecord.getParent_ns_id())
                    .getProjectId());
          }
        }
      }

      virtualNetworkFunctionRecord.setTask(actionName);
      task.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

      log.info(
          "Executing Task "
              + beanName
              + " for vnfr "
              + virtualNetworkFunctionRecord.getName()
              + ". Cyclic="
              + virtualNetworkFunctionRecord.hasCyclicDependency());
    }
    log.trace("AsyncExecutor pool size: " + asyncExecutor.getPoolSize());
    log.trace("AsyncExecutor active count: " + asyncExecutor.getActiveCount());

    if (isaReturningTask(nfvMessage.getAction())) {
      return asyncExecutor.submit(task);
    } else {
      asyncExecutor.submit(task);
      return null;
    }
  }

  @Override
  public void terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    ReleaseresourcesTask task = (ReleaseresourcesTask) context.getBean("releaseresourcesTask");
    task.setAction(Action.RELEASE_RESOURCES);
    task.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

    this.asyncExecutor.submit(task);
  }

  @Override
  @Async
  public Future<Void> sendMessageToVNFR(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, NFVMessage nfvMessage)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint =
        generator.getVnfm(virtualNetworkFunctionRecordDest.getEndpoint());
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + virtualNetworkFunctionRecordDest.getType()
              + " (endpoint = "
              + virtualNetworkFunctionRecordDest.getEndpoint()
              + ") is not registered");
    }
    VnfmSender vnfmSender;
    try {
      vnfmSender = generator.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    log.debug(
        "Sending message "
            + nfvMessage.getAction()
            + " to "
            + virtualNetworkFunctionRecordDest.getName());
    executeAction(vnfmSender.sendCommand(nfvMessage, endpoint));
    return new AsyncResult<>(null);
  }
}
