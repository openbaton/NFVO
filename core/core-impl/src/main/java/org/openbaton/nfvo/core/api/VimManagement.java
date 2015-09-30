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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope
public class VimManagement implements org.openbaton.nfvo.core.interfaces.VimManagement {

    @Autowired
    private VimRepository vimRepository;

    @Autowired
    private VimBroker vimBroker;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private NetworkRepository networkRepository;

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public VimInstance add(VimInstance vimInstance) throws VimException {
        this.refresh(vimInstance);
        log.trace("Persisting VimInstance: " + vimInstance);
        return vimRepository.save(vimInstance);
    }

    @Override
    public void delete(String id) {
        vimRepository.delete(vimRepository.findOne(id));
    }

    @Override
    public VimInstance update(VimInstance vimInstance, String id) throws VimException {
        vimInstance = vimRepository.save(vimInstance);
        refresh(vimInstance);
        return vimInstance;
    }

    @Override
    public Iterable<VimInstance> query() {
        return vimRepository.findAll();
    }

    @Override
    public VimInstance query(String id) {
        return vimRepository.findOne(id);
    }

    @Override
    public void refresh(VimInstance vimInstance) throws VimException {
        //Refreshing Images
        Set<NFVImage> images_refreshed = new HashSet<NFVImage>();
        Set<NFVImage> images_new = new HashSet<NFVImage>();
        Set<NFVImage> images_old = new HashSet<NFVImage>();
        images_refreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
        if (vimInstance.getImages() == null) {
            vimInstance.setImages(new HashSet<NFVImage>());
        }
        for (NFVImage image_new : images_refreshed) {
            boolean found = false;
            for (NFVImage nfvImage_nfvo : vimInstance.getImages()) {
                if (nfvImage_nfvo.getExtId().equals(image_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                images_new.add(image_new);
            }
        }
        for (NFVImage nfvImage_nfvo : vimInstance.getImages()) {
            boolean found = false;
            for (NFVImage image_new : images_refreshed) {
                if (nfvImage_nfvo.getExtId().equals(image_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                images_old.add(nfvImage_nfvo);
            }
        }
        vimInstance.getImages().addAll(images_new);
        vimInstance.getImages().removeAll(images_old);
        imageRepository.delete(images_old);
        //Refreshing Networks
        Set<Network> networks_refreshed = new HashSet<Network>();
        Set<Network> networks_new = new HashSet<Network>();
        Set<Network> networks_old = new HashSet<Network>();
        networks_refreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
        if (vimInstance.getNetworks() == null) {
            vimInstance.setNetworks(new HashSet<Network>());
        }
        for (Network network_new : networks_refreshed) {
            boolean found = false;
            for (Network network_nfvo : vimInstance.getNetworks()) {
                if (network_nfvo.getExtId().equals(network_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                networks_new.add(network_new);
            }
        }
        for (Network network_nfvo : vimInstance.getNetworks()) {
            boolean found = false;
            for (Network network_new : networks_refreshed) {
                if (network_nfvo.getExtId().equals(network_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                networks_old.add(network_nfvo);
            }
        }
        vimInstance.getNetworks().addAll(networks_new);
        vimInstance.getNetworks().removeAll(networks_old);
        networkRepository.delete(networks_old);
        //Refreshing Flavors
        Set<DeploymentFlavour> flavors_refreshed = new HashSet<DeploymentFlavour>();
        Set<DeploymentFlavour> flavors_new = new HashSet<DeploymentFlavour>();
        Set<DeploymentFlavour> flavors_old = new HashSet<DeploymentFlavour>();
        flavors_refreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
        if (vimInstance.getFlavours() == null) {
            vimInstance.setFlavours(new HashSet<DeploymentFlavour>());
        }
        for (DeploymentFlavour flavor_new : flavors_refreshed) {
            boolean found = false;
            for (DeploymentFlavour flavor_nfvo : vimInstance.getFlavours()) {
                if (flavor_nfvo.getExtId().equals(flavor_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                flavors_new.add(flavor_new);
            }
        }
        for (DeploymentFlavour flavor_nfvo : vimInstance.getFlavours()) {
            boolean found = false;
            for (DeploymentFlavour flavor_new : flavors_refreshed) {
                if (flavor_nfvo.getExtId().equals(flavor_new.getExtId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                flavors_old.add(flavor_nfvo);
            }
        }
        vimInstance.getFlavours().addAll(flavors_new);
        vimInstance.getFlavours().removeAll(flavors_old);
        vimRepository.save(vimInstance);
    }

    /**
     * Adds a new NFVImage to the VimInstance with id
     *
     * @param id    of VimInstance
     * @param image the new NFVImage
     * @return NFVImage
     */
    @Override
    public NFVImage addImage(String id, NFVImage image) throws VimException {
        image = vimRepository.addImage(id, image);
        refresh(vimRepository.findFirstById(id));
        return image;
    }


    public NFVImage queryImage(String idVim, String idImage) {
        vimRepository.exists(idVim);
        return imageRepository.findOne(idImage);
    }

    /**
     * Removes the NFVImage with idImage from VimInstance with idVim
     *
     * @param idVim
     * @param idImage
     */
    @Override
    public void deleteImage(String idVim, String idImage) throws VimException {
        vimRepository.deleteImage(idVim, idImage);
        refresh(vimRepository.findFirstById(idVim));

    }
}
