package org.project.openbaton.nfvo.core.utils;

import org.project.openbaton.nfvo.catalogue.mano.common.*;
import org.project.openbaton.nfvo.catalogue.mano.descriptor.*;
import org.project.openbaton.nfvo.catalogue.mano.record.*;
import org.project.openbaton.nfvo.catalogue.nfvo.Network;
import org.project.openbaton.nfvo.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 11/05/15.
 */
public class NSRUtils {
    private static Logger log = LoggerFactory.getLogger(NSRUtils.class);
    public static NetworkServiceRecord createNetworkServiceRecord(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        log.debug("" + networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
        networkServiceRecord.setName(networkServiceDescriptor.getName());
        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(new HashSet<String>());
        networkServiceRecord.getMonitoring_parameter().addAll(networkServiceDescriptor.getMonitoring_parameter());
        networkServiceRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        networkServiceRecord.getAuto_scale_policy().addAll(networkServiceDescriptor.getAuto_scale_policy());
        networkServiceRecord.setVnfr(new HashSet<VirtualNetworkFunctionRecord>());
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = NSRUtils.createVirtualNetworkFunctionRecord(vnfd);
//            virtualNetworkFunctionRecord.setParent_ns(networkServiceRecord);
            networkServiceRecord.getVnfr().add(virtualNetworkFunctionRecord);
        }
        //TODO set dependencies!!! (DONE)
        networkServiceRecord.setVnf_dependency(new HashSet<VNFRecordDependency>());
        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
            VNFRecordDependency vnfDependency_new = new VNFRecordDependency();


            for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
                log.debug("Source is: " + vnfDependency.getSource().getName() + ". Target is: " + vnfDependency.getTarget().getName() + ". VNFR is: " + virtualNetworkFunctionRecord.getName());
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

        networkServiceRecord.setLifecycle_event(new HashSet<LifecycleEvent>());
        networkServiceRecord.getLifecycle_event().addAll(networkServiceDescriptor.getLifecycle_event());
        Set<PhysicalNetworkFunctionRecord> pnfrs = new HashSet<PhysicalNetworkFunctionRecord>();
        if(networkServiceDescriptor.getPnfd() != null)
            for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()){
                pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
            }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.INITIAILZED);
        networkServiceRecord.setVnffgr(new HashSet<VNFForwardingGraphRecord>());
//      TODO translate them from descriptors to records
//        networkServiceRecord.getVnffgr().addAll(networkServiceDescriptor.getVnffgd());
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new HashSet<VirtualLinkRecord>());
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

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException, BadFormatException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setName(vnfd.getName());
        virtualNetworkFunctionRecord.setType(vnfd.getType());
        virtualNetworkFunctionRecord.setMonitoring_parameter(new HashSet<String>());
        virtualNetworkFunctionRecord.getMonitoring_parameter().addAll(vnfd.getMonitoring_parameter());
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        virtualNetworkFunctionRecord.getAuto_scale_policy().addAll(vnfd.getAuto_scale_policy());

        // TODO mange the VirtualLinks and links...
//        virtualNetworkFunctionRecord.setConnected_external_virtual_link(vnfd.getVirtual_link());

        virtualNetworkFunctionRecord.setVdu(new HashSet<VirtualDeploymentUnit>());
        virtualNetworkFunctionRecord.getVdu().addAll(vnfd.getVdu());
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(new HashSet<ConnectionPoint>());
        virtualNetworkFunctionRecord.getConnection_point().addAll(vnfd.getConnection_point());

        // TODO find a way to choose between deployment flavors and create the new one
        virtualNetworkFunctionRecord.setDeployment_flavour_key(vnfd.getDeployment_flavour().iterator().next().getFlavour_key());
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()){
            if (!existsDeploymentFlavor(virtualNetworkFunctionRecord.getDeployment_flavour_key(), virtualDeploymentUnit.getVimInstance())){
                throw new BadFormatException("no key " + virtualNetworkFunctionRecord.getDeployment_flavour_key() + " found in vim instance: " + virtualDeploymentUnit.getVimInstance());
            }
        }

        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(new HashSet<LifecycleEvent>());
        virtualNetworkFunctionRecord.getLifecycle_event().addAll(vnfd.getLifecycle_event());
        virtualNetworkFunctionRecord.setVirtual_link(new HashSet<InternalVirtualLink>());
        virtualNetworkFunctionRecord.getVirtual_link().addAll(vnfd.getVirtual_link());
        virtualNetworkFunctionRecord.setVnf_address(new HashSet<String>());
        virtualNetworkFunctionRecord.setStatus(Status.INITIAILZED);
        return virtualNetworkFunctionRecord;
    }

    private static boolean existsDeploymentFlavor(String key, VimInstance vimInstance){
        log.debug("" + vimInstance);
        for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()){
            if (deploymentFlavour.getFlavour_key().equals(key) || deploymentFlavour.getExtId().equals(key) || deploymentFlavour.getId().equals(key)){
                return true;
            }
        }
        return false;
    }

    public static void createConnectionsPoints(VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu, Network network) {
        //Create ConnectionPoint for VNFR
        ConnectionPoint connectionPoint = new ConnectionPoint();
        connectionPoint.setName(network.getName());
        connectionPoint.setExtId(network.getExtId());
        connectionPoint.setType(network.getNetworkType());
        vnfr.getConnection_point().add(connectionPoint);
        //Create ConnectionPoint for VDU
        VNFDConnectionPoint vnfdConnectionPoint = new VNFDConnectionPoint();
        vnfdConnectionPoint.setVirtual_link_reference(network.getName());
        vnfdConnectionPoint.setName(network.getName());
        vnfdConnectionPoint.setExtId(network.getExtId());
        vnfdConnectionPoint.setType(network.getNetworkType());
        //Create VNFC for VDU
        VNFComponent vnfComponent = new VNFComponent();
        vnfComponent.getConnection_point().add(vnfdConnectionPoint);
        vdu.getVnfc().add(vnfComponent);
    }

}
