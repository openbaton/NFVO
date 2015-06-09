package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.common.*;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.*;
import org.project.neutrino.nfvo.catalogue.mano.record.*;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.BadFormatException;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 11/05/15.
 */
public class NSRUtils {
    private static Logger log = LoggerFactory.getLogger(NSRUtils.class);
    public static NetworkServiceRecord createNetworkServiceRecord(final NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
        networkServiceRecord.setName(networkServiceDescriptor.getName());
        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(new ArrayList<String>() {{
            addAll(networkServiceDescriptor.getMonitoring_parameter());
        }});
        networkServiceRecord.setAuto_scale_policy(new ArrayList<AutoScalePolicy>() {{
            addAll(networkServiceDescriptor.getAuto_scale_policy());
        }});
        networkServiceRecord.setVnfr(new ArrayList<VirtualNetworkFunctionRecord>());
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = NSRUtils.createVirtualNetworkFunctionRecord(vnfd);
//            virtualNetworkFunctionRecord.setParent_ns(networkServiceRecord);
            networkServiceRecord.getVnfr().add(virtualNetworkFunctionRecord);
        }
        //TODO set dependencies!!! (DONE)
        networkServiceRecord.setVnf_dependency(new ArrayList<VNFRecordDependency>());
        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
            VNFRecordDependency vnfDependency_new = new VNFRecordDependency();

            for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
                if (vnfDependency.getSource().getName().equals(virtualNetworkFunctionRecord.getName())) {
                    vnfDependency_new.setSource(virtualNetworkFunctionRecord);
                }
                else if (vnfDependency.getTarget().getName().equals(virtualNetworkFunctionRecord.getName())){
                    vnfDependency_new.setTarget(virtualNetworkFunctionRecord);
                }
            }

            if (vnfDependency_new.getSource() == null || vnfDependency_new.getTarget() == null){
                throw new BadFormatException("No available VNFR found in NSR for dependency: " +vnfDependency);
            }
            networkServiceRecord.getVnf_dependency().add(vnfDependency_new);
        }

        networkServiceRecord.setLifecycle_event(new ArrayList<LifecycleEvent>() {{addAll(networkServiceDescriptor.getLifecycle_event());}});
        List<PhysicalNetworkFunctionRecord> pnfrs = new ArrayList<PhysicalNetworkFunctionRecord>();
        if(networkServiceDescriptor.getPnfd() != null)
            for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()){
                pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
            }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.INITIAILZED);
        networkServiceRecord.setVnffgr(new ArrayList<VNFForwardingGraph>(){{addAll(networkServiceDescriptor.getVnffgd());}});
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new ArrayList<VirtualLinkRecord>());
        if(networkServiceDescriptor.getVld() != null) {
            for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()) {
                networkServiceRecord.getVlr().add(NSRUtils.createVirtualLinkRecord(virtualLinkDescriptor));
            }
        }
        return networkServiceRecord;
    }

    public static VirtualLinkRecord createVirtualLinkRecord(VirtualLinkDescriptor virtualLinkDescriptor) {
        VirtualLinkRecord virtualLinkRecord = new VirtualLinkRecord();
        // TODO implement it
        return virtualLinkRecord;
    }

    public static PhysicalNetworkFunctionRecord createPhysicalNetworkFunctionRecord(PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor) {
        PhysicalNetworkFunctionRecord physicalNetworkFunctionRecord = new PhysicalNetworkFunctionRecord();
        // TODO implement it
        return physicalNetworkFunctionRecord;
    }

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(final VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException, BadFormatException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setName(vnfd.getName());
        virtualNetworkFunctionRecord.setType(vnfd.getType());
        virtualNetworkFunctionRecord.setMonitoring_parameter(new ArrayList<String>() {{
            addAll(vnfd.getMonitoring_parameter());
        }});
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(new ArrayList<AutoScalePolicy>() {{
            addAll(vnfd.getAuto_scale_policy());
        }});


        // TODO mange the VirtualLinks and links...
//        virtualNetworkFunctionRecord.setConnected_external_virtual_link(vnfd.getVirtual_link());

        virtualNetworkFunctionRecord.setVdu(new ArrayList<VirtualDeploymentUnit>() {{
            addAll(vnfd.getVdu());
        }});
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(new ArrayList<ConnectionPoint>() {{
            addAll(vnfd.getConnection_point());
        }});

        // TODO find a way to choose between deployment flavors and create the new one
        virtualNetworkFunctionRecord.setDeployment_flavour_key(vnfd.getDeployment_flavour().get(0).getFlavour_key());
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()){
            if (!existsDeploymentFlavor(virtualNetworkFunctionRecord.getDeployment_flavour_key(), virtualDeploymentUnit.getVimInstance())){
                throw new BadFormatException("no key " + virtualNetworkFunctionRecord.getDeployment_flavour_key() + " found in vim instance: " + virtualDeploymentUnit.getVimInstance());
            }
        }

        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(new ArrayList<LifecycleEvent>() {{
            addAll(vnfd.getLifecycle_event());
        }});
        virtualNetworkFunctionRecord.setVirtual_link(new ArrayList<InternalVirtualLink>(){{vnfd.getVirtual_link();}});
        virtualNetworkFunctionRecord.setStatus(Status.INITIAILZED);
        return virtualNetworkFunctionRecord;
    }

    private static boolean existsDeploymentFlavor(String key, VimInstance vimInstance){
        for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()){
            if (deploymentFlavour.getFlavour_key().equals(key) || deploymentFlavour.getExtId().equals(key) || deploymentFlavour.getId().equals(key)){
                return true;
            }
        }
        return false;
    }
}
