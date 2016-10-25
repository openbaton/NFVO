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

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
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
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class AmazonVIM extends GenericVIM {

  public AmazonVIM(String name, int port, String managementPort) throws PluginException {
    super("amazon", name, port, managementPort, null);
  }

  public AmazonVIM(String managementPort) throws PluginException {
    super("amazon", managementPort, null);
  }

  public AmazonVIM(int port, String managementPort) throws PluginException {
    super("amazon", managementPort, null);
  }

  public AmazonVIM() {}

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimException {
    throw new UnsupportedOperationException();
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
  public List<NFVImage> queryImages(VimInstance vimInstance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {}

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
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Server> queryResources(VimInstance vimInstance) {
    throw new UnsupportedOperationException();
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
  public Quota getQuota(VimInstance vimInstance) {
    return null;
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
  public Network add(VimInstance vimInstance, Network network) throws VimException {
    return null;
  }

  @Override
  public void delete(VimInstance vimInstance, Network network) throws VimException {}

  @Override
  public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
    return null;
  }

  @Override
  public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
    return null;
  }

  @Override
  public Network query(VimInstance vimInstance, String extId) throws VimException {
    return null;
  }
}
