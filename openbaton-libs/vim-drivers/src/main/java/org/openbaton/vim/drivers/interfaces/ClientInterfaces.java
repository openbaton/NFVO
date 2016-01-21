/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.vim.drivers.interfaces;

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.exceptions.VimDriverException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces extends Remote {

    /**
     * This version must match the version of the plugin...
     */
    String interfaceVersion = "1.0";

    Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData) throws RemoteException, VimDriverException;
//    void init(VimInstance vimInstance);

    List<NFVImage> listImages(VimInstance vimInstance) throws RemoteException, VimDriverException;

    List<Server> listServer(VimInstance vimInstance) throws RemoteException, VimDriverException;

    List<Network> listNetworks(VimInstance vimInstance) throws RemoteException, VimDriverException;

    List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws RemoteException, VimDriverException;

    Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s, Map<String, String> floatingIps) throws RemoteException, VimDriverException;

    Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws RemoteException, VimDriverException;

    void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws RemoteException, VimDriverException;

    Network createNetwork(VimInstance vimInstance, Network network) throws RemoteException, VimDriverException;

    DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException, VimDriverException;

    NFVImage addImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws RemoteException, VimDriverException;

    NFVImage addImage(VimInstance vimInstance, NFVImage image, String image_url) throws RemoteException, VimDriverException;

    NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws RemoteException, VimDriverException;

    NFVImage copyImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws RemoteException, VimDriverException;

    boolean deleteImage(VimInstance vimInstance, NFVImage image) throws RemoteException, VimDriverException;

    DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException, VimDriverException;

    boolean deleteFlavor(VimInstance vimInstance, String extId) throws RemoteException, VimDriverException;

    Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet) throws RemoteException, VimDriverException;

    Network updateNetwork(VimInstance vimInstance, Network network) throws RemoteException, VimDriverException;

    Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet) throws RemoteException, VimDriverException;

    List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws RemoteException, VimDriverException;

    boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws RemoteException, VimDriverException;

    boolean deleteNetwork(VimInstance vimInstance, String extId) throws RemoteException, VimDriverException;

    Network getNetworkById(VimInstance vimInstance, String id) throws RemoteException, VimDriverException;

    Quota getQuota(VimInstance vimInstance) throws RemoteException, VimDriverException;

    String getType(VimInstance vimInstance) throws RemoteException, VimDriverException;
}
