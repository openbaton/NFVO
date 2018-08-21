/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.vim.drivers.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.VimDriverException;

/** Created by lto on 12/05/15. */
public interface ClientInterfaces {

  /** This version must match the version of the plugin... */
  String interfaceVersion = "1.0";

  Server launchInstance(
      BaseVimInstance vimInstance,
      String name,
      String image,
      String flavor,
      String keypair,
      Set<VNFDConnectionPoint> networks,
      Set<String> secGroup,
      String userData)
      throws VimDriverException;

  List<Server> listServer(BaseVimInstance vimInstance) throws VimDriverException;

  Server rebuildServer(BaseVimInstance vimInstance, String serverId, String imageId)
      throws VimDriverException;

  List<BaseNetwork> listNetworks(BaseVimInstance vimInstance) throws VimDriverException;

  List<BaseNfvImage> listImages(BaseVimInstance vimInstance) throws VimDriverException;

  List<DeploymentFlavour> listFlavors(BaseVimInstance vimInstance) throws VimDriverException;

  BaseVimInstance refresh(BaseVimInstance vimInstance) throws VimDriverException;

  Server launchInstanceAndWait(
      BaseVimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
      Set<String> securityGroups,
      String s,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimDriverException;

  Server launchInstanceAndWait(
      BaseVimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
      Set<String> securityGroups,
      String s)
      throws VimDriverException;

  void deleteServerByIdAndWait(BaseVimInstance vimInstance, String id) throws VimDriverException;

  BaseNetwork createNetwork(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimDriverException;

  DeploymentFlavour addFlavor(BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimDriverException;

  BaseNfvImage addImage(BaseVimInstance vimInstance, BaseNfvImage image, byte[] imageFile)
      throws VimDriverException;

  BaseNfvImage addImage(BaseVimInstance vimInstance, BaseNfvImage image, String image_url)
      throws VimDriverException;

  BaseNfvImage updateImage(BaseVimInstance vimInstance, BaseNfvImage image)
      throws VimDriverException;

  BaseNfvImage copyImage(BaseVimInstance vimInstance, BaseNfvImage image, byte[] imageFile)
      throws VimDriverException;

  boolean deleteImage(BaseVimInstance vimInstance, BaseNfvImage image) throws VimDriverException;

  DeploymentFlavour updateFlavor(BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimDriverException;

  boolean deleteFlavor(BaseVimInstance vimInstance, String extId) throws VimDriverException;

  Subnet createSubnet(BaseVimInstance vimInstance, BaseNetwork createdNetwork, Subnet subnet)
      throws VimDriverException;

  BaseNetwork updateNetwork(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimDriverException;

  Subnet updateSubnet(BaseVimInstance vimInstance, BaseNetwork updatedNetwork, Subnet subnet)
      throws VimDriverException;

  List<String> getSubnetsExtIds(BaseVimInstance vimInstance, String network_extId)
      throws VimDriverException;

  boolean deleteSubnet(BaseVimInstance vimInstance, String existingSubnetExtId)
      throws VimDriverException;

  boolean deleteNetwork(BaseVimInstance vimInstance, String extId) throws VimDriverException;

  BaseNetwork getNetworkById(BaseVimInstance vimInstance, String id) throws VimDriverException;

  Quota getQuota(BaseVimInstance vimInstance) throws VimDriverException;

  String getType(BaseVimInstance vimInstance) throws VimDriverException;
}
