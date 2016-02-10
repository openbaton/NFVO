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
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmScalingMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
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
 * Created by mob on 03.12.15.
 */
@Service
@Scope("prototype")
public class HealTask extends AbstractTask {

    private VNFCInstance vnfcInstance;
    private String cause;
    @Autowired private DependencyManagement dependencyManagement;
    @Autowired private NetworkServiceRecordRepository nsrRepository;
    @Autowired private VNFRecordDependencyRepository vnfRecordDependencyRepository;

    @Override
    protected NFVMessage doWork() throws Exception {

        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        log.info("NFVO: VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName() + " has finished Healing");

        saveVirtualNetworkFunctionRecord();

        // read property file if to execute the healed task
        // if yes

        if(cause!=null && !cause.equals("switchToStandby")){
            log.info("normal Healing terminated");
            return null;

        }
        if(vnfcInstance==null){
            log.error("The vnfcInstance returned for the switch to standby function is null");
            return null;
        }
        if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("active"))
            log.debug("The vnfcInstance activated is: "+vnfcInstance.toString());
        else{
            log.error("The vnfcInstance returned for the switch to standby function has STATE null or different to ACTIVE");
            return null;
        }

        //Find all the dependency (VNFC) sources where I am the target
        List<VNFRecordDependency> dependenciesSource = dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);
        log.debug(virtualNetworkFunctionRecord.getName() + " is source of " + dependenciesSource.size() + " dependencies");

        for (VNFRecordDependency dependency : dependenciesSource) {
            VirtualNetworkFunctionRecord vnfrToNotify = getVnfrTarget(networkServiceRecord,dependency.getTarget());

            //new Dependency containing only the new VNFC
            VNFRecordDependency dependency_new = new VNFRecordDependency();
            //This remains the same because the target still depends on the same VNFs
            dependency_new.setIdType(new HashMap<String, String>());
            for (Map.Entry<String, String> entry : dependency.getIdType().entrySet()) {
                dependency_new.getIdType().put(entry.getKey(), entry.getValue());
            }
            //----

            dependency_new.setParameters(new HashMap<String, DependencyParameters>());
            //Here there could be new parameters coming from the vnfr already scaled out
            DependencyParameters dependencyParameters = new DependencyParameters();
            dependencyParameters.setParameters(new HashMap<String, String>());
            HashMap<String, String> parametersNew = new HashMap<>();
            for (Map.Entry<String, String> entry : dependency.getParameters().get(virtualNetworkFunctionRecord.getType()).getParameters().entrySet()) {
                parametersNew.put(entry.getKey(), entry.getValue());
            }
            dependencyParameters.getParameters().putAll(parametersNew);
            dependency_new.getParameters().put(virtualNetworkFunctionRecord.getType(), dependencyParameters);
            //----

            //Now add only the dependency with the VNFC already activated
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

            String vnfcId = getVnfcId();
            log.debug("Added VNFCInstance: " + vnfcInstance);

            vnfcDependencyParameters.getParameters().put(vnfcId, vnfcDP);
            dependency_new.getVnfcParameters().put(virtualNetworkFunctionRecord.getType(), vnfcDependencyParameters);
            //----
            //Update the current dependency
            if (dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()) == null) {
                VNFCDependencyParameters vnfcDependencyParameters1 = new VNFCDependencyParameters();
                vnfcDependencyParameters1.setParameters(new HashMap<String, DependencyParameters>());
                dependency.getVnfcParameters().put(virtualNetworkFunctionRecord.getType(), vnfcDependencyParameters1);
            }
            if (dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters() == null)
                dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).setParameters(new HashMap<String, DependencyParameters>());
            //Add new VNFC dependency to the current dependency!
            dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters().putAll(vnfcDependencyParameters.getParameters());
            log.debug("Current dependency: "+dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters());
            //Delete the failed VNFCInstance from the current dependency
            VNFCInstance failedVnfc = getVnfcInSuchState("failed");
            log.debug("Failed vnfc: "+failedVnfc);
            if(failedVnfc!=null)
                dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters().remove(failedVnfc.getId());
            log.debug("Current dependency without failed vnfc: "+dependency.getVnfcParameters().get(virtualNetworkFunctionRecord.getType()).getParameters());
            //----

            //Set the target
            dependency_new.setTarget(dependency.getTarget());
            //----

            //need to update dependency in the repo
            vnfRecordDependencyRepository.save(dependency);
            log.debug("Dependency updated: " + dependency);
            //----

            /*//Preparing scaling in message
            OrVnfmScalingMessage orVnfmScalingMessage = new OrVnfmScalingMessage();
            orVnfmScalingMessage.setAction(Action.SCALE_IN);
            orVnfmScalingMessage.setVirtualNetworkFunctionRecord(vnfrToNotify);
            orVnfmScalingMessage.setVnfcInstance(failedVnfc);

            //Getting the sender
            VnfmManagerEndpoint vnfmManagerEndpoint = vnfmRegister.getVnfm(vnfrToNotify.getEndpoint());
            VnfmSender vnfmSender = this.getVnfmSender(vnfmManagerEndpoint.getEndpointType());
            vnfmSender.sendCommand(orVnfmScalingMessage, vnfmManagerEndpoint);
            log.debug("scaling in message sent");
            Thread.sleep(2000);*/
            VnfmManagerEndpoint vnfmManagerEndpoint = vnfmRegister.getVnfm(vnfrToNotify.getEndpoint());
            VnfmSender vnfmSender = this.getVnfmSender(vnfmManagerEndpoint.getEndpointType());

            //Preparing modify message (with only the new dependency)
            OrVnfmGenericMessage modifyMessage = new OrVnfmGenericMessage(vnfrToNotify, Action.MODIFY);
            modifyMessage.setVnfrd(dependency_new);
            vnfmSender.sendCommand(modifyMessage, vnfmManagerEndpoint);
            log.debug("modify in message sent");
        }

        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());

        return null;
    }

    private String getVnfcId() {
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                if (this.vnfcInstance.getVc_id().equals(vnfcInstance.getVc_id())) {
                    return vnfcInstance.getId();
                }
            }
            if (vnfcInstance.getId() != null){
                return vnfcInstance.getId();
            }
        }
        return null;
    }
    private VNFCInstance getVnfcInSuchState(String state) {
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                if (vnfcInstance.getState()!=null && vnfcInstance.getState().equals(state)) {
                    return vnfcInstance;
                }
            }
        }
        return null;
    }

    private VirtualNetworkFunctionRecord getVnfrTarget(NetworkServiceRecord networkServiceRecord, String target) throws NotFoundException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            if (vnfr.getName().equals(target)) {
                return vnfr;
            }
        }
        throw new NotFoundException("The vnfr with name: "+target+" is not found");
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public VNFCInstance getVnfcInstance() {
        return vnfcInstance;
    }

    public void setVnfcInstance(VNFCInstance vnfcInstance) {
        this.vnfcInstance = vnfcInstance;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}