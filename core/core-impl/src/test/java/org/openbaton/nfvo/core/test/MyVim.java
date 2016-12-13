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

package org.openbaton.nfvo.core.test;

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.springframework.stereotype.Service;

/** Created by lto on 26/11/15. */
@Service
public class MyVim extends Vim {

  public MyVim() {
    super();
    this.setClient(mock(VimDriverCaller.class));
  }

  @Override
  public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    return null;
  }

  @Override
  public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor)
      throws VimException {}

  @Override
  public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    return null;
  }

  @Override
  public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance)
      throws VimException {
    return null;
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimException {
    return null;
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, String image_url)
      throws VimException {
    return null;
  }

  @Override
  public void delete(VimInstance vimInstance, NFVImage image) throws VimException {}

  @Override
  public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
    return null;
  }

  @Override
  public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
    return null;
  }

  @Override
  public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {}

  @Override
  public Network add(VimInstance vimInstance, Network network) throws VimException {
    return network;
  }

  @Override
  public void delete(VimInstance vimInstance, Network network) throws VimException {}

  @Override
  public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
    return updatingNetwork;
  }

  @Override
  public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
    return null;
  }

  @Override
  public Network query(VimInstance vimInstance, String extId) throws VimException {
    return null;
  }

  @Override
  public Future<VNFCInstance> allocate(
      VimInstance vimInstance,
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent vnfComponent,
      String userdata,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimException {
    return null;
  }

  @Override
  public List<Server> queryResources(VimInstance vimInstance) throws VimException {
    return null;
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
  public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance)
      throws VimException {
    return null;
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
  public Quota getQuota(VimInstance vimInstance) {
    return null;
  }
}
