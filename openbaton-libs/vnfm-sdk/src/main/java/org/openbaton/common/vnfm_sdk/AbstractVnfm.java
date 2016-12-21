/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.common.vnfm_sdk;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.security.Key;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleChangeNotification;
import org.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.openbaton.common.vnfm_sdk.utils.VNFRUtils;
import org.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 08/07/15. */
public abstract class AbstractVnfm
    implements VNFLifecycleManagement, VNFLifecycleChangeNotification {

  protected static String type;
  protected static String endpoint;
  protected static String endpointType;
  protected static Properties properties;
  protected static Logger log = LoggerFactory.getLogger(AbstractVnfm.class);
  protected VnfmHelper vnfmHelper;
  protected VnfmManagerEndpoint vnfmManagerEndpoint;
  private ExecutorService executor;
  protected static String brokerIp;
  protected static String brokerPort;
  protected static String monitoringIp;
  protected static String timezone;
  protected static String username;
  protected static String password;
  protected static String exchangeName;
  protected static String nsrId;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  private boolean enabled;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  private String description;

  @PreDestroy
  private void shutdown() {}

  @PostConstruct
  private void init() {
    setup();
    executor =
        Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("concurrency", "15")));
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public abstract void query();

  @Override
  public abstract VirtualNetworkFunctionRecord scale(
      Action scaleInOrOut,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent component,
      Object scripts,
      VNFRecordDependency dependency)
      throws Exception;

  @Override
  public abstract void checkInstantiationFeasibility();

  @Override
  public abstract VirtualNetworkFunctionRecord heal(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance component,
      String cause)
      throws Exception;

  @Override
  public abstract VirtualNetworkFunctionRecord updateSoftware(
      Script script, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

  @Override
  public abstract VirtualNetworkFunctionRecord modify(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFRecordDependency dependency)
      throws Exception;

  @Override
  public abstract void upgradeSoftware();

  @Override
  public abstract VirtualNetworkFunctionRecord terminate(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

  public abstract void handleError(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

  protected void loadProperties() {
    properties = new Properties();
    try {
      properties.load(ClassLoader.getSystemResourceAsStream("conf.properties"));
    } catch (IOException e) {
      e.printStackTrace();
      log.error(e.getLocalizedMessage());
    }
    endpoint = (String) properties.get("endpoint");
    type = (String) properties.get("type");
    endpointType = properties.getProperty("endpoint-type", "RABBIT");
    description = properties.getProperty("description", "");
    enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
  }

  protected void onAction(NFVMessage message) throws NotFoundException, BadFormatException {

    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;

    try {
      log.debug(
          "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
              + message.getAction()
              + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      log.trace("VNFM: Received Message: " + message.getAction());
      NFVMessage nfvMessage = null;
      OrVnfmGenericMessage orVnfmGenericMessage = null;
      switch (message.getAction()) {
        case SCALE_IN:
          OrVnfmScalingMessage scalingMessage = (OrVnfmScalingMessage) message;
          nsrId = scalingMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
          virtualNetworkFunctionRecord = scalingMessage.getVirtualNetworkFunctionRecord();
          VNFCInstance vnfcInstanceToRemove = scalingMessage.getVnfcInstance();

          virtualNetworkFunctionRecord =
              this.scale(
                  Action.SCALE_IN, virtualNetworkFunctionRecord, vnfcInstanceToRemove, null, null);
          nfvMessage = null;
          break;
        case SCALE_OUT:
          scalingMessage = (OrVnfmScalingMessage) message;
          // TODO I don't know if, using a bean of this class the instance can be destroyed and recreated and
          // parameters could be lost
          getExtension(scalingMessage.getExtension());

          nsrId = scalingMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
          virtualNetworkFunctionRecord = scalingMessage.getVirtualNetworkFunctionRecord();
          VNFRecordDependency dependency = scalingMessage.getDependency();
          VNFComponent component = scalingMessage.getComponent();
          String mode = scalingMessage.getMode();

          log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
          log.info("Adding VNFComponent: " + component);
          log.trace("The mode is:" + mode);
          VNFCInstance vnfcInstance_new = null;
          if (!properties.getProperty("allocate", "true").equalsIgnoreCase("true")) {
            NFVMessage message2 =
                vnfmHelper.sendAndReceive(
                    VnfmUtils.getNfvScalingMessage(getUserData(), virtualNetworkFunctionRecord));
            if (message2 instanceof OrVnfmGenericMessage) {
              OrVnfmGenericMessage message1 = (OrVnfmGenericMessage) message2;
              virtualNetworkFunctionRecord = message1.getVnfr();
              log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
            } else if (message2 instanceof OrVnfmErrorMessage) {
              this.handleError(((OrVnfmErrorMessage) message2).getVnfr());
              return;
            }
            vnfcInstance_new = getVnfcInstance(virtualNetworkFunctionRecord, component);
            if (vnfcInstance_new == null) {
              throw new RuntimeException("no new VNFCInstance found. This should not happen...");
            }
            if (mode != null && mode.equalsIgnoreCase("standby")) {
              vnfcInstance_new.setState("STANDBY");
            }
          }

          Object scripts;
          if (scalingMessage.getVnfPackage() == null) {
            scripts = new HashSet<>();
          } else if (scalingMessage.getVnfPackage().getScriptsLink() != null) {
            scripts = scalingMessage.getVnfPackage().getScriptsLink();
          } else {
            scripts = scalingMessage.getVnfPackage().getScripts();
          }

          VirtualNetworkFunctionRecord vnfr =
              this.scale(
                  Action.SCALE_OUT,
                  virtualNetworkFunctionRecord,
                  vnfcInstance_new,
                  scripts,
                  dependency);
          if (vnfcInstance_new == null) {
            log.warn(
                "No new VNFCInstance found, either a bug or was not possible to instantiate it.");
          }
          nfvMessage = VnfmUtils.getNfvMessageScaled(Action.SCALED, vnfr, vnfcInstance_new);
          break;
        case SCALING:
          break;
        case ERROR:
          OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) message;
          nsrId = errorMessage.getVnfr().getParent_ns_id();
          log.error("ERROR Received: " + errorMessage.getMessage());
          handleError(errorMessage.getVnfr());

          nfvMessage = null;
          break;
        case MODIFY:
          orVnfmGenericMessage = (OrVnfmGenericMessage) message;
          virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
          nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
          nfvMessage =
              VnfmUtils.getNfvMessage(
                  Action.MODIFY,
                  this.modify(orVnfmGenericMessage.getVnfr(), orVnfmGenericMessage.getVnfrd()));
          break;
        case RELEASE_RESOURCES:
          orVnfmGenericMessage = (OrVnfmGenericMessage) message;
          nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
          virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
          nfvMessage =
              VnfmUtils.getNfvMessage(
                  Action.RELEASE_RESOURCES, this.terminate(virtualNetworkFunctionRecord));
          break;
        case INSTANTIATE:
          OrVnfmInstantiateMessage orVnfmInstantiateMessage = (OrVnfmInstantiateMessage) message;
          Map<String, String> extension = orVnfmInstantiateMessage.getExtension();

          log.debug("Extensions are: " + extension);
          log.debug("Keys are: " + orVnfmInstantiateMessage.getKeys());
          getExtension(extension);

          Map<String, Collection<VimInstance>> vimInstances =
              orVnfmInstantiateMessage.getVimInstances();
          virtualNetworkFunctionRecord =
              createVirtualNetworkFunctionRecord(
                  orVnfmInstantiateMessage.getVnfd(),
                  orVnfmInstantiateMessage.getVnfdf().getFlavour_key(),
                  orVnfmInstantiateMessage.getVlrs(),
                  orVnfmInstantiateMessage.getExtension(),
                  vimInstances);
          GrantOperation grantOperation = new GrantOperation();
          grantOperation.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

          Future<OrVnfmGrantLifecycleOperationMessage> result = executor.submit(grantOperation);
          OrVnfmGrantLifecycleOperationMessage msg;
          try {
            msg = result.get();
            if (msg == null) {
              return;
            }
          } catch (ExecutionException e) {
            log.error("Got exception while allocating vms");
            throw e.getCause();
          }

          virtualNetworkFunctionRecord = msg.getVirtualNetworkFunctionRecord();
          Map<String, VimInstance> vimInstanceChosen = msg.getVduVim();

          log.trace("VERSION IS: " + virtualNetworkFunctionRecord.getHb_version());

          if (!properties.getProperty("allocate", "true").equalsIgnoreCase("true")) {
            AllocateResources allocateResources = new AllocateResources();
            allocateResources.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
            allocateResources.setVimInstances(vimInstanceChosen);
            allocateResources.setKeyPairs(orVnfmInstantiateMessage.getKeys());
            try {
              virtualNetworkFunctionRecord = executor.submit(allocateResources).get();
              if (virtualNetworkFunctionRecord == null) {
                return;
              }
            } catch (ExecutionException e) {
              log.error("Got exception while allocating vms");
              throw e.getCause();
            }
          }
          setupProvides(virtualNetworkFunctionRecord);

          if (orVnfmInstantiateMessage.getVnfPackage() != null) {
            if (orVnfmInstantiateMessage.getVnfPackage().getScriptsLink() != null) {
              virtualNetworkFunctionRecord =
                  instantiate(
                      virtualNetworkFunctionRecord,
                      orVnfmInstantiateMessage.getVnfPackage().getScriptsLink(),
                      vimInstances);
            } else {
              virtualNetworkFunctionRecord =
                  instantiate(
                      virtualNetworkFunctionRecord,
                      orVnfmInstantiateMessage.getVnfPackage().getScripts(),
                      vimInstances);
            }
          } else {
            virtualNetworkFunctionRecord =
                instantiate(virtualNetworkFunctionRecord, null, vimInstances);
          }
          nfvMessage = VnfmUtils.getNfvMessage(Action.INSTANTIATE, virtualNetworkFunctionRecord);
          break;
        case RELEASE_RESOURCES_FINISH:
          break;
        case UPDATE:
          OrVnfmUpdateMessage orVnfmUpdateMessage = (OrVnfmUpdateMessage) message;
          nfvMessage =
              VnfmUtils.getNfvMessage(
                  Action.UPDATE,
                  updateSoftware(orVnfmUpdateMessage.getScript(), orVnfmUpdateMessage.getVnfr()));
          break;
        case HEAL:
          OrVnfmHealVNFRequestMessage orVnfmHealMessage = (OrVnfmHealVNFRequestMessage) message;

          nsrId = orVnfmHealMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
          VirtualNetworkFunctionRecord vnfrObtained =
              this.heal(
                  orVnfmHealMessage.getVirtualNetworkFunctionRecord(),
                  orVnfmHealMessage.getVnfcInstance(),
                  orVnfmHealMessage.getCause());
          nfvMessage =
              VnfmUtils.getNfvMessageHealed(
                  Action.HEAL, vnfrObtained, orVnfmHealMessage.getVnfcInstance());

          break;
        case INSTANTIATE_FINISH:
          break;
        case CONFIGURE:
          orVnfmGenericMessage = (OrVnfmGenericMessage) message;
          virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
          nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
          nfvMessage =
              VnfmUtils.getNfvMessage(Action.CONFIGURE, configure(orVnfmGenericMessage.getVnfr()));
          break;
        case START:
          {
            OrVnfmStartStopMessage orVnfmStartStopMessage = (OrVnfmStartStopMessage) message;
            virtualNetworkFunctionRecord = orVnfmStartStopMessage.getVirtualNetworkFunctionRecord();
            nsrId = virtualNetworkFunctionRecord.getParent_ns_id();
            VNFCInstance vnfcInstance = orVnfmStartStopMessage.getVnfcInstance();

            if (vnfcInstance == null) // Start the VNF Record
            {
              nfvMessage =
                  VnfmUtils.getNfvMessage(Action.START, start(virtualNetworkFunctionRecord));
            } else // Start the VNFC Instance
            {
              nfvMessage =
                  VnfmUtils.getNfvMessageStartStop(
                      Action.START,
                      startVNFCInstance(virtualNetworkFunctionRecord, vnfcInstance),
                      vnfcInstance);
            }
            break;
          }
        case STOP:
          {
            OrVnfmStartStopMessage orVnfmStartStopMessage = (OrVnfmStartStopMessage) message;
            virtualNetworkFunctionRecord = orVnfmStartStopMessage.getVirtualNetworkFunctionRecord();
            nsrId = virtualNetworkFunctionRecord.getParent_ns_id();
            VNFCInstance vnfcInstance = orVnfmStartStopMessage.getVnfcInstance();

            if (vnfcInstance == null) // Stop the VNF Record
            {
              nfvMessage = VnfmUtils.getNfvMessage(Action.STOP, stop(virtualNetworkFunctionRecord));
            } else // Stop the VNFC Instance
            {
              nfvMessage =
                  VnfmUtils.getNfvMessageStartStop(
                      Action.STOP,
                      stopVNFCInstance(virtualNetworkFunctionRecord, vnfcInstance),
                      vnfcInstance);
            }

            break;
          }
        case RESUME:
          {
            OrVnfmGenericMessage orVnfmResumeMessage = (OrVnfmGenericMessage) message;
            virtualNetworkFunctionRecord = orVnfmResumeMessage.getVnfr();
            nsrId = virtualNetworkFunctionRecord.getParent_ns_id();

            Action resumedAction = this.getResumedAction(virtualNetworkFunctionRecord, null);
            nfvMessage =
                VnfmUtils.getNfvMessage(
                    resumedAction,
                    resume(virtualNetworkFunctionRecord, null, orVnfmResumeMessage.getVnfrd()));
            log.debug(
                "Resuming vnfr '"
                    + virtualNetworkFunctionRecord.getId()
                    + "' with dependency target: '"
                    + orVnfmResumeMessage.getVnfrd().getTarget()
                    + "' for action: "
                    + resumedAction
                    + "'");
            break;
          }
      }

      log.debug(
          "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      if (nfvMessage != null) {
        log.debug("send " + nfvMessage.getClass().getSimpleName() + " to NFVO");
        vnfmHelper.sendToNfvo(nfvMessage);
      }
    } catch (Throwable e) {
      log.error("ERROR: ", e);
      if (e instanceof VnfmSdkException) {
        VnfmSdkException vnfmSdkException = (VnfmSdkException) e;
        if (vnfmSdkException.getVnfr() != null) {
          log.debug("sending VNFR with version: " + vnfmSdkException.getVnfr().getHb_version());
          vnfmHelper.sendToNfvo(
              VnfmUtils.getNfvErrorMessage(vnfmSdkException.getVnfr(), vnfmSdkException, nsrId));
          return;
        }
      } else if (e.getCause() instanceof VnfmSdkException) {
        VnfmSdkException vnfmSdkException = (VnfmSdkException) e.getCause();
        if (vnfmSdkException.getVnfr() != null) {
          log.debug("sending VNFR with version: " + vnfmSdkException.getVnfr().getHb_version());
          vnfmHelper.sendToNfvo(
              VnfmUtils.getNfvErrorMessage(vnfmSdkException.getVnfr(), vnfmSdkException, nsrId));
          return;
        }
      }
      vnfmHelper.sendToNfvo(VnfmUtils.getNfvErrorMessage(virtualNetworkFunctionRecord, e, nsrId));
    }
  }

  private VNFCInstance getVnfcInstance(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFComponent component) {
    VNFCInstance vnfcInstance_new = null;
    boolean found = false;
    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        if (vnfcInstance.getVnfComponent().getId().equals(component.getId())) {
          vnfcInstance_new = vnfcInstance;
          fillProvidesVNFC(virtualNetworkFunctionRecord, vnfcInstance);
          found = true;
          log.debug("VNFComponentInstance FOUND : " + vnfcInstance_new.getVnfComponent());
          break;
        }
      }
      if (found) {
        break;
      }
    }
    return vnfcInstance_new;
  }

  private void getExtension(Map<String, String> extension) {
    log.debug("Extensions are: " + extension);

    brokerIp = extension.get("brokerIp");
    brokerPort = extension.get("brokerPort");
    monitoringIp = extension.get("monitoringIp");
    timezone = extension.get("timezone");
    username = extension.get("username");
    password = extension.get("password");
    exchangeName = extension.get("exchangeName");
    nsrId = extension.get("nsr-id");
  }

  private void setupProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {}

  private void fillProvidesVNFC(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance) {}

  /**
   * This method needs to set all the parameter specified in the VNFDependency.parameters
   *
   * @param virtualNetworkFunctionRecord
   */
  protected void fillSpecificProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {}

  /**
   * This method can be overwritten in case you want a specific initialization of the
   * VirtualNetworkFunctionRecordShort from the VirtualNetworkFunctionDescriptor
   *
   * @param virtualNetworkFunctionDescriptor
   * @param extension
   * @param vimInstances
   * @return The new VirtualNetworkFunctionRecordShort
   * @throws BadFormatException
   * @throws NotFoundException
   */
  protected VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String flavourId,
      Set<VirtualLinkRecord> virtualLinkRecords,
      Map<String, String> extension,
      Map<String, Collection<VimInstance>> vimInstances)
      throws BadFormatException, NotFoundException {
    try {
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
          VNFRUtils.createVirtualNetworkFunctionRecord(
              virtualNetworkFunctionDescriptor,
              flavourId,
              extension.get("nsr-id"),
              virtualLinkRecords,
              vimInstances);
      for (InternalVirtualLink internalVirtualLink :
          virtualNetworkFunctionRecord.getVirtual_link()) {
        for (VirtualLinkRecord virtualLinkRecord : virtualLinkRecords) {
          if (internalVirtualLink.getName().equals(virtualLinkRecord.getName())) {
            internalVirtualLink.setExtId(virtualLinkRecord.getExtId());
            internalVirtualLink.setConnectivity_type(virtualLinkRecord.getConnectivity_type());
          }
        }
      }
      log.debug("Created VirtualNetworkFunctionRecordShort: " + virtualNetworkFunctionRecord);
      return virtualNetworkFunctionRecord;
    } catch (NotFoundException e) {
      e.printStackTrace();
      vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, null));
      throw e;
    } catch (BadFormatException e) {
      e.printStackTrace();
      vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, null));
      throw e;
    }
  }

  public abstract VirtualNetworkFunctionRecord start(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

  public abstract VirtualNetworkFunctionRecord stop(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

  public abstract VirtualNetworkFunctionRecord startVNFCInstance(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance)
      throws Exception;

  public abstract VirtualNetworkFunctionRecord stopVNFCInstance(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance)
      throws Exception;

  public abstract VirtualNetworkFunctionRecord configure(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

  public abstract VirtualNetworkFunctionRecord resume(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      VNFRecordDependency dependency)
      throws Exception;

  protected Action getResumedAction(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance) {
    return null;
  }

  /** This method unsubscribe the VNFM in the NFVO */
  protected abstract void unregister();

  /** This method subscribe the VNFM to the NFVO sending the right endpoint */
  protected abstract void register();

  /**
   * This method setups the VNFM and then subscribe it to the NFVO. We recommend to not change this
   * method or at least to override calling super()
   */
  protected void setup() {
    loadProperties();
    vnfmManagerEndpoint = new VnfmManagerEndpoint();
    vnfmManagerEndpoint.setType(this.type);
    vnfmManagerEndpoint.setDescription(this.description);
    vnfmManagerEndpoint.setEnabled(this.enabled);
    vnfmManagerEndpoint.setActive(true);
    vnfmManagerEndpoint.setEndpoint(this.endpoint);
    log.debug("creating VnfmManagerEndpoint for vnfm endpointType: " + this.endpointType);
    vnfmManagerEndpoint.setEndpointType(EndpointType.valueOf(this.endpointType));
    register();
  }

  class GrantOperation implements Callable<OrVnfmGrantLifecycleOperationMessage> {
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
      return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
      this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    private OrVnfmGrantLifecycleOperationMessage grantLifecycleOperation() throws VnfmSdkException {
      NFVMessage response;
      try {
        response =
            vnfmHelper.sendAndReceive(
                VnfmUtils.getNfvMessage(Action.GRANT_OPERATION, virtualNetworkFunctionRecord));
      } catch (Exception e) {
        throw new VnfmSdkException("Not able to grant operation", e, virtualNetworkFunctionRecord);
      }
      if (response != null) {
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
          throw new VnfmSdkException(
              "Not able to grant operation because: "
                  + ((OrVnfmErrorMessage) response).getMessage(),
              ((OrVnfmErrorMessage) response).getVnfr());
        }
        OrVnfmGrantLifecycleOperationMessage orVnfmGrantLifecycleOperationMessage =
            (OrVnfmGrantLifecycleOperationMessage) response;

        return orVnfmGrantLifecycleOperationMessage;
      }
      return null;
    }

    @Override
    public OrVnfmGrantLifecycleOperationMessage call() throws Exception {
      return this.grantLifecycleOperation();
    }
  }

  class AllocateResources implements Callable<VirtualNetworkFunctionRecord> {
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    private Set<Key> keyPairs;

    public void setVimInstances(Map<String, VimInstance> vimInstances) {
      this.vimInstances = vimInstances;
    }

    private Map<String, VimInstance> vimInstances;

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
      return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
      this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    public VirtualNetworkFunctionRecord allocateResources() throws VnfmSdkException {
      NFVMessage response;
      try {

        String userData = getUserData();
        log.debug("Userdata sent to NFVO: " + userData);
        response =
            vnfmHelper.sendAndReceive(
                VnfmUtils.getNfvInstantiateMessage(
                    virtualNetworkFunctionRecord, vimInstances, userData, keyPairs));
      } catch (Exception e) {
        log.error("" + e.getMessage());
        throw new VnfmSdkException(
            "Not able to allocate Resources", e, virtualNetworkFunctionRecord);
      }
      if (response != null) {
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
          OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) response;
          log.error(errorMessage.getMessage());
          virtualNetworkFunctionRecord = errorMessage.getVnfr();
          throw new VnfmSdkException(
              "Not able to allocate Resources because: " + errorMessage.getMessage(),
              virtualNetworkFunctionRecord);
        }
        OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
        log.debug("Received from ALLOCATE: " + orVnfmGenericMessage.getVnfr());
        return orVnfmGenericMessage.getVnfr();
      }
      return null;
    }

    @Override
    public VirtualNetworkFunctionRecord call() throws Exception {
      return this.allocateResources();
    }

    public void setKeyPairs(Set<Key> keyPairs) {
      this.keyPairs = keyPairs;
    }
  }

  protected String getUserData() {
    return "#!/bin/bash\n";
  }
}
