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

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFCInstanceRepository;
import org.openbaton.nfvo.repositories.VNFRecordDependencyRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private VNFRecordDependencyRepository vnfRecordDependencyRepository;

    @Autowired
    private VNFCInstanceRepository vnfcInstanceRepository;

    private VNFCInstance vnfcInstance;

    public VNFCInstance getVnfcInstance() {
        return vnfcInstance;
    }

    public void setVnfcInstance(VNFCInstance vnfcInstance) {
        this.vnfcInstance = vnfcInstance;
    }

    @Override
    protected NFVMessage doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());


        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        log.debug("NFVO: VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName() + " has finished scaling");
        log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
        saveVirtualNetworkFunctionRecord();

        //If the VNFCInstace is in standby the NFVO doesn't have to configure the VNF source dependencies
        if(vnfcInstance!=null){
            log.debug("The current vnfcInstance is: "+vnfcInstance.toString());
            if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("standby"))
                return null;
        }

        List<VNFRecordDependency> dependenciesSource = dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);
        log.debug(virtualNetworkFunctionRecord.getName() + " is source of " + dependenciesSource.size() + " dependencies");

        for (VNFRecordDependency dependency : dependenciesSource) {
            for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
                if (vnfr.getName().equals(dependency.getTarget())) {
                    OrVnfmGenericMessage message = new OrVnfmGenericMessage(vnfr, Action.MODIFY);

                    //new Dependency containing only the new VNFC
                    VNFRecordDependency dependency_new = new VNFRecordDependency();
                    dependency_new.setIdType(new HashMap<String, String>());
                    for (Map.Entry<String, String> entry : dependency.getIdType().entrySet()) {
                        dependency_new.getIdType().put(entry.getKey(), entry.getValue());
                    }
                    dependency_new.setParameters(new HashMap<String, DependencyParameters>());

                    DependencyParameters dependencyParameters = new DependencyParameters();
                    dependencyParameters.setParameters(new HashMap<String, String>());

                    //set values of VNFCI new
                    HashMap<String, String> parametersNew = new HashMap<>();
                    for (Map.Entry<String, String> entry : dependency.getParameters().get(virtualNetworkFunctionRecord.getType()).getParameters().entrySet()) {
                        parametersNew.put(entry.getKey(), entry.getValue());
                    }

                    dependencyParameters.getParameters().putAll(parametersNew);
                    dependency_new.getParameters().put(virtualNetworkFunctionRecord.getType(), dependencyParameters);

                    dependency_new.setVnfcParameters(new HashMap<String, VNFCDependencyParameters>());
                    VNFCDependencyParameters vnfcDependencyParameters = new VNFCDependencyParameters();
                    vnfcDependencyParameters.setParameters(new HashMap<String, DependencyParameters>());

                    DependencyParameters vnfcDP = new DependencyParameters();
                    vnfcDP.setParameters(new HashMap<String, String>());

                    for (Ip ip : vnfcInstance.getFloatingIps())
                        vnfcDP.getParameters().put(ip.getNetName() + "_floatingIp",ip.getIp());

                    for (Ip ip : vnfcInstance.getIps())
                        vnfcDP.getParameters().put(ip.getNetName(), ip.getIp());

                    vnfcDP.getParameters().put("hostname", vnfcInstance.getHostname());

                    String vnfcId = "";

                    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                        for (VNFCInstance vnfcInstance1 : vdu.getVnfc_instance()) {
                            if (vnfcInstance.getVc_id().equals(vnfcInstance1.getVc_id())) {
                                vnfcId = vnfcInstance1.getId();
                                break;
                            }
                        }
                        if (vnfcInstance.getId() != null){
                            break;
                        }
                    }
                    log.debug("Added VNFCInstance: " + vnfcInstance);

//                    vnfcInstanceRepository.save(vnfcInstance);

                    vnfcDependencyParameters.getParameters().put(vnfcId, vnfcDP);

                    dependency_new.getVnfcParameters().put(virtualNetworkFunctionRecord.getType(), vnfcDependencyParameters);
                    if (dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()) == null) {
                        VNFCDependencyParameters vnfcDependencyParameters1 = new VNFCDependencyParameters();
                        vnfcDependencyParameters1.setParameters(new HashMap<String, DependencyParameters>());
                        dependency.getVnfcParameters().put(virtualNetworkFunctionRecord.getType(), vnfcDependencyParameters1);
                    }
                    if (dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters() == null)
                        dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).setParameters(new HashMap<String, DependencyParameters>());
                    dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters().putAll(vnfcDependencyParameters.getParameters());


                    dependency_new.setTarget(dependency.getTarget());

                    //TODO Delete the failed dependency of the VNFCInstance in failed state

                    message.setVnfrd(dependency_new);

                    //need to update dependency
                    vnfRecordDependencyRepository.save(dependency);
                    log.debug("Dependency updated: " + dependency);

                    vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
                }
            }
        }
        return null;
    }


    @Override
    public boolean isAsync() {
        return true;
    }
}
