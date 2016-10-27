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

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.VNFRDependencyRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by lto on 19/08/15.
 */
@Service
@Scope
public class DependencyQueuer implements org.openbaton.nfvo.core.interfaces.DependencyQueuer {

  @Autowired
  @Qualifier("vnfmManager")
  private VnfmManager vnfmManager;

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private VNFRDependencyRepository vnfrDependencyRepository;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private Map<String, Set<String>> queues;

  @PostConstruct
  private void init() {
    this.queues = new HashMap<>();
  }

  @Override
  public synchronized void waitForVNFR(String targetDependencyId, Set<String> sourceIds) {
    if (queues.get(targetDependencyId) == null) {
      queues.put(targetDependencyId, new HashSet<String>());
    }
    log.debug("Adding to the queue: " + sourceIds + ", dependency: " + targetDependencyId);
    for (String name : sourceIds) queues.get(targetDependencyId).add(name);
  }

  /**
   * Check all dependencies that are waiting in in the map queues for the source vnfr to get
   * instantiated. If the vnfr that got ready was the last source in a waiting dependency send a
   * modify message to the target vnfr.
   *
   * @param vnfrSourceName
   * @param nsrFather
   * @throws NotFoundException
   */
  @Override
  public synchronized void releaseVNFR(String vnfrSourceName, NetworkServiceRecord nsrFather)
      throws NotFoundException {
    List<String> dependencyIdToBeRemoved = new ArrayList<>();
    log.debug("Doing release for VNFR id: " + vnfrSourceName);
    for (Entry<String, Set<String>> entry : queues.entrySet()) {
      String dependencyId = entry.getKey();
      Set<String> sourceList = entry.getValue();
      log.debug(
          "Dependency "
              + dependencyId
              + " contains "
              + sourceList.size()
              + " dependencies: "
              + sourceList);
      if (sourceList.contains(vnfrSourceName + nsrFather.getId())) {
        sourceList.remove(vnfrSourceName + nsrFather.getId());
        if (sourceList.isEmpty()) {

          VNFRecordDependency vnfRecordDependency =
              vnfrDependencyRepository.findFirstById(dependencyId);

          log.debug("Found VNFRecordDependency: " + vnfRecordDependency);

          //get the vnfr target by its name
          VirtualNetworkFunctionRecord target = null;
          for (VirtualNetworkFunctionRecord vnfr : nsrFather.getVnfr())
            if (vnfr.getName().equals(vnfRecordDependency.getTarget()))
              target = vnfrRepository.findFirstById(vnfr.getId());
          log.info("Found target of relation: " + target.getName());

          //                    for (LifecycleEvent lifecycleEvent : target.getLifecycle_event()) {
          //                        if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal()) {
          //                            LinkedHashSet<String> strings = new LinkedHashSet<>();
          //                            strings.addAll(lifecycleEvent.getLifecycle_events());
          //                            lifecycleEvent.setLifecycle_events(Arrays.asList(strings.toArray(new String[1])));
          //                        }
          //                    }
          log.debug("Sending MODIFY to " + target.getName());
          OrVnfmGenericMessage orVnfmGenericMessage =
              new OrVnfmGenericMessage(target, Action.MODIFY);
          orVnfmGenericMessage.setVnfrd(vnfRecordDependency);
          vnfmManager.sendMessageToVNFR(target, orVnfmGenericMessage);
          dependencyIdToBeRemoved.add(dependencyId);
        }
      }
    }

    for (String depIdToRem : dependencyIdToBeRemoved) {
      for (String depId : queues.keySet()) {
        if (depIdToRem.equals(depId)) {
          queues.remove(depId);
          break;
        }
      }
    }
  }
}
