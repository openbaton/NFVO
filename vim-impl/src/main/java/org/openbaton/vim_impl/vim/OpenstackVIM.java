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

package org.openbaton.vim_impl.vim;

import java.util.*;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/** Created by lto on 12/05/15. */
@Service
@Scope("prototype")
public class OpenstackVIM extends GenericVIM {

  public OpenstackVIM(
      String username,
      String password,
      String brokerIp,
      int port,
      String managementPort,
      ApplicationContext context,
      String pluginName,
      int pluginTimeout)
      throws PluginException {
    super(
        "openstack",
        username,
        password,
        brokerIp,
        port,
        managementPort,
        context,
        pluginName,
        pluginTimeout);
  }

  public OpenstackVIM() {}

  @Override
  public Network update(VimInstance vimInstance, Network network) throws VimException {
    Network updatedNetwork = null;
    try {
      log.debug(
          "Updating Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
      updatedNetwork = client.updateNetwork(vimInstance, network);
      log.info(
          "Updated Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not updated Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not updated Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not updated Network with name: "
              + network.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    log.debug(
        "Updating Subnets for Network with name: "
            + network.getName()
            + " on VimInstance "
            + vimInstance.getName()
            + " -> "
            + network.getSubnets());
    Set<Subnet> updatedSubnets = new HashSet<>();
    List<String> updatedSubnetExtIds = new ArrayList<>();
    for (Subnet subnet : network.getSubnets()) {
      if (subnet.getExtId() != null) {
        try {
          log.debug(
              "Updating Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          Subnet updatedSubnet = client.updateSubnet(vimInstance, updatedNetwork, subnet);
          log.info(
              "Updated Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          updatedSubnet.setNetworkId(updatedNetwork.getId().toString());
          updatedSubnets.add(updatedSubnet);
          updatedSubnetExtIds.add(updatedSubnet.getExtId());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not updated Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not updated Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not updated Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      } else {
        try {
          log.debug(
              "Creating Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          Subnet createdSubnet = client.createSubnet(vimInstance, updatedNetwork, subnet);
          log.info(
              "Created Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          createdSubnet.setNetworkId(updatedNetwork.getId().toString());
          updatedSubnets.add(createdSubnet);
          updatedSubnetExtIds.add(createdSubnet.getExtId());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not created Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not created Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not created Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      }
    }
    updatedNetwork.setSubnets(updatedSubnets);
    List<String> existingSubnetExtIds = null;
    try {
      log.debug(
          "Listing all Subnet IDs of Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
      existingSubnetExtIds = client.getSubnetsExtIds(vimInstance, updatedNetwork.getExtId());
      log.info(
          "Listed all Subnet IDs of Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName()
              + " -> Subnet IDs: "
              + existingSubnetExtIds);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed Subnets of Network with name: "
                + network.getName()
                + " successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed Subnets of Network with name: "
                + network.getName()
                + " successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed Subnets of Network with name: "
              + network.getName()
              + " successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    for (String existingSubnetExtId : existingSubnetExtIds) {
      if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
        try {
          log.debug(
              "Deleting Subnet with id: "
                  + existingSubnetExtId
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          client.deleteSubnet(vimInstance, existingSubnetExtId);
          log.info(
              "Deleted Subnet with id: "
                  + existingSubnetExtId
                  + " on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not Deleted Subnet with id: "
                    + existingSubnetExtId
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not Deleted Subnet with id: "
                    + existingSubnetExtId
                    + " successfully on Network with name: "
                    + network.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not Deleted Subnet with id: "
                  + existingSubnetExtId
                  + " successfully on Network with name: "
                  + network.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      }
    }
    log.info(
        "Subnets of Network with name: "
            + network.getName()
            + " updated successfully on VimInstance "
            + vimInstance.getName());
    return updatedNetwork;
  }

  @Override
  @Async
  public Future<VNFCInstance> allocate(
      VimInstance vimInstance,
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord vnfr,
      VNFComponent vnfComponent,
      String userdata,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimException {
    log.debug("Launching new VM on VimInstance: " + vimInstance.getName());
    log.debug("VDU is : " + vdu.toString());
    log.debug("VNFR is : " + vnfr.toString());
    log.debug("VNFC is : " + vnfComponent.toString());
    /** *) choose image *) ...? */
    String image = this.chooseImage(vdu.getVm_image(), vimInstance);

    log.debug("Finding Networks...");
    Set<VNFDConnectionPoint> networks = new HashSet<>();
    for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
      //      for (Network net : vimInstance.getNetworks())
      //        if (vnfdConnectionPoint.getVirtual_link_reference().equals(net.getName()))
      networks.add(vnfdConnectionPoint);
    }
    log.debug("Found Networks with ExtIds: " + networks);
    String flavorKey = null;
    if (vdu.getComputation_requirement() != null && !vdu.getComputation_requirement().isEmpty()) {
      flavorKey = vdu.getComputation_requirement();
    } else {
      flavorKey = vnfr.getDeployment_flavour_key();
    }
    String flavorExtId = getFlavorExtID(flavorKey, vimInstance);

    log.debug("Generating Hostname...");
    vdu.setHostname(vnfr.getName());
    String hostname = vdu.getHostname() + "-" + ((int) (Math.random() * 10000000));
    log.debug("Generated Hostname: " + hostname);

    userdata = userdata.replace("#Hostname=", "Hostname=" + hostname);

    log.debug("Using SecurityGroups: " + vimInstance.getSecurityGroups());

    log.debug(
        "Launching VM with params: "
            + hostname
            + " - "
            + image
            + " - "
            + flavorExtId
            + " - "
            + vimInstance.getKeyPair()
            + " - "
            + networks
            + " - "
            + vimInstance.getSecurityGroups());
    Server server;

    try {
      if (vimInstance == null) throw new NullPointerException("VimInstance is null");
      if (hostname == null) throw new NullPointerException("hostname is null");
      if (image == null) throw new NullPointerException("image is null");
      if (flavorExtId == null) throw new NullPointerException("flavorExtId is null");
      if (vimInstance.getKeyPair() == null) {
        log.debug("vimInstance.getKeyPair() is null");
        vimInstance.setKeyPair("");
      }
      if (networks == null || networks.isEmpty())
        throw new NullPointerException("networks is null");
      if (vimInstance.getSecurityGroups() == null)
        throw new NullPointerException("vimInstance.getSecurityGroups() is null");

      server =
          client.launchInstanceAndWait(
              vimInstance,
              hostname,
              image,
              flavorExtId,
              vimInstance.getKeyPair(),
              vnfComponent.getConnection_point(),
              vimInstance.getSecurityGroups(),
              userdata,
              floatingIps,
              new HashSet<>(keys));
      log.debug(
          "Launched VM with hostname "
              + hostname
              + " with ExtId "
              + server.getExtId()
              + " on VimInstance "
              + vimInstance.getName());
    } catch (VimDriverException e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not launched VM with hostname "
                + hostname
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not launched VM with hostname "
                + hostname
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      VNFCInstance vnfcInstance = null;
      VimDriverException vimDriverException = (VimDriverException) e.getCause();
      server = vimDriverException.getServer();
      if (server != null) {
        vnfcInstance =
            getVnfcInstanceFromServer(
                vimInstance, vnfComponent, hostname, server, vdu, floatingIps, vnfr);
      }
      throw new VimException(
          "Not launched VM with hostname "
              + hostname
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e,
          vdu,
          vnfcInstance);
    }

    log.debug("Creating VNFCInstance based on the VM launched previously -> VM: " + server);
    VNFCInstance vnfcInstance =
        getVnfcInstanceFromServer(
            vimInstance, vnfComponent, hostname, server, vdu, floatingIps, vnfr);

    log.info("Launched VNFCInstance: " + vnfcInstance + " on VimInstance " + vimInstance.getName());
    return new AsyncResult<>(vnfcInstance);
  }
}
