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

package org.project.openbaton.nfvo.vim;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.project.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.plugin.utils.PluginBroker;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
@ComponentScan(basePackages = "org.project.openbaton.clients")
public class OpenstackVIM implements Vim {// TODO and so on...

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private PluginBroker<ClientInterfaces> pluginBroker;

    private ClientInterfaces openstackClient;

    @Autowired
    private VimBroker vimBroker;

    @PostConstruct
    private void init() {
        pluginBroker = new PluginBroker<>();
        try {
            openstackClient = pluginBroker.getPlugin("openstack-plugin");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        try {
            NFVImage addedImage = openstackClient.addImage(vimInstance, image, inputStream);
            log.debug("Image with id: " + image.getId() + " added successfully.");
            return addedImage;
        } catch (Exception e) {
            log.warn("Image with id: " + image.getId() + " not added successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not added successfully.");
        }
    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException{
        try {
            NFVImage updatedImage = openstackClient.updateImage(vimInstance, image);
            log.debug("Image with id: " + image.getId() + " updated successfully.");
            return updatedImage;
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not updated successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not updated successfully.");
        }
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException{
        try {
            openstackClient.copyImage(vimInstance, image, inputStream);
            log.debug("Image with id: " + image.getId() + " copied successfully.");
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not copied successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not copied successfully.");
        }
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {
        boolean isDeleted = false;
        isDeleted = openstackClient.deleteImage(vimInstance, image);
        if (isDeleted) {
            log.debug("Image with id: " + image.getId() + " deleted successfully.");
        } else {
            log.warn("Image with id: " + image.getId() + " not deleted successfully.");
            throw new VimException("Image with id: " + image.getId() + " not deleted successfully.");
        }
    }

    @Override
    public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        try {
            DeploymentFlavour flavor = openstackClient.addFlavor(vimInstance, deploymentFlavour);
            log.debug("Flavor with id: " + deploymentFlavour.getId() + " added successfully.");
            return flavor;
        } catch (Exception e) {
            log.error("Flavor with id: " + deploymentFlavour.getId() + " not added successfully.", e);
            throw new VimException("Image with id: " + deploymentFlavour.getId() + " not added successfully.");
        }
    }

    @Override
    public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        try {
            DeploymentFlavour flavor = openstackClient.updateFlavor(vimInstance, deploymentFlavour);
            log.debug("Flavor with id: " + deploymentFlavour.getId() + " updated successfully.");
            return flavor;
        } catch (Exception e) {
            log.error("Flavor with id: " + deploymentFlavour.getId() + " not updated successfully.", e);
            throw new VimException("Flavor with id: " + deploymentFlavour.getId() + " not updated successfully.");
        }
    }

    @Override
    public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {
        boolean isDeleted = false;
        isDeleted = openstackClient.deleteFlavor(vimInstance, deploymentFlavor.getExtId());
        if (isDeleted) {
            log.debug("Flavor with id: " + deploymentFlavor.getId() + " deleted successfully.");
        } else {
            log.warn("Flavor with id: " + deploymentFlavor.getId() + " not deleted successfully.");
            throw new VimException("Flavor with id: " + deploymentFlavor.getId() + " not deleted successfully.");
        }
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException {
        try {
            List<DeploymentFlavour> flavors = openstackClient.listFlavors(vimInstance);
            log.debug("Flavors listed successfully.");
            return flavors;
        } catch (Exception e) {
            log.error("Flavors not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        Network createdNetwork = null;
        try {
            createdNetwork = openstackClient.createNetwork(vimInstance, network);
            log.debug("Network with id: " + network.getId() + " created successfully.");
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not created successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not created successfully.");
        }
        Set<Subnet> createdSubnets = new HashSet<>();
        for (Subnet subnet : network.getSubnets()) {
            try {
                Subnet createdSubnet = openstackClient.createSubnet(vimInstance, createdNetwork, subnet);
                log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                createdSubnet.setNetworkId(createdNetwork.getId().toString());
                createdSubnets.add(createdSubnet);
            } catch (Exception e) {
                log.error("Subnet with id: " + subnet.getId() + " not created successfully.", e);
                throw new VimException("Subnet with id: " + subnet.getId() + " not created successfully.");
            }
        }
        createdNetwork.setSubnets(createdSubnets);
        return createdNetwork;
    }

    @Override
    public Network update(VimInstance vimInstance, Network network) throws VimException {
        Network updatedNetwork = null;
        try {
            updatedNetwork = openstackClient.updateNetwork(vimInstance, network);
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not updated successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not updated successfully.");
        }
        Set<Subnet> updatedSubnets = new HashSet<Subnet>();
        List<String> updatedSubnetExtIds = new ArrayList<String>();
        for (Subnet subnet : network.getSubnets()) {
            if (subnet.getExtId()!=null){
                try {
                    Subnet updatedSubnet = openstackClient.updateSubnet(vimInstance, updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " updated successfully.");
                    updatedSubnet.setNetworkId(updatedNetwork.getId().toString());
                    updatedSubnets.add(updatedSubnet);
                    updatedSubnetExtIds.add(updatedSubnet.getExtId());
                } catch (Exception e) {
                    log.error("Subnet with id: " + subnet.getId() + " not updated successfully.", e);
                    throw new VimException("Subnet with id: " + subnet.getId() + " not updated successfully.");
                }
            } else {
                try {
                    Subnet createdSubnet = openstackClient.createSubnet(vimInstance, updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                    createdSubnet.setNetworkId(updatedNetwork.getId().toString());
                    updatedSubnets.add(createdSubnet);
                    updatedSubnetExtIds.add(createdSubnet.getExtId());
                } catch (Exception e) {
                    log.error("Subnet with id: " + subnet.getId() + " not created successfully.", e);
                    throw new VimException("Subnet with id: " + subnet.getId() + " not created successfully.");
                }
            }
        }
        updatedNetwork.setSubnets(updatedSubnets);
        List<String> existingSubnetExtIds = null;
        existingSubnetExtIds = openstackClient.getSubnetsExtIds(vimInstance, updatedNetwork.getExtId());
        for (String existingSubnetExtId : existingSubnetExtIds) {
            if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
                openstackClient.deleteSubnet(vimInstance, existingSubnetExtId);
            }
        }
        return updatedNetwork;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException{
        boolean isDeleted = false;
        isDeleted = openstackClient.deleteNetwork(vimInstance, network.getExtId());
        if (isDeleted) {
            log.debug("Network with id: " + network.getId() + " deleted successfully.");
        } else {
            log.warn("Network with id: " + network.getId() + " not deleted successfully.");
            throw new VimException("Network with id: " + network.getId() + " not deleted successfully.");
        }
    }

    @Override
    public Network query(VimInstance vimInstance, String id) throws VimException {
        try {
            Network network = openstackClient.getNetworkById(vimInstance, id);
            log.debug("Network with id: " + network.getId() + " found.");
            return network;
        } catch (Exception e) {
            log.error("Network with id: " + id + "  not found.", e);
            throw new VimException("Network with id: " + id + "  not found.");
        }
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
        try {
            List<Network> networks = openstackClient.listNetworks(vimInstance);
            log.debug("Networks listed successfully.");
            return networks;
        } catch (Exception e) {
            log.error("Networks not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord vnfr, VNFComponent vnfComponent) throws VimDriverException, VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        log.debug("initialized VimInstance");
        /**
         *  *) choose image
         *  *) ...?
         */

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        Set<String> networks = new HashSet<String>();
        for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point())
            networks.add(vnfdConnectionPoint.getExtId());

        String flavorExtId = getFlavorExtID(vnfr.getDeployment_flavour_key(), vimInstance);
        String hostname = vdu.getHostname() /*+ "-" + vnfComponent.getId().substring(0,4)*/;

        log.trace("Params are: hostname:" + hostname + " - " + image + " - " + flavorExtId + " - " + vimInstance.getKeyPair() + " - " + networks + " - " + vimInstance.getSecurityGroups());
        Server server;
        server = openstackClient.launchInstanceAndWait(vimInstance, hostname, image, flavorExtId, vimInstance.getKeyPair(), networks, vimInstance.getSecurityGroups(), "#userdata");
        log.debug("launched instance with id " + server.getExtId());

//        vdu.setExtId(server.getExtId());
        VNFCInstance vnfcInstance = new VNFCInstance();
        vnfcInstance.setVc_id(server.getExtId());
        vnfcInstance.setVim_id(vdu.getVimInstance().getId());

        if (vnfcInstance.getConnection_point() == null)
            vnfcInstance.setConnection_point(new HashSet<VNFDConnectionPoint>());

        for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
            VNFDConnectionPoint connectionPoint_new = new VNFDConnectionPoint();
            connectionPoint_new.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
            connectionPoint_new.setExtId(connectionPoint.getExtId());
            connectionPoint_new.setName(connectionPoint.getName());
            connectionPoint_new.setType(connectionPoint.getType());

            vnfcInstance.getConnection_point().add(connectionPoint_new);
        }

        if (vdu.getVnfc_instance() == null)
            vdu.setVnfc_instance(new HashSet<VNFCInstance>());
        vdu.getVnfc_instance().add(vnfcInstance);

        for (String network : server.getIps().keySet()) {
            for (String ip : server.getIps().get(network)) {
                vnfr.getVnf_address().add(ip);
            }
        }
        return new AsyncResult<>(server.getExtId());
    }

    private String getFlavorExtID(String key, VimInstance vimInstance) throws VimException {
        for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()){
            if (deploymentFlavour.getFlavour_key().equals(key) || deploymentFlavour.getExtId().equals(key) || deploymentFlavour.getId().equals(key)){
                return deploymentFlavour.getExtId();
            }
        }
        throw new VimException("no key " + key + " found in any vim instance found");
    }

    @Override
    public List<Server> queryResources(VimInstance vimInstance) throws VimException {
        return openstackClient.listServer(vimInstance);
    }

    //     TODO choose the right image (DONE)
    private String chooseImage(Collection<String> vm_images, VimInstance vimInstance) throws VimException {
        if (vm_images != null && vm_images.size() > 0) {
            for (String image : vm_images){
                for (NFVImage nfvImage : vimInstance.getImages()){
                    if (image.equals(nfvImage.getName()) || image.equals(nfvImage.getExtId()))
                        return nfvImage.getExtId();
                }
            }
            throw new VimException("No available image matching: " + vm_images);
        }
        throw new VimException("List of VM images is empty or null");
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
        log.trace("Openstack client is: " + openstackClient);
        return openstackClient.listImages(vimInstance);
    }

    @Override
    public void update(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void scale(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void migrate(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void operate(VirtualDeploymentUnit vdu, String operation) {

    }

    @Override
    @Async
    public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance) throws VimException {
        log.debug("Removing VM with ext id: " + vnfcInstance.getVc_id());
        openstackClient.deleteServerByIdAndWait(vimInstance, vnfcInstance.getVc_id());
        return new AsyncResult<>(null);
    }

    @Override
    public void createReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void queryReservation() {

    }

    @Override
    public void updateReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void releaseReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public Quota getQuota(VimInstance vimInstance) throws VimException {
        Quota quota = null;
        quota = openstackClient.getQuota(vimInstance);
        return quota;
    }

}
