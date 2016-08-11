/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.vim_impl.vim;

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class TestVIM extends Vim {

  public TestVIM(String name, int port, String managementPort) throws PluginException {
    super("test", name, port, managementPort, null);
  }

  public TestVIM(String managementPort) throws PluginException {
    super("test", managementPort, null);
  }

  public TestVIM(int port, String managementPort) throws PluginException {
    super("test", managementPort, null);
  }

  public TestVIM() {}

  @Override
  public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor)
      throws VimException {}

  @Override
  public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance)
      throws VimException {
    try {
      return client.listFlavors(vimInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimException {
    try {
      return this.client.addImage(vimInstance, image, imageFile);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, String image_url)
      throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(VimInstance vimInstance, NFVImage image) throws VimException {}

  @Override
  public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
    try {
      return client.listImages(vimInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }

  @Override
  public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {}

  @Override
  @Async
  public Future<VNFCInstance> allocate(
      VimInstance vimInstance,
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent vnfComponent,
      String userdata,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimException {
    log.trace("Initializing " + vimInstance);
    try {
      HashSet<String> networks = new HashSet<>();
      networks.add("network_id");
      HashSet<String> securityGroups = new HashSet<>();
      securityGroups.add("secGroup_id");

      Server server =
          client.launchInstanceAndWait(
              vimInstance,
              vdu.getHostname(),
              vimInstance.getImages().iterator().next().getExtId(),
              "flavor",
              "keypair",
              networks,
              securityGroups,
              "#userdate");

      VNFCInstance vnfcInstance = new VNFCInstance();
      vnfcInstance.setVc_id(server.getExtId());
      vnfcInstance.setVim_id(vimInstance.getId());

      if (vnfcInstance.getConnection_point() == null)
        vnfcInstance.setConnection_point(new HashSet<VNFDConnectionPoint>());

      for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
        VNFDConnectionPoint connectionPoint_new = new VNFDConnectionPoint();
        connectionPoint_new.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
        connectionPoint_new.setType(connectionPoint.getType());
        vnfcInstance.getConnection_point().add(connectionPoint_new);
      }

      if (vdu.getVnfc_instance() == null) vdu.setVnfc_instance(new HashSet<VNFCInstance>());

      vnfcInstance.setVnfComponent(vnfComponent);
      vnfcInstance.setFloatingIps(new HashSet<Ip>());
      vnfcInstance.setIps(new HashSet<Ip>());
      vdu.getVnfc_instance().add(vnfcInstance);

      for (String network : server.getIps().keySet()) {
        for (String ip : server.getIps().get(network)) {
          virtualNetworkFunctionRecord.getVnf_address().add(ip);
        }
      }
      String id = server.getId();
      log.debug("launched instance with id " + id);
      return new AsyncResult<>(vnfcInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }

  @Override
  public List<Server> queryResources(VimInstance vimInstance) throws VimException {

    try {
      return client.listServer(vimInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
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
  public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance) {
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
  public Network add(VimInstance vimInstance, Network network) throws VimException {
    return network;
  }

  @Override
  public void delete(VimInstance vimInstance, Network network) throws VimException {}

  @Override
  public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
    try {
      return this.client.listNetworks(vimInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }

  @Override
  public Network query(VimInstance vimInstance, String extId) throws VimException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Quota getQuota(VimInstance vimInstance) throws VimException {
    try {
      return this.client.getQuota(vimInstance);
    } catch (Exception e) {
      e.printStackTrace();
      throw new VimException(e);
    }
  }
}
