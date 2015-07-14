package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.common.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 30/06/15.
 */
@Service
@Scope
public class DependencyManagement implements org.project.openbaton.nfvo.core.interfaces.DependencyManagement {

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void provisionDependencies(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException, JMSException, NamingException {
        List<NetworkServiceRecord> nsrs = nsrRepository.findAll();
        for (NetworkServiceRecord nsr: nsrs){
            log.debug("Found NSR");
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                /**
                 * looking for the VirtualNetworkFunctionRecord
                 */
                if (vnfr.getId().equals(virtualNetworkFunctionRecord.getId())){
                    Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();
                    log.debug("Found VNF; there are " + vnfRecordDependencies.size() + " dependencies");
                    for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies){
                        /**
                         * invoke on target the modify with the source information
                         */
                        log.trace(vnfRecordDependency.getSource().getId() + " == " + virtualNetworkFunctionRecord.getId());
                        if (vnfRecordDependency.getSource().getId().equals(virtualNetworkFunctionRecord.getId())){
                            CoreMessage coreMessage = new CoreMessage();
                            coreMessage.setAction(Action.MODIFY);
                            coreMessage.setPayload(virtualNetworkFunctionRecord);
                            vnfmManager.modify(vnfRecordDependency.getTarget(), coreMessage);
                        }
                    }
                    return;
                }
            }
        }
        throw new NotFoundException("There is a big error. No NetworkServiceRecord found containing VirtualNetworkFunctionRecord with id " + virtualNetworkFunctionRecord.getId());
    }
}
