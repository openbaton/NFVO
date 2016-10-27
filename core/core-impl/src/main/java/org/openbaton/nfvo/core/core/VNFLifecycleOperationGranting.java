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

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope
public class VNFLifecycleOperationGranting
    implements org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private VimBroker vimBroker;
  @Autowired private VimRepository vimInstanceRepository;

  @Override
  public Map<String, VimInstance> grantLifecycleOperation(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws VimException, PluginException {
    Map<String, VimInstance> result = new HashMap<>();

    //HashMap holds how many VNFCInstances are needed to deploy on a specific VimInstance
    HashMap<VimInstance, Integer> countVDUsOnVimInstances = new HashMap<>();

    //Find how many VNFCInstances are needed to deploy on a specific VimInstance
    log.info("Granting Lifecycle Operation for vnfr: " + virtualNetworkFunctionRecord.getName());
    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      for (String vimName : vdu.getVimInstanceName()) {

        VimInstance vimInstance = null;

        for (VimInstance vi : vimInstanceRepository.findByProjectId(vdu.getProjectId())) {
          if (vimName.equals(vi.getName())) vimInstance = vi;
        }

        if (countVDUsOnVimInstances.containsKey(vimInstance)) {
          countVDUsOnVimInstances.put(
              vimInstance,
              countVDUsOnVimInstances.get(vimInstance)
                  + vdu.getVnfc().size()
                  - vdu.getVnfc_instance().size());
        } else {
          log.debug(
              "VimInstance: "
                  + vdu.getVimInstanceName()
                  + "\n VNFC: "
                  + vdu.getVnfc()
                  + "\nVNFCINST: "
                  + vdu.getVnfc_instance());
          countVDUsOnVimInstances.put(
              vimInstance, vdu.getVnfc().size() - vdu.getVnfc_instance().size());
        }
      }
    }

    //Check if enough resources are available for the deployment
    log.debug("Checking if enough resources are available on the defined VimInstance.");
    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      VimInstance vimInstanceChosen =
          pickVimInstance(
              virtualDeploymentUnit.getVimInstanceName(),
              countVDUsOnVimInstances,
              virtualNetworkFunctionRecord);
      if (vimInstanceChosen != null) {
        log.info(
            "Enough resources are available for deploying VDU in vimInstance: "
                + vimInstanceChosen.getName());
        result.put(virtualDeploymentUnit.getId(), vimInstanceChosen);
      }
    }

    return result;
  }

  private VimInstance pickVimInstance(
      Collection<String> vimInstanceNames,
      HashMap<VimInstance, Integer> countVDUsOnVimInstances,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws VimException, PluginException {
    if (countVDUsOnVimInstances.isEmpty()) {
      for (VimInstance vimInstance : vimInstanceRepository.findAll()) {
        Quota leftQuota = vimBroker.getLeftQuota(vimInstance);
        log.debug(
            "Left Quota on VimInstance " + vimInstance.getName() + " at start is " + leftQuota);

        //Fetch the Flavor for getting allocated resources needed
        DeploymentFlavour flavor = null;
        for (DeploymentFlavour currentFlavor : vimInstance.getFlavours()) {
          if (currentFlavor
              .getFlavour_key()
              .equals(virtualNetworkFunctionRecord.getDeployment_flavour_key())) {
            flavor = currentFlavor;
            break;
          }
        }

        //Subtract needed resources from the left resources
        int nc = 0;

        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
          for (VNFComponent ignored : virtualDeploymentUnit.getVnfc()) {
            nc++;
          }
        }

        for (int i = 1; i <= nc; i++) {
          leftQuota.setInstances(leftQuota.getInstances() - 1);
          leftQuota.setCores(leftQuota.getCores() - flavor.getVcpus());
          leftQuota.setRam(leftQuota.getRam() - flavor.getRam());
          log.debug(
              "Left Quota on VimInstance "
                  + vimInstance.getName()
                  + " after considering VDU is "
                  + leftQuota);
        }

        //If one value is negative, it is not possible to deploy the VNFR on (at least on one VimInstance) -> return false
        if (leftQuota.getInstances() < 0 || leftQuota.getRam() < 0 || leftQuota.getCores() < 0) {
          log.warn(
              "Not enough resources are available to deploy VNFR "
                  + virtualNetworkFunctionRecord.getName());
          continue;
        }
        return vimInstance;
      }
    } else {
      for (VimInstance vimInstance1 : countVDUsOnVimInstances.keySet()) {
        if (vimInstanceNames.contains(vimInstance1.getName())) {
          Quota leftQuota = vimBroker.getLeftQuota(vimInstance1);
          log.debug(
              "Left Quota on VimInstance " + vimInstance1.getName() + " at start is " + leftQuota);
          //Fetch the Flavor for getting allocated resources needed
          DeploymentFlavour flavor = null;
          for (DeploymentFlavour currentFlavor : vimInstance1.getFlavours()) {
            if (currentFlavor
                .getFlavour_key()
                .equals(virtualNetworkFunctionRecord.getDeployment_flavour_key())) {
              flavor = currentFlavor;
              break;
            }
          }
          //Subtract needed resources from the left resources
          for (int i = 1; i <= countVDUsOnVimInstances.get(vimInstance1); i++) {
            leftQuota.setInstances(leftQuota.getInstances() - 1);
            leftQuota.setCores(leftQuota.getCores() - flavor.getVcpus());
            leftQuota.setRam(leftQuota.getRam() - flavor.getRam());
            log.debug(
                "Left Quota on VimInstance "
                    + vimInstance1.getName()
                    + " after considering VDU is "
                    + leftQuota);
          }
          //If one value is negative, it is not possible to deploy the VNFR on (at least on one VimInstance) -> return false
          if (leftQuota.getInstances() < 0 || leftQuota.getRam() < 0 || leftQuota.getCores() < 0) {
            log.warn(
                "Not enough resources are available to deploy VNFR "
                    + virtualNetworkFunctionRecord.getName());
            continue;
          }
          return vimInstance1;
        }
      }
    }
    return null;
  }
}
