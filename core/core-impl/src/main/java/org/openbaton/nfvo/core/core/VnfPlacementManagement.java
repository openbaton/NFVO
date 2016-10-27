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

package org.openbaton.nfvo.core.core;

import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.nfvo.repositories.VimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lto on 10/03/16.
 */
@Service
@Scope
public class VnfPlacementManagement
    implements org.openbaton.nfvo.core.interfaces.VnfPlacementManagement {

  @Autowired private VimRepository vimInstanceRepository;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public VimInstance choseRandom(Collection<String> vimInstanceName) {
    if (!vimInstanceName.isEmpty()) {
      String name =
          vimInstanceName.toArray(new String[0])[
              ((int) (Math.random() * 1000)) % vimInstanceName.size()];
      VimInstance vimInstance = vimInstanceRepository.findFirstByName(name);
      log.info("Chosen VimInstance: " + vimInstance.getName());
      return vimInstance;
    } else {
      Iterable<VimInstance> vimInstances = vimInstanceRepository.findAll();
      List<Iterable<VimInstance>> iterableList = Collections.singletonList(vimInstances);
      return iterableList.toArray(new VimInstance[0])[
          ((int) (Math.random() * 1000)) % iterableList.size()];
    }
  }
}
