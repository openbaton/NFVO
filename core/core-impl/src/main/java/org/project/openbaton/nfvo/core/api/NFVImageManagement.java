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

package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NFVImageManagement implements org.project.openbaton.nfvo.core.interfaces.NFVImageManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("imageRepository")
    private GenericRepository<NFVImage> imageRepository;

    @Override
    public NFVImage add(NFVImage NFVImage) {
        log.trace("Adding image " + NFVImage);
        log.debug("Adding image with name " + NFVImage.getName());
        //TODO maybe check whenever the image is available on the VimInstance
        return imageRepository.create(NFVImage);
    }

    @Override
    public void delete(String id) {
        log.debug("Removing image with id " + id);
        imageRepository.remove(imageRepository.find(id));
    }

    @Override
    public NFVImage update(NFVImage new_NFV_image, String id) {
        NFVImage old = imageRepository.find(id);
        old.setName(new_NFV_image.getName());
        old.setMinRam(new_NFV_image.getMinRam());
        old.setMinCPU(new_NFV_image.getMinCPU());
        old.setExtId(new_NFV_image.getExtId());
        old.setUpdated(new Date());
        old.setMinDiskSpace(new_NFV_image.getMinDiskSpace());
        return old;
    }

    @Override
    public List<NFVImage> query() {
        return imageRepository.findAll();
    }
    
    @Override
    public NFVImage query(String id){
        return imageRepository.find(id);
    }
    
    @Override
    public void copy() {
        throw new UnsupportedOperationException();
    }
}
