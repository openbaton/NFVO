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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NFVImageManagement implements org.openbaton.nfvo.core.interfaces.NFVImageManagement {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ImageRepository imageRepository;

  @Override
  public NFVImage add(NFVImage NFVImage) {
    log.trace("Adding image " + NFVImage);
    log.debug("Adding image with name " + NFVImage.getName());
    return imageRepository.save(NFVImage);
  }

  @Override
  public void delete(String id) {
    log.debug("Removing image with id " + id);
    imageRepository.delete(id);
  }

  @Override
  public NFVImage update(NFVImage nfvImage, String id) {
    nfvImage = imageRepository.save(nfvImage);
    nfvImage.setUpdated(new Date());
    return nfvImage;
  }

  @Override
  public Iterable<NFVImage> query() {
    return imageRepository.findAll();
  }

  @Override
  public NFVImage query(String id) {
    return imageRepository.findOne(id);
  }

  @Override
  public void copy() {
    throw new UnsupportedOperationException();
  }
}
