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

package org.openbaton.nfvo.vnfm_reg.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGrantLifecycleOperationMessage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.NsrNotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils;
import org.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.VirtualLinkRecordRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@ConfigurationProperties
public class GrantoperationTask extends AbstractTask {

  @Autowired private VnfPlacementManagement vnfPlacementManagement;
  @Autowired private VNFLifecycleOperationGranting lifecycleOperationGranting;
  @Autowired private NetworkServiceDescriptorRepository networkServiceDescriptorRepository;
  @Autowired private NetworkManagement networkManagement;
  @Autowired private VimManagement vimManagement;
  @Autowired private VirtualLinkRecordRepository vlrRepository;

  @Value("${nfvo.quota.check:false}")
  private boolean checkQuota;

  private static final Map<String, Object> lockMap = new HashMap<>();

  @Value("${nfvo.networks.dedicated:false}")
  private boolean dedicatedNetworks;

  @Override
  protected NFVMessage doWork() throws Exception {
    log.info("Executing task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());
    printOldAndNewHibernateVersion();

    //Save the vnfr since in the grantLifecycleOperation method we use vdu.getId()
    saveVirtualNetworkFunctionRecord();

    Map<String, BaseVimInstance> vimInstancesChosen = new HashMap<>();

    if (!checkQuota) {
      log.warn("Checking quota is disabled, please consider to enable it");

      for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
        BaseVimInstance vimInstance =
            vnfPlacementManagement.choseRandom(
                virtualDeploymentUnit.getVimInstanceName(),
                virtualNetworkFunctionRecord.getProjectId());
        performChecks(vimInstance, virtualDeploymentUnit);
        vimInstancesChosen.put(virtualDeploymentUnit.getId(), vimInstance);
      }
      log.info("Choose all Vim Instance for vnfr: " + virtualNetworkFunctionRecord.getName());

      saveVirtualNetworkFunctionRecord();
      log.trace(
          "VNFR ("
              + virtualNetworkFunctionRecord.getId()
              + ") current hibernate version is: "
              + virtualNetworkFunctionRecord.getHbVersion());

      setHistoryLifecycleEvent();
      OrVnfmGrantLifecycleOperationMessage nfvMessage = new OrVnfmGrantLifecycleOperationMessage();
      nfvMessage.setGrantAllowed(true);
      nfvMessage.setVduVim(vimInstancesChosen);
      nfvMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
      return nfvMessage;
    } else {

      for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
        log.debug(
            "For vdu "
                + virtualDeploymentUnit.getName()
                + " possible vim instances are: "
                + virtualDeploymentUnit.getVimInstanceName());
      }
      vimInstancesChosen =
          lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord);

