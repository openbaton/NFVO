package org.openbaton.nfvo.core.core;

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
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

/**
 * Created by lto on 19/08/15.
 */
@Service
@Scope
public class DependencyQueuer implements org.openbaton.nfvo.core.interfaces.DependencyQueuer {

    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;

    @Autowired
    private VNFRRepository vnfrRepository;

    @Autowired
    private VNFRDependencyRepository vnfrDependencyRepository;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Set<String>> queues;

    @PostConstruct
    private void init() {
        this.queues = new HashMap<>();
    }

    @Override
    public synchronized void waitForVNFR(String targetDependencyId, Set<String> sourceIds) throws InterruptedException, NotFoundException {
        if (queues.get(targetDependencyId) == null) {
            queues.put(targetDependencyId, new HashSet<String>());
        }
        log.debug("Adding to the queue: " + sourceIds + ", dependency: " + targetDependencyId);
        for (String name : sourceIds)
            queues.get(targetDependencyId).add(name);
    }

    @Override
    public synchronized void releaseVNFR(String vnfrSourceName, NetworkServiceRecord nsrFather) throws NotFoundException {
        List<String> dependencyIdToBeRemoved = new ArrayList<>();
        log.debug("Doing release for VNFR id: " + vnfrSourceName);
        for (Map.Entry<String, Set<String>> entry : queues.entrySet()) {
            String dependencyId = entry.getKey();
            Set<String> sourceList = entry.getValue();
            log.debug("Dependency " + dependencyId + " contains " + sourceList.size() + " dependencies: " + sourceList);
            if (sourceList.contains(vnfrSourceName + nsrFather.getId())) {
                sourceList.remove(vnfrSourceName + nsrFather.getId());
                if (sourceList.size() == 0) {

                    VNFRecordDependency vnfRecordDependency = vnfrDependencyRepository.findOne(dependencyId);

                    //get the vnfr target by its name
                    VirtualNetworkFunctionRecord target = null;
                    for (VirtualNetworkFunctionRecord vnfr : nsrFather.getVnfr())
                        if (vnfr.getName().equals(vnfRecordDependency.getTarget()))
                            target = vnfrRepository.findOne(vnfr.getId());
                    log.debug("Target version is: " + target.getHb_version());

                    for (LifecycleEvent lifecycleEvent : target.getLifecycle_event()) {
                        if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal()) {
                            log.debug("THE EVENT CONFIGURE HAS THESE SCRIPTS: " + lifecycleEvent.getLifecycle_events());
                            LinkedHashSet<String> strings = new LinkedHashSet<>();
                            strings.addAll(lifecycleEvent.getLifecycle_events());
                            lifecycleEvent.setLifecycle_events(Arrays.asList(strings.toArray(new String[1])));
                            log.debug("NOW THE EVENT CONFIGURE HAS THESE SCRIPTS: " + lifecycleEvent.getLifecycle_events());
                        }
                    }
                    log.debug("SENDING MODIFY");
                    OrVnfmGenericMessage orVnfmGenericMessage = new OrVnfmGenericMessage(target, Action.MODIFY);
                    orVnfmGenericMessage.setVnfrd(vnfRecordDependency);
                    vnfmManager.modify(target, orVnfmGenericMessage);
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
