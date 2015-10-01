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
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.vim.drivers.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class OpenstackVIM extends Vim {// TODO and so on...


    public OpenstackVIM(String name, int port) {
        super("openstack",name, port);
    }
    public OpenstackVIM() {
        super("openstack");
    }
    public OpenstackVIM(int port) {
        super("openstack",port);
    }
    
    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile ) throws VimException {
        try {
            NFVImage addedImage = client.addImage(vimInstance, image, imageFile);
            log.debug("Image with name: " + image.getName() + " added successfully.");
            return addedImage;
        } catch (Exception e) {
            log.warn("Image with name: " + image.getName() + " not added successfully.", e);
            throw new VimException("Image with name: " + image.getName() + " not added successfully.");
        }
    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException{
        try {
            NFVImage updatedImage = client.updateImage(vimInstance, image);
            log.debug("Image with id: " + image.getId() + " updated successfully.");
            return updatedImage;
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not updated successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not updated successfully.");
        }
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException{
        try {
            client.copyImage(vimInstance, image, imageFile);
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
            isDeleted = client.deleteImage(vimInstance, image);
        } catch (RemoteException e) {
            e.printStackTrace();
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
            DeploymentFlavour flavor = client.addFlavor(vimInstance, deploymentFlavour);
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
            DeploymentFlavour flavor = client.updateFlavor(vimInstance, deploymentFlavour);
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
            isDeleted = client.deleteFlavor(vimInstance, deploymentFlavor.getExtId());
        } catch (RemoteException e) {
            e.printStackTrace();
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
            List<DeploymentFlavour> flavors = client.listFlavors(vimInstance);
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
            createdNetwork = client.createNetwork(vimInstance, network);
            log.debug("Network with name: " + network.getName() + " created successfully.");
        } catch (Exception e) {
            log.error("Network with name: " + network.getName() + " not created successfully.", e);
            throw new VimException("Network with name: " + network.getName() + " not created successfully.");
        }
        Set<Subnet> createdSubnets = new HashSet<>();
        for (Subnet subnet : network.getSubnets()) {
            try {
                Subnet createdSubnet = client.createSubnet(vimInstance, createdNetwork, subnet);
                log.debug("Subnet with name: " + subnet.getName() + " created successfully.");
                createdSubnet.setNetworkId(createdNetwork.getId());
                createdSubnets.add(createdSubnet);
            } catch (Exception e) {
                log.error("Subnet with name: " + subnet.getName() + " not created successfully.", e);
                throw new VimException("Subnet with name: " + subnet.getName() + " not created successfully.");
            }
        }
        createdNetwork.setSubnets(createdSubnets);
        return createdNetwork;
    }

    @Override
    public Network update(VimInstance vimInstance, Network network) throws VimException {
        Network updatedNetwork = null;
        try {
            updatedNetwork = client.updateNetwork(vimInstance, network);
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not updated successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not updated successfully.");
        }
        Set<Subnet> updatedSubnets = new HashSet<Subnet>();
        List<String> updatedSubnetExtIds = new ArrayList<String>();
        for (Subnet subnet : network.getSubnets()) {
            if (subnet.getExtId()!=null){
                try {
                    Subnet updatedSubnet = client.updateSubnet(vimInstance, updatedNetwork, subnet);
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
                    Subnet createdSubnet = client.createSubnet(vimInstance, updatedNetwork, subnet);
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
        try {
            existingSubnetExtIds = client.getSubnetsExtIds(vimInstance, updatedNetwork.getExtId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for (String existingSubnetExtId : existingSubnetExtIds) {
            if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
                try {
                    client.deleteSubnet(vimInstance, existingSubnetExtId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return updatedNetwork;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException{
        boolean isDeleted = false;
        try {
            isDeleted = client.deleteNetwork(vimInstance, network.getExtId());
        } catch (RemoteException e) {
            e.printStackTrace();
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
            Network network = client.getNetworkById(vimInstance, id);
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
            List<Network> networks = client.listNetworks(vimInstance);
            log.debug("Networks listed successfully.");
            return networks;
        } catch (Exception e) {
            log.error("Networks not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    @Async
    public Future<VNFCInstance> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord vnfr, VNFComponent vnfComponent, String userdata, boolean floatingIp) throws VimDriverException, VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.debug("Initializing " + vimInstance.toString());
        log.debug("initialized VimInstance");
        log.debug("VDU is : " + vdu.toString());
        log.debug("VNFR is : " + vnfr.toString());
        log.debug("VNFC is : " + vnfComponent.toString());
        /**
         *  *) choose image
         *  *) ...?
         */

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        Set<String> networks = new HashSet<String>();
        for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
            for (InternalVirtualLink internalVirtualLink : vnfr.getVirtual_link()) {

                log.debug("InternalVirtualLink is: " + internalVirtualLink);

                if (vnfdConnectionPoint. getVirtual_link_reference().equals(internalVirtualLink.getName())) {
                    networks.add(internalVirtualLink.getExtId());
                }
            }
        }

        String flavorExtId = getFlavorExtID(vnfr.getDeployment_flavour_key(), vimInstance);
        vdu.setHostname(vnfr.getName());
        String hostname = vdu.getHostname() + "-" + ((int)(Math.random()*1000));

        log.debug("Params are: hostname:" + hostname + " - " + image + " - " + flavorExtId + " - " + vimInstance.getKeyPair() + " - " + networks + " - " + vimInstance.getSecurityGroups());
        Server server;

        try {
            if(vimInstance==null)
                throw new NullPointerException("VimInstance is null");
            if(hostname==null)
                throw new NullPointerException("hostname is null");
            if(image==null)
                throw new NullPointerException("image is null");
            if(flavorExtId==null)
                throw new NullPointerException("flavorExtId is null");
            if(vimInstance.getKeyPair()==null)
                throw new NullPointerException("vimInstance.getKeyPair() is null");
            if(networks==null)
                throw new NullPointerException("networks is null");
            if(vimInstance.getSecurityGroups()==null)
                throw new NullPointerException("vimInstance.getSecurityGroups() is null");

            server = client.launchInstanceAndWait(vimInstance, hostname, image, flavorExtId, vimInstance.getKeyPair(), networks, vimInstance.getSecurityGroups(), userdata, floatingIp);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
        log.debug("launched instance with id " + server.getExtId());

        VNFCInstance vnfcInstance = new VNFCInstance();
        vnfcInstance.setHostname(hostname);
        vnfcInstance.setVc_id(server.getExtId());
        vnfcInstance.setVim_id(vdu.getVimInstance().getId());
        vnfcInstance.setVnfc_reference(vnfComponent.getId());

        if (vnfcInstance.getConnection_point() == null)
            vnfcInstance.setConnection_point(new HashSet<VNFDConnectionPoint>());

        for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
            VNFDConnectionPoint connectionPoint_vnfci = new VNFDConnectionPoint();
            connectionPoint_vnfci.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
            connectionPoint_vnfci.setType(connectionPoint.getType());
            vnfcInstance.getConnection_point().add(connectionPoint_vnfci);
        }

        vnfcInstance.setIps(new HashSet<Ip>());

        if (floatingIp){
            vnfcInstance.setFloatingIps(server.getFloatingIp());
        }

        if (vdu.getVnfc_instance() == null)
            vdu.setVnfc_instance(new HashSet<VNFCInstance>());

        for (Map.Entry<String,List<String>> network : server.getIps().entrySet()) {
            Ip ip = new Ip();
            ip.setNetName(network.getKey());
            ip.setIp(network.getValue().iterator().next());
            vnfcInstance.getIps().add(ip);
            for (String ip1 : server.getIps().get(network.getKey())) {
                vnfr.getVnf_address().add(ip1);
            }
        }
        vdu.getVnfc_instance().add(vnfcInstance);
        return new AsyncResult<>(vnfcInstance);
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
            return client.listServer(vimInstance);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
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
        log.trace("Openstack client is: " + client);
        try {
            return client.listImages(vimInstance);
        } catch (RemoteException e) {
            e.printStackTrace();
            log.error("Is the plugin still up and running ?");
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
    public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance) throws VimException {
        log.debug("Removing VM with ext id: " + vnfcInstance.getVc_id());
        try {
            client.deleteServerByIdAndWait(vimInstance, vnfcInstance.getVc_id());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
            quota = client.getQuota(vimInstance);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return quota;
    }

}
