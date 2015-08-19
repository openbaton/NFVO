package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lto on 19/08/15.
 */
@Service
@Scope
public class DependencyQueuer implements org.project.openbaton.nfvo.core.interfaces.DependencyQueuer {
    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, BlockingDeque<VNFRecordDependency>> queues;

    @Override
    public void waitForVNFR(String vnfrSourceId, VNFRecordDependency dependency) throws InterruptedException, NotFoundException {
        if (queues.get(vnfrSourceId) == null){
            queues.put(vnfrSourceId, new LinkedBlockingDeque<VNFRecordDependency>());
        }
        log.debug("Adding to the queue: " + vnfrSourceId + ", dependency: " + dependency);
        queues.get(vnfrSourceId).add(dependency);
    }

    @Override
    public void releaseVNFR(String vnfrId) throws NotFoundException {
        if (queues.get(vnfrId) != null){
            for (VNFRecordDependency dependency : queues.get(vnfrId)){
                log.debug("releasing from the queue: " + vnfrId + ", dependency: " + dependency);
                CoreMessage coreMessage = new CoreMessage();
                coreMessage.setAction(Action.MODIFY);
                coreMessage.setVirtualNetworkFunctionRecord(dependency.getTarget());
                coreMessage.setDependency(dependency);
                vnfmManager.modify(dependency.getTarget(), coreMessage);
            }
        }
    }
}
