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

import java.util.List;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.nfvo.repositories.VimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 10/03/16. */
@Service
@Scope
public class VnfPlacementManagement
    implements org.openbaton.nfvo.core.interfaces.VnfPlacementManagement {

  @Autowired private VimRepository vimInstanceRepository;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public VimInstance choseRandom(List<String> vimInstanceName, String projectId) {
    if (!vimInstanceName.isEmpty()) {
      String name = vimInstanceName.get((int) (Math.random() * 1000) % vimInstanceName.size());
      VimInstance vimInstance = null;
      for (VimInstance vimInstance1 : vimInstanceRepository.findByProjectId(projectId))
        if (vimInstance1.getName().equals(name)) {
          vimInstance = vimInstance1;
          break;
        }
      log.info("Chosen VimInstance: " + vimInstance.getName());
      return vimInstance;
    } else {
      List<VimInstance> vimInstances = vimInstanceRepository.findByProjectId(projectId);

      return vimInstances.get((int) (Math.random() * 1000) % vimInstances.size());
    }
  }
}
