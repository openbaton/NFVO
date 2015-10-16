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

package org.openbaton.nfvo.core.core;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFRDependencyRepository;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.*;

/**
 * Created by lto on 30/06/15.
 */
@Service
@Scope
public class DependencyManagement implements org.openbaton.nfvo.core.interfaces.DependencyManagement {


    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;

    @Autowired
    private org.openbaton.nfvo.core.interfaces.DependencyQueuer dependencyQueuer;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NetworkServiceRecordRepository nsrRepository;

    @Autowired
    private VNFRDependencyRepository vnfrDependencyRepository;

    @Override
    public int provisionDependencies(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NoResultException, NotFoundException, InterruptedException {
        NetworkServiceRecord nsr = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        int dep = 0;
        if (nsr.getStatus().ordinal() != Status.ERROR.ordinal()) {
            Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();
            for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies) {
                log.trace(vnfRecordDependency.getTarget() + " == " + virtualNetworkFunctionRecord.getName());
                if (vnfRecordDependency.getTarget().equals(virtualNetworkFunctionRecord.getName())) {
                    dep++;
                    //waiting for them to finish
                    log.debug("found dependency: " + vnfRecordDependency);
                    Set<String> notInitIds = getNotInitializedVnfrSource(vnfRecordDependency.getIdType().keySet(), nsr);
                    if (notInitIds.size() > 0) {
                        dependencyQueuer.waitForVNFR(vnfRecordDependency.getId(), notInitIds);
                        log.debug("Found " + notInitIds.size() + " for VNFR " + virtualNetworkFunctionRecord.getName() + " ( " + virtualNetworkFunctionRecord.getId() + " ) ");
                    } else {
                        //send sendMessageToVNFR to VNFR
                        OrVnfmGenericMessage orVnfmGenericMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.MODIFY);
                        orVnfmGenericMessage.setVnfrd(vnfRecordDependency);
                        vnfmManager.sendMessageToVNFR(virtualNetworkFunctionRecord, orVnfmGenericMessage);
                    }
                    return dep;
                }
            }
            log.debug("Found 0 for VNFR " + virtualNetworkFunctionRecord.getName() + " ( " + virtualNetworkFunctionRecord.getId() + " ) ");
            return 0;
        } else return -1;
    }

    @Override
    public synchronized void fillParameters(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        NetworkServiceRecord nsr = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        if (nsr.getStatus().ordinal() != Status.ERROR.ordinal()) {
            Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();

            for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies) {
                vnfRecordDependency = vnfrDependencyRepository.findOne(vnfRecordDependency.getId());

                DependencyParameters dp = vnfRecordDependency.getParameters().get(virtualNetworkFunctionRecord.getType());
                if (dp != null) {
                    for (Map.Entry<String, String> keyValueDep : dp.getParameters().entrySet()) {
                        for (ConfigurationParameter cp : virtualNetworkFunctionRecord.getProvides().getConfigurationParameters()) {
                            if (cp.getConfKey().equals(keyValueDep.getKey())) {
                                log.debug("Filling parameter " + keyValueDep.getKey() + " with value: " + cp.getValue());
                                keyValueDep.setValue(cp.getValue());
                                break;
                            }
                        }
                    }
                    vnfrDependencyRepository.save(vnfRecordDependency);
                }
                log.debug("Filled parameter for depedendency target = " + vnfRecordDependency.getTarget() + " with parameters: " + vnfRecordDependency.getParameters());
            }

        }
    }

    private Set<String> getNotInitializedVnfrSource(Set<String> ids, NetworkServiceRecord nsr) {

        Set<String> res = new HashSet<>();
        for (String sourceName : ids) {
            log.debug("Looking for VNFR name: " + sourceName);
            boolean found = false;
            for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
                if (sourceName.equals(vnfr.getName())) {
                    found = true;
                    if (vnfr.getStatus().ordinal() < Status.INITIALIZED.ordinal())
                        res.add(vnfr.getName() + nsr.getId());
                }
            }
            if (!found)
                res.add(sourceName + nsr.getId());
        }
        if (!res.isEmpty())
            log.debug("There are the following not initialized vnfr sources:" + res);
        return res;
    }

    @Override
    public VNFRecordDependency getDependencyForAVNFRecordTarget(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord){
        NetworkServiceRecord nsr = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        if (nsr.getStatus().ordinal() != Status.ERROR.ordinal()) {
            Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();

            for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies) {
                vnfRecordDependency = vnfrDependencyRepository.findOne(vnfRecordDependency.getId());

                if (vnfRecordDependency.getTarget().equals(virtualNetworkFunctionRecord.getName())){
                    return vnfRecordDependency;
                }

            }
        }
        return null;
    }

    @Override
    public List<VNFRecordDependency> getDependencyForAVNFRecordSource(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        List<VNFRecordDependency> res = new ArrayList<>();
        NetworkServiceRecord nsr = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        if (nsr.getStatus().ordinal() != Status.ERROR.ordinal()) {
            Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();

            for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies) {
                vnfRecordDependency = vnfrDependencyRepository.findOne(vnfRecordDependency.getId());

                log.trace("Checking if " + virtualNetworkFunctionRecord.getName() + " is source for " + vnfRecordDependency);
                if (vnfRecordDependency.getIdType().containsKey(virtualNetworkFunctionRecord.getName())){
                    res.add(vnfRecordDependency);
                }
            }
        }
        return res;
    }
}
