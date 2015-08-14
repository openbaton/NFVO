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
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.plugin.concretes.ClientInterfacePluginAgent;
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

    @Autowired
    private ClientInterfacePluginAgent openstackClient;

    @Autowired
    private VimBroker vimBroker;

    @PostConstruct
    private void init() {
//        ClientInterfaces client = vimBroker.getClient("openstack");
//        if (client==null) {
//            log.error("Plugin Openstack Vim Drivers not found. Have you installed it?");
//            NotFoundException notFoundException = new NotFoundException("Plugin Openstack Vim Drivers not found. Have you installed it?");
//            throw new RuntimeException(notFoundException);
//        }
//        log.debug("Found Client " + client);
//        this.openstackClient = client;

    }

    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        try {
            NFVImage addedImage = openstackClient.addImage(vimInstance.getType(), vimInstance,image, inputStream);
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
            NFVImage updatedImage = openstackClient.updateImage(vimInstance.getType(), vimInstance, image);
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
            openstackClient.copyImage(vimInstance.getType(), vimInstance, image, inputStream);
            log.debug("Image with id: " + image.getId() + " copied successfully.");
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not copied successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not copied successfully.");
        }
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {
        boolean isDeleted = false;
        try {
            isDeleted = openstackClient.deleteImage(vimInstance.getType(), vimInstance, image);
        } catch (NotFoundException e) {
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
            DeploymentFlavour flavor = openstackClient.addFlavor(vimInstance.getType(), vimInstance, deploymentFlavour);
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
            DeploymentFlavour flavor = openstackClient.updateFlavor(vimInstance.getType(), vimInstance, deploymentFlavour);
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
        try {
            isDeleted = openstackClient.deleteFlavor(vimInstance.getType(), vimInstance, deploymentFlavor.getExtId());
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
            List<DeploymentFlavour> flavors = openstackClient.listFlavors(vimInstance.getType(), vimInstance);
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
            createdNetwork = openstackClient.createNetwork(vimInstance.getType(), vimInstance, network);
            log.debug("Network with id: " + network.getId() + " created successfully.");
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not created successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not created successfully.");
        }
        Set<Subnet> createdSubnets = new HashSet<>();
        for (Subnet subnet : network.getSubnets()) {
            try {
                Subnet createdSubnet = openstackClient.createSubnet(vimInstance.getType(), vimInstance, createdNetwork, subnet);
                log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                createdSubnet.setNetworkId(createdNetwork.getId());
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
            updatedNetwork = openstackClient.updateNetwork(vimInstance.getType(), vimInstance, network);
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not updated successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not updated successfully.");
        }
        Set<Subnet> updatedSubnets = new HashSet<Subnet>();
        List<String> updatedSubnetExtIds = new ArrayList<String>();
        for (Subnet subnet : network.getSubnets()) {
            if (subnet.getExtId()!=null){
                try {
                    Subnet updatedSubnet = openstackClient.updateSubnet(vimInstance.getType(), vimInstance, updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " updated successfully.");
                    updatedSubnet.setNetworkId(updatedNetwork.getId());
                    updatedSubnets.add(updatedSubnet);
                    updatedSubnetExtIds.add(updatedSubnet.getExtId());
                } catch (Exception e) {
                    log.error("Subnet with id: " + subnet.getId() + " not updated successfully.", e);
                    throw new VimException("Subnet with id: " + subnet.getId() + " not updated successfully.");
                }
            } else {
                try {
                    Subnet createdSubnet = openstackClient.createSubnet(vimInstance.getType(), vimInstance, updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                    createdSubnet.setNetworkId(updatedNetwork.getId());
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
        try {
            existingSubnetExtIds = openstackClient.getSubnetsExtIds(vimInstance.getType(), vimInstance, updatedNetwork.getExtId());
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
        for (String existingSubnetExtId : existingSubnetExtIds) {
            if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
                try {
                    openstackClient.deleteSubnet(vimInstance.getType(), vimInstance, existingSubnetExtId);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                    throw new VimException(e);
                } catch (PluginInvokeException e) {
                    e.printStackTrace();
                    throw new VimException(e);
                }
            }
        }
        return updatedNetwork;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException{
        boolean isDeleted = false;
        try {
            isDeleted = openstackClient.deleteNetwork(vimInstance.getType(), vimInstance, network.getExtId());
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
            Network network = openstackClient.getNetworkById(vimInstance.getType(), vimInstance, id);
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
            List<Network> networks = openstackClient.listNetworks(vimInstance.getType(), vimInstance);
            log.debug("Networks listed successfully.");
            return networks;
        } catch (Exception e) {
            log.error("Networks not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord vnfr) throws VimDriverException, VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        log.debug("initialized VimInstance");
        /**
         *  *) Create hostname
         *  *) choose image
         *  *) ...?
         */
        vdu.setHostname(vnfr.getName() + "-" + vdu.getId().substring((vdu.getId().length() - 5), vdu.getId().length() - 1));

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        Set<String> networks = new HashSet<String>();
        for (VNFComponent vnfc: vdu.getVnfc()) {
            for (VNFDConnectionPoint vnfdConnectionPoint : vnfc.getConnection_point())
                networks.add(vnfdConnectionPoint.getExtId());
        }
        String flavorExtId = getFlavorExtID(vnfr.getDeployment_flavour_key(), vimInstance);
        log.trace("Params are: hostname:" + vdu.getHostname() + " - " + image + " - " + flavorExtId + " - " + vimInstance.getKeyPair() + " - " + networks + " - " + vimInstance.getSecurityGroups());
        Server server = null;
        try {
            server = openstackClient.launchInstanceAndWait(vimInstance.getType(), vimInstance, vdu.getHostname(), image, flavorExtId, vimInstance.getKeyPair(), networks, vimInstance.getSecurityGroups(), "#userdata");
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
        log.debug("launched instance with id " + server.getExtId());
        vdu.setExtId(server.getExtId());
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
        try {
            return openstackClient.listServer(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
        try {
            return openstackClient.listImages(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
    public Future<Void> release(VirtualDeploymentUnit vdu, VimInstance vimInstance) throws VimException {
        log.debug("Removing VM with ext id: " + vdu.getExtId());
        try {
            openstackClient.deleteServerByIdAndWait(vimInstance.getType(), vimInstance, vdu.getExtId());
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
        vdu.setExtId(null);
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
        try {
            quota = openstackClient.getQuota(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
        return quota;
    }

}
