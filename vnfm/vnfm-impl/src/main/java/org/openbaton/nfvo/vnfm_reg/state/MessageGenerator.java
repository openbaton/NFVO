package org.openbaton.nfvo.vnfm_reg.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrAllocateResourcesMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrErrorMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScaledMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScalingMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrStartStopMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.ManagerCredentialsRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.AllocateresourcesTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ErrorTask;
import org.openbaton.nfvo.vnfm_reg.tasks.HealTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ScaledTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ScalingTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StartTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StopTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 29.05.17. */
@Service
@Scope("prototype")
public class MessageGenerator implements org.openbaton.vnfm.interfaces.manager.MessageGenerator {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private ManagerCredentialsRepository managerCredentialsRepository;
  @Autowired private ConfigurableApplicationContext context;

  @Autowired
  @Qualifier("vnfmRegister")
  private VnfmRegister vnfmRegister;

  @Value("${nfvo.rabbit.brokerIp:127.0.0.1}")
  private String brokerIp;

  @Value("${nfvo.monitoring.ip:}")
  private String generalMonitoringIp;

  @Value("${nfvo.timezone:CET}")
  private String timezone;

  @Value("${spring.rabbitmq.port:5672}")
  private String brokerPort;

  @Override
  public VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException {
    String senderName = endpointType.toString().toLowerCase() + "VnfmSender";
    return (VnfmSender) this.context.getBean(senderName);
  }

  @Override
  public VnfmSender getVnfmSender(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException {
    VnfmManagerEndpoint endpoint = this.getEndpoint(vnfd);
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + vnfd.getType()
              + " (endpoint = "
              + vnfd.getEndpoint()
              + ") is not registered");
    }

