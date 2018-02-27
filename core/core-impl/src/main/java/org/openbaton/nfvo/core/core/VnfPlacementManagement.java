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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.NotFoundException;
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
  public BaseVimInstance choseRandom(Set<String> vimInstanceName, String projectId)
      throws NotFoundException {
    if (!vimInstanceName.isEmpty()) {
      String name =
          vimInstanceName.toArray(new String[0])[
              (int) (Math.random() * 1000) % vimInstanceName.size()];
      Optional<BaseVimInstance> instanceOptional =
          vimInstanceRepository
              .findByProjectId(projectId)
              .stream()
              .filter(v -> v.getName().equals(name))
              .findAny();
      log.info("Chosen VimInstance: " + instanceOptional);
      if (instanceOptional.isPresent()) return instanceOptional.get();
      else throw new NotFoundException("No Vim instance found for name " + name);
    } else {
      List<BaseVimInstance> vimInstances = vimInstanceRepository.findByProjectId(projectId);

      return vimInstances.get((int) (Math.random() * 1000) % vimInstances.size());
    }
  }

  @Override
  public List<String> chose(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      Set<String> vimTypes,
      String projectId)
      throws NotFoundException {
    List<BaseVimInstance> vimInstances = vimInstanceRepository.findByProjectId(projectId);
    List<String> vimInstancesChosen = new ArrayList<>();
    if (vimTypes != null && !vimTypes.isEmpty()) {
      for (BaseVimInstance vimInstance : vimInstances) {
        if (vimTypes.contains(vimInstance.getType())) {
          vimInstancesChosen.add(vimInstance.getName());
        }
      }
    } else {
      for (BaseVimInstance vimInstance : vimInstances) {
        vimInstancesChosen.add(vimInstance.getName());
        return vimInstancesChosen;
      }
    }
    if (vimInstancesChosen.isEmpty()) {
      if (vimTypes != null && !vimTypes.isEmpty()) {
        throw new NotFoundException(
            "Not found any VIM which supports the following types: " + vimTypes);
      } else {
        throw new NotFoundException(
            "Not found any VIM where the VNFD "
                + virtualNetworkFunctionDescriptor.getId()
                + " can be deployed");
      }
    } else {
      return vimInstancesChosen;
    }
  }
}
