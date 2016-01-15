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

package org.openbaton.nfvo.vnfm_reg.tasks;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.DependencyQueuer;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
public class InstantiateTask extends AbstractTask {

    @Autowired
    private DependencyManagement dependencyManagement;

    @Value("${nfvo.start.ordered:")
    private String ordered;


    @Autowired
    private DependencyQueuer dependencyQueuer;

    @Override
    protected NFVMessage doWork() throws Exception {
        log.info("Instantiation is finished for vnfr: " + virtualNetworkFunctionRecord.getName() + " his nsr id father is:" + virtualNetworkFunctionRecord.getParent_ns_id());
        VirtualNetworkFunctionRecord existing = vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());
        log.debug("VNFR arrived version= " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("VNFR existing version= " + existing.getHb_version());
        saveVirtualNetworkFunctionRecord();

        dependencyManagement.fillParameters(virtualNetworkFunctionRecord);

        NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr())
            log.debug("Current Vnfrs in the database: " + vnfr.getName());
        dependencyQueuer.releaseVNFR(virtualNetworkFunctionRecord.getName(), nsr);
        log.debug("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
        int dep;
        dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
        log.debug("Found " + dep + " dependencies");
        boolean allVnfrInInactive = allVnfrInInactive(networkServiceRecordRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id()));
        if (ordered != null && Boolean.parseBoolean(ordered)) {

            if (dep == 0) {
                virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
                saveVirtualNetworkFunctionRecord();
                if (allVnfrInInactive) {
                    VirtualNetworkFunctionRecord nextToCallStart = getNextToCallStart(virtualNetworkFunctionRecord);
                    if (nextToCallStart != null) {
                        vnfmManager.getVnfrNames().get(virtualNetworkFunctionRecord.getParent_ns_id()).remove(nextToCallStart.getName());
                        sendStart(nextToCallStart);
                    }
                } else {
                    log.debug("Not calling start to next VNFR because not all VNFRs are in state INACTIVE");
                }
            } else {
                log.debug("Not calling start to next VNFR because not all VNFRs are in state INACTIVE");
            }
        } else {
            if (dep == 0) {
                sendStart(virtualNetworkFunctionRecord);
            }
        }
        return null;
    }

    private void sendStart(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
        log.info("VNFR: " + virtualNetworkFunctionRecord.getName() + " (" + virtualNetworkFunctionRecord.getId() + ") has 0 dependencies, Calling START");
        log.debug("HIBERNATE VERSION IS: " + virtualNetworkFunctionRecord.getHb_version());
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.START), vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
