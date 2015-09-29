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
        Set<NFVImage> images = new HashSet<NFVImage>();
        images.addAll(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
        vimInstance.setImages(images);
        Set<Network> networks = new HashSet<Network>();
        networks.addAll(vimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
        vimInstance.setNetworks(networks);
        Set<DeploymentFlavour> flavours = new HashSet<DeploymentFlavour>();
        flavours.addAll(vimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
        vimInstance.setFlavours(flavours);
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