      if (vimInstancesChosen.size() == virtualNetworkFunctionRecord.getVdu().size()) {
        for (Map.Entry<String, BaseVimInstance> entry : vimInstancesChosen.entrySet()) {
          performChecks(
              entry.getValue(),
              virtualNetworkFunctionRecord
                  .getVdu()
                  .stream()
                  .filter(vdu -> vdu.getId().equals(entry.getKey()))
                  .findFirst()
                  .orElseThrow(() -> new RuntimeException("That's impossible")));
        }

      } else {
        // there are not enough resources for deploying VNFR
        log.error(
            "Not enough resources for deploying VNFR " + virtualNetworkFunctionRecord.getName());
        virtualNetworkFunctionRecord.setStatus(Status.ERROR);
        saveVirtualNetworkFunctionRecord();
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
        return new OrVnfmErrorMessage(
            virtualNetworkFunctionRecord,
            "Not enough resources for deploying VNFR " + virtualNetworkFunctionRecord.getName());
      }
    }
    log.info("Finished task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());

    saveVirtualNetworkFunctionRecord();
    log.trace(
        "VNFR ("
            + virtualNetworkFunctionRecord.getId()
            + ") current hibernate version is: "
            + virtualNetworkFunctionRecord.getHbVersion());
    setHistoryLifecycleEvent();
    OrVnfmGrantLifecycleOperationMessage nfvMessage = new OrVnfmGrantLifecycleOperationMessage();
    nfvMessage.setGrantAllowed(true);
    nfvMessage.setVduVim(vimInstancesChosen);
    nfvMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    return nfvMessage;
  }

  private void performChecks(
      BaseVimInstance vimInstance, VirtualDeploymentUnit virtualDeploymentUnit)
      throws VimException, NotFoundException, BadRequestException, IOException,
          InterruptedException, ExecutionException, PluginException {
    Object lock;
    String key = String.format("%s%s", vimInstance.getName(), vimInstance.getProjectId());
    synchronized (lockMap) {
      lock = lockMap.computeIfAbsent(key, k -> new Object());
    }
    synchronized (lock) {
      vimInstance = vimManagement.query(vimInstance.getId(), vimInstance.getProjectId());
      vimInstance = vimManagement.refresh(vimInstance, true).get();

      // check images
      if (!vimInstance.getType().equals("test")) {
        log.debug(
            String.format(
                "One of the images %s must be available in the VimInstance %s",
                virtualDeploymentUnit.getVm_image(), vimInstance.getName()));
        BaseVimInstance finalVimInstance = vimInstance;
        if (virtualDeploymentUnit
            .getVm_image()
            .stream()
            .noneMatch(
                name -> VimInstanceUtils.findActiveImagesByName(finalVimInstance, name).size() > 0))
          throw new VimException(
              String.format(
                  "None of the images %s where found on the chosen vim instance %s",
                  virtualDeploymentUnit.getVm_image(), vimInstance.getName()));
      }

      //check networks
      if (!networkServiceRecordRepository.exists(virtualNetworkFunctionRecord.getParent_ns_id()))
        throw new NsrNotFoundException(
            String.format(
                "NSR with id [%s] was not found, probably deleted during deploying",
                virtualNetworkFunctionRecord.getParent_ns_id()));
      NetworkServiceRecord networkServiceRecord =
          networkServiceRecordRepository.findFirstById(
              virtualNetworkFunctionRecord.getParent_ns_id());
      NetworkServiceDescriptor networkServiceDescriptor =
          networkServiceDescriptorRepository.findFirstById(
              networkServiceRecord.getDescriptor_reference());

      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
          networkServiceDescriptor
              .getVnfd()
              .stream()
              .filter(vnfd -> vnfd.getName().equals(virtualNetworkFunctionRecord.getName()))
              .findFirst()
              .orElseThrow(() -> new NotFoundException("That's impossible"));

      Exception[] ex = new Exception[1];
      Map<String, BaseNetwork> networkToAdd = new HashMap<>();
      BaseVimInstance finalVimInstance1 = vimInstance;
      networkServiceRecord
          .getVlr()
          .stream()
          .filter(
              virtualLinkRecord -> {
                log.debug(
                    String.format(
                        "Checking VLR %s for VNFR [%s]",
                        virtualLinkRecord, virtualNetworkFunctionRecord.getName()));
                for (BaseNetwork net : finalVimInstance1.getNetworks()) {
                  if (VimInstanceUtils.isVLRExisting(virtualLinkRecord, net, dedicatedNetworks)) {
                    virtualLinkRecord.setExtId(net.getExtId());
                    return false;
                  }
                }
                return true;
              })
          .forEach(
              virtualLinkRecord -> {
                try {
                  networkToAdd.put(
                      virtualLinkRecord.getId(),
                      VimInstanceUtils.createBaseNetwork(
                          networkServiceDescriptor,
                          virtualNetworkFunctionDescriptor,
                          virtualLinkRecord,
                          finalVimInstance1));
                } catch (BadRequestException e) {
                  e.printStackTrace();
                  ex[0] = e;
                }
              });
      if (ex[0] != null) {
        throw (BadRequestException) ex[0];
      }
      for (Map.Entry<String, BaseNetwork> entry : networkToAdd.entrySet()) {
        BaseNetwork net = networkManagement.add(vimInstance, entry.getValue());
        VirtualLinkRecord virtualLinkRecord =
            networkServiceRecord
                .getVlr()
                .stream()
                .filter(vlr -> vlr.getId().equals(entry.getKey()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("That's impossible"));
        virtualLinkRecord.setExtId(net.getExtId());
        //        virtualLinkRecord.setName(net.getName());
        virtualLinkRecord.setParent_ns(networkServiceRecord.getId());
        virtualLinkRecord.setVim_id(vimInstance.getId());
        virtualLinkRecord = vlrRepository.save(virtualLinkRecord);
        for (VNFComponent vnfComponent : virtualDeploymentUnit.getVnfc()) {
          for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
            if (virtualLinkRecord
                .getName()
                .equals(vnfdConnectionPoint.getVirtual_link_reference())) {
              vnfdConnectionPoint.setVirtual_link_reference(virtualLinkRecord.getName());
              vnfdConnectionPoint.setVirtual_link_reference_id(virtualLinkRecord.getExtId());
            }
          }
        }
      }
      vimManagement.refresh(vimInstance, false).get();
      for (VNFComponent vnfc : virtualDeploymentUnit.getVnfc()) {
        for (VNFDConnectionPoint vnfdConnectionPoint : vnfc.getConnection_point()) {
          networkServiceRecord
              .getVlr()
              .forEach(
                  vlr -> {
                    if (vlr.getName().equals(vnfdConnectionPoint.getVirtual_link_reference())) {
                      vnfdConnectionPoint.setVirtual_link_reference(vlr.getName());
                      vnfdConnectionPoint.setVirtual_link_reference_id(vlr.getExtId());
                      log.debug(
                          String.format(
                              "VNFR [%s] set CP [%s] to ExtID [%s]",
                              virtualNetworkFunctionRecord.getName(),
                              vnfdConnectionPoint.getVirtual_link_reference(),
                              vnfdConnectionPoint.getVirtual_link_reference_id()));
                    }
                  });
        }
      }
      saveVirtualNetworkFunctionRecord();
    }
  }

  @Override
  public boolean isAsync() {
    return true;
  }

  @Override
  protected void setEvent() {
    event = Event.GRANTED.name();
  }

  @Override
  protected void setDescription() {
    description =
        "All the resources that are contained in this VNFR were granted to be deployed in the chosen VIM(s)";
  }
}
