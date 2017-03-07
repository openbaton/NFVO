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

package org.openbaton.vim.drivers.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.VimDriverException;

/** Created by lto on 12/05/15. */
public interface ClientInterfaces {

  /** This version must match the version of the plugin... */
  String interfaceVersion = "1.0";

  Server launchInstance(
      VimInstance vimInstance,
      String name,
      String image,
      String flavor,
      String keypair,
      Set<VNFDConnectionPoint> networks,
      Set<String> secGroup,
      String userData)
      throws VimDriverException, TimeoutException;

  List<NFVImage> listImages(VimInstance vimInstance) throws VimDriverException, TimeoutException;

  List<Server> listServer(VimInstance vimInstance) throws VimDriverException, TimeoutException;

  List<Network> listNetworks(VimInstance vimInstance) throws VimDriverException, TimeoutException;

  List<DeploymentFlavour> listFlavors(VimInstance vimInstance)
      throws VimDriverException, TimeoutException;

  Server launchInstanceAndWait(
      VimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
      Set<String> securityGroups,
      String s,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimDriverException, TimeoutException;

  Server launchInstanceAndWait(
      VimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
      Set<String> securityGroups,
      String s)
      throws VimDriverException, TimeoutException;

  void deleteServerByIdAndWait(VimInstance vimInstance, String id)
      throws VimDriverException, TimeoutException;

  Network createNetwork(VimInstance vimInstance, Network network)
      throws VimDriverException, TimeoutException;

  DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimDriverException, TimeoutException;

  NFVImage addImage(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimDriverException, TimeoutException;

  NFVImage addImage(VimInstance vimInstance, NFVImage image, String image_url)
      throws VimDriverException, TimeoutException;

  NFVImage updateImage(VimInstance vimInstance, NFVImage image)
      throws VimDriverException, TimeoutException;

  NFVImage copyImage(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimDriverException, TimeoutException;

  boolean deleteImage(VimInstance vimInstance, NFVImage image)
      throws VimDriverException, TimeoutException;

  DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimDriverException, TimeoutException;

  boolean deleteFlavor(VimInstance vimInstance, String extId)
      throws VimDriverException, TimeoutException;

  Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet)
      throws VimDriverException, TimeoutException;

  Network updateNetwork(VimInstance vimInstance, Network network)
      throws VimDriverException, TimeoutException;

  Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet)
      throws VimDriverException, TimeoutException;

  List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId)
      throws VimDriverException, TimeoutException;

  boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId)
      throws VimDriverException, TimeoutException;

  boolean deleteNetwork(VimInstance vimInstance, String extId)
      throws VimDriverException, TimeoutException;

  Network getNetworkById(VimInstance vimInstance, String id)
      throws VimDriverException, TimeoutException;

  Quota getQuota(VimInstance vimInstance) throws VimDriverException, TimeoutException;

  String getType(VimInstance vimInstance) throws VimDriverException, TimeoutException;
}
