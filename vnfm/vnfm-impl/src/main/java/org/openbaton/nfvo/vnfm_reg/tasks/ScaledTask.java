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
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScaledTask extends AbstractTask {

    @Autowired
    private DependencyManagement dependencyManagement;

    @Autowired
    private NetworkServiceRecordRepository nsrRepository;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());


        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        log.debug("NFVO: VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName() + " has finished scaling");
        log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
        saveVirtualNetworkFunctionRecord();

        List<VNFRecordDependency> dependenciesSource = dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);
        log.debug(virtualNetworkFunctionRecord.getName() + " is source of " + dependenciesSource.size() + " dependencies");
        //TODO set up dependencies, HOW TO DO WHEN multiple VNFC???
        for (VNFRecordDependency dependency : dependenciesSource){
            for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
                if (vnfr.getName().equals(dependency.getTarget())) {
                    OrVnfmGenericMessage message = new OrVnfmGenericMessage(vnfr, Action.MODIFY);
                    message.setVnfrd(dependency);

                    vnfmSender.sendCommand(message,vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
                }
            }
        }
    }


    @Override
    public boolean isAsync() {
        return true;
    }
}