    try {
      return getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }
  }

  @Override
  public Map<String, String> getExtension() {
    return getExtension(null);
  }

  @Override
  public Map<String, String> getExtension(String monitoringIp) {
    Map<String, String> extension = new HashMap<>();
    extension.put("brokerIp", brokerIp.trim());
    extension.put("brokerPort", brokerPort.trim());
    extension.put(
        "monitoringIp",
        monitoringIp != null && !monitoringIp.equals("") ? monitoringIp : generalMonitoringIp);
    extension.put("timezone", timezone);
    return extension;
  }

  @Override
  public NFVMessage getNextMessage(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    return null;
  }

  @Override
  public OrVnfmInstantiateMessage getNextMessage(
      VirtualNetworkFunctionDescriptor vnfd,
      Map<String, Set<String>> vduVimInstances,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      String monitoringIp)
      throws NotFoundException {
    Map<String, Collection<BaseVimInstance>> vimInstances = new HashMap<>();

    for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
      vimInstances.put(vdu.getId(), new LinkedHashSet<>());
      Set<String> vimInstanceNames = vduVimInstances.get(vdu.getId());
      for (String vimInstanceName : vimInstanceNames) {
        log.debug(
            "deployment procedure for (" + vnfd.getName() + "). Looking for " + vimInstanceName);
        BaseVimInstance vimInstance = null;

        String name;
        if (vimInstanceName.contains(":")) {
          name = vimInstanceName.split(":")[0];
          if (vdu.getMetadata() == null) {
            vdu.setMetadata(new HashMap<>());
          }
          String az = vimInstanceName.split(":")[1];
          vdu.getMetadata().put("az", az);
          //          vdu = vduRepository.save(vdu);
        } else {
          name = vimInstanceName;
        }
        vimInstance = vimInstanceRepository.findByProjectIdAndName(vdu.getProjectId(), name);

        vimInstances.get(vdu.getId()).add(vimInstance);
      }
    }

    //Creating the extension
    Map<String, String> extension = getExtension(monitoringIp);
    extension = fillAccessibilityConfigurationParameters(extension, vnfd, body);

    extension.put("nsr-id", networkServiceRecord.getId());

    HashSet<Key> keys;
    if (body != null && body.getKeys() != null) {
      keys = new HashSet<>(body.getKeys());
    } else {
      keys = new HashSet<>();
    }
    if (vnfd.getVnfPackageLocation() != null) {
      VNFPackage vnfPackage = vnfPackageRepository.findFirstById(vnfd.getVnfPackageLocation());
      return new OrVnfmInstantiateMessage(
          vnfd,
          getDeploymentFlavour(vnfd),
          vnfd.getName(),
          networkServiceRecord.getVlr(),
          extension,
          vimInstances,
          keys,
          vnfPackage);
    } else {
      return new OrVnfmInstantiateMessage(
          vnfd,
          getDeploymentFlavour(vnfd),
          vnfd.getName(),
          networkServiceRecord.getVlr(),
          extension,
          vimInstances,
          keys,
          null);
    }
  }

  private Map<String, String> fillAccessibilityConfigurationParameters(
      Map<String, String> extension, VirtualNetworkFunctionDescriptor vnfd, DeployNSRBody body) {
    if (body == null || body.getConfigurations().get(vnfd.getName()) == null) return extension;
    for (ConfigurationParameter passedConfigurationParameter :
        body.getConfigurations().get(vnfd.getName()).getConfigurationParameters()) {
      if (passedConfigurationParameter.getConfKey().equalsIgnoreCase("ssh_username")
          && passedConfigurationParameter.getValue() != null
          && !passedConfigurationParameter.getValue().isEmpty()) {
        extension.put(
            passedConfigurationParameter.getConfKey(), passedConfigurationParameter.getValue());
      }
      if (passedConfigurationParameter.getConfKey().equals("ssh_password")
          && passedConfigurationParameter.getValue() != null
          && !passedConfigurationParameter.getValue().isEmpty()) {
        extension.put(
            passedConfigurationParameter.getConfKey(), passedConfigurationParameter.getValue());
      }
    }
    return extension;
  }

  //As a default operation of the NFVO, it get always the first DeploymentFlavour!
  private VNFDeploymentFlavour getDeploymentFlavour(VirtualNetworkFunctionDescriptor vnfd)
      throws NotFoundException {
    VNFDeploymentFlavour flavor = null;
    if (!vnfd.getDeployment_flavour().iterator().hasNext()) {
      for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
        if (vdu.getComputation_requirement() == null
            || vdu.getComputation_requirement().isEmpty()) {
          throw new NotFoundException(
              "There is no DeploymentFlavour in vnfd or in all VDUs: " + vnfd.getName());
        } else {
          flavor = new VNFDeploymentFlavour();
          flavor.setFlavour_key(vdu.getComputation_requirement());
        }
      }
    } else {
      flavor = vnfd.getDeployment_flavour().iterator().next();
    }
    return flavor;
  }

  @Override
  public VnfmManagerEndpoint getEndpoint(VirtualNetworkFunctionDescriptor vnfd)
      throws NotFoundException {
    return this.vnfmRegister.getVnfm(vnfd.getEndpoint());
  }

  @Override
  public VnfmManagerEndpoint getVnfm(String endpoint) throws NotFoundException {
    return vnfmRegister.getVnfm(endpoint);
  }

  public VirtualNetworkFunctionRecord setupTask(
      NFVMessage nfvMessage, org.openbaton.vnfm.interfaces.tasks.AbstractTask task) {
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    if (nfvMessage.getAction().ordinal() == Action.ERROR.ordinal()) {
      VnfmOrErrorMessage vnfmOrErrorMessage = (VnfmOrErrorMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrErrorMessage.getVirtualNetworkFunctionRecord();
      Exception e = vnfmOrErrorMessage.getException();
      ((ErrorTask) task).setException(e);
      ((ErrorTask) task).setNsrId(vnfmOrErrorMessage.getNsrId());
    } else if (nfvMessage.getAction().ordinal() == Action.INSTANTIATE.ordinal()) {
      VnfmOrInstantiateMessage vnfmOrInstantiate = (VnfmOrInstantiateMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrInstantiate.getVirtualNetworkFunctionRecord();
    } else if (nfvMessage.getAction().ordinal() == Action.SCALED.ordinal()) {
      VnfmOrScaledMessage vnfmOrScaled = (VnfmOrScaledMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrScaled.getVirtualNetworkFunctionRecord();
      ((ScaledTask) task).setVnfcInstance(vnfmOrScaled.getVnfcInstance());
    } else if (nfvMessage.getAction().ordinal() == Action.HEAL.ordinal()) {
      VnfmOrHealedMessage vnfmOrHealedMessage = (VnfmOrHealedMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrHealedMessage.getVirtualNetworkFunctionRecord();
      ((HealTask) task).setVnfcInstance(vnfmOrHealedMessage.getVnfcInstance());
      ((HealTask) task).setCause(vnfmOrHealedMessage.getCause());
    } else if (nfvMessage.getAction().ordinal() == Action.SCALING.ordinal()) {
      VnfmOrScalingMessage vnfmOrScalingMessage = (VnfmOrScalingMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrScalingMessage.getVirtualNetworkFunctionRecord();
      ((ScalingTask) task).setUserdata(vnfmOrScalingMessage.getUserData());
      ((ScalingTask) task).setVimInstance(vnfmOrScalingMessage.getVimInstance());
    } else if (nfvMessage.getAction().ordinal() == Action.ALLOCATE_RESOURCES.ordinal()) {
      VnfmOrAllocateResourcesMessage vnfmOrAllocateResourcesMessage =
          (VnfmOrAllocateResourcesMessage) nfvMessage;
      virtualNetworkFunctionRecord =
          vnfmOrAllocateResourcesMessage.getVirtualNetworkFunctionRecord();
      Map<String, BaseVimInstance> vimChosen = vnfmOrAllocateResourcesMessage.getVimInstances();
      ((AllocateresourcesTask) task).setVims(vimChosen);
      ((AllocateresourcesTask) task)
          .setKeys(new HashSet<>(vnfmOrAllocateResourcesMessage.getKeyPairs()));
      ((AllocateresourcesTask) task).setUserData(vnfmOrAllocateResourcesMessage.getUserdata());
    } else if (nfvMessage.getAction().ordinal() == Action.START.ordinal()) {
      VnfmOrStartStopMessage vnfmOrStartStopMessage = (VnfmOrStartStopMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrStartStopMessage.getVirtualNetworkFunctionRecord();
      VNFCInstance vnfcInstance = vnfmOrStartStopMessage.getVnfcInstance();
      if (vnfcInstance != null) {
        ((StartTask) task).setVnfcInstance(vnfcInstance);
      }
    } else if (nfvMessage.getAction().ordinal() == Action.STOP.ordinal()) {
      VnfmOrStartStopMessage vnfmOrStartStopMessage = (VnfmOrStartStopMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrStartStopMessage.getVirtualNetworkFunctionRecord();
      VNFCInstance vnfcInstance = vnfmOrStartStopMessage.getVnfcInstance();
      if (vnfcInstance != null) {
        ((StopTask) task).setVnfcInstance(vnfcInstance);
      }
    } else {
      VnfmOrGenericMessage vnfmOrGeneric = (VnfmOrGenericMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrGeneric.getVirtualNetworkFunctionRecord();
    }
    return virtualNetworkFunctionRecord;
  }
}
