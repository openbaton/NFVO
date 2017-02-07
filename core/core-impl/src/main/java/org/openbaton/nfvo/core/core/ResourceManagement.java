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

package org.openbaton.nfvo.core.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.repositories.KeyRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/** Created by lto on 11/06/15. */
@Service
@Scope("prototype")
@ConfigurationProperties
public class ResourceManagement implements org.openbaton.nfvo.core.interfaces.ResourceManagement {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private VimBroker vimBroker;
  @Autowired private VimRepository vimInstanceRepository;

  @Autowired private VnfPlacementManagement vnfPlacementManagement;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private KeyRepository keyRepository;

  @Override
  @Async
  public Future<List<String>> allocate(
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VimInstance vimInstance,
      String userdata,
      Set<Key> keys)
      throws VimException, ExecutionException, InterruptedException, PluginException {
    List<Future<VNFCInstance>> instances = new ArrayList<>();
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    log.debug("Executing allocate with Vim: " + vim.getClass().getSimpleName());
    log.debug("NAME: " + virtualNetworkFunctionRecord.getName());
    log.debug("ID: " + virtualDeploymentUnit.getId());
    String hostname = virtualNetworkFunctionRecord.getName().replaceAll("_", "-");
    log.debug("Hostname is: " + hostname);
    virtualDeploymentUnit.setHostname(hostname);
    for (VNFComponent component : virtualDeploymentUnit.getVnfc()) {
      log.trace("UserData is: " + userdata);
      Map<String, String> floatingIps = new HashMap<>();
      for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
        if (connectionPoint.getFloatingIp() != null)
          floatingIps.put(
              connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
      }
      log.info("FloatingIp chosen are: " + floatingIps);
      Future<VNFCInstance> added =
          vim.allocate(
              vimInstance,
              virtualDeploymentUnit,
              virtualNetworkFunctionRecord,
              component,
              userdata,
              floatingIps,
              keys);
      instances.add(added);
    }
    List<String> ids = new ArrayList<>();
    for (Future<VNFCInstance> futureInstance : instances) {
      VNFCInstance instance = futureInstance.get();
      virtualDeploymentUnit.getVnfc_instance().add(instance);
      ids.add(instance.getVc_id());
      log.debug("Launched VM with id: " + instance.getVc_id());
      Map<String, String> floatingIps = new HashMap<>();
      for (VNFDConnectionPoint connectionPoint : instance.getVnfComponent().getConnection_point()) {
        if (connectionPoint.getFloatingIp() != null)
          floatingIps.put(
              connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
      }
      if (floatingIps.size() != instance.getFloatingIps().size()) {
        log.warn("NFVO wasn't able to all associate FloatingIPs. Is there enough available?");
        log.debug("Expected FloatingIPs: " + floatingIps);
        log.debug("Real FloatingIPs: " + instance.getFloatingIps());
      }
    }
    log.info("Finished deploying VMs with external ids: " + ids);
    return new AsyncResult<>(ids);
  }

  private String allocateVNFC(
      VimInstance vimInstance,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim,
      VNFComponent component,
      String userdata,
      Set<Key> keys)
      throws InterruptedException, ExecutionException, VimException {
    log.trace("UserData is: " + userdata);
    Map<String, String> floatinIps = new HashMap<>();
    for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
      floatinIps.put(connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
    }
    log.info("FloatingIp chosen are: " + floatinIps);
    VNFCInstance added =
        vim.allocate(
                vimInstance,
                virtualDeploymentUnit,
                virtualNetworkFunctionRecord,
                component,
                userdata,
                floatinIps,
                keys)
            .get();
    virtualDeploymentUnit.getVnfc_instance().add(added);
    if (!floatinIps.isEmpty() && added.getFloatingIps().isEmpty())
      log.warn("NFVO wasn't able to associate FloatingIPs. Is there enough available");
    return added.getVim_id();
  }

  @Override
  public List<Server> query(VimInstance vimInstance) throws VimException, PluginException {
    return vimBroker.getVim(vimInstance.getType()).queryResources(vimInstance);
  }

  @Override
  public void update(VirtualDeploymentUnit vdu) {}

  @Override
  public void scale(VirtualDeploymentUnit vdu) {}

  @Override
  public void migrate(VirtualDeploymentUnit vdu) {}

  @Override
  public void operate(VirtualDeploymentUnit vdu, String operation) {}

  @Override
  @Async
  public Future<Void> release(
      VirtualDeploymentUnit virtualDeploymentUnit, VNFCInstance vnfcInstance)
      throws VimException, ExecutionException, InterruptedException, PluginException {
    VimInstance vimInstance = vimInstanceRepository.findFirstById(vnfcInstance.getVim_id());
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim =
        vimBroker.getVim(vimInstance.getType());
    log.debug("Removing vnfcInstance: " + vnfcInstance);
    vim.release(vnfcInstance, vimInstance).get();
    virtualDeploymentUnit.getVnfc().remove(vnfcInstance.getVnfComponent());
    return new AsyncResult<>(null);
  }

  @Override
  public void createReservation(VirtualDeploymentUnit vdu) {}

  @Override
  public void queryReservation() {}

  @Override
  public void updateReservation(VirtualDeploymentUnit vdu) {}

  @Override
  public void releaseReservation(VirtualDeploymentUnit vdu) {}

  @Override
  @Async
  public Future<String> allocate(
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent componentToAdd,
      VimInstance vimInstance,
      String userdata)
      throws InterruptedException, ExecutionException, PluginException, VimException,
          VimDriverException {
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    log.debug("Executing allocate with Vim: " + vim.getClass().getSimpleName());
    log.debug("NAME: " + virtualNetworkFunctionRecord.getName());
    log.debug("ID: " + virtualDeploymentUnit.getId());
    // TODO retrive nsr->getKeys->keyRepository->getKeys
    Set<Key> keys = new HashSet<>();
    for (String keyName :
        nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id()).getKeyNames()) {
      keys.add(keyRepository.findKey(virtualNetworkFunctionRecord.getProjectId(), keyName));
    }
    String vnfc =
        allocateVNFC(
            vimInstance,
            virtualDeploymentUnit,
            virtualNetworkFunctionRecord,
            vim,
            componentToAdd,
            userdata,
            keys);
    return new AsyncResult<>(vnfc);
  }
}
