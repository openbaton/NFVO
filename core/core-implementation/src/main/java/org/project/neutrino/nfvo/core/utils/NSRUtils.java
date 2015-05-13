package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.*;
import org.project.neutrino.nfvo.catalogue.mano.record.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 11/05/15.
 */
public class NSRUtils {

    public static NetworkServiceRecord createNetworkServiceRecord(NetworkServiceDescriptor networkServiceDescriptor){
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();

        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(networkServiceDescriptor.getMonitoring_parameter());
        networkServiceRecord.setAuto_scale_policy(networkServiceDescriptor.getAuto_scale_policy());
        networkServiceRecord.setVnfr(new ArrayList<VirtualNetworkFunctionRecord>());
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = NSRUtils.createVirtualNetworkFunctionRecord(vnfd);
            virtualNetworkFunctionRecord.setParent_ns(networkServiceRecord);
            //TODO set dependecies!!!
            networkServiceRecord.getVnfr().add(virtualNetworkFunctionRecord);

        }
        networkServiceRecord.setLifecycle_event(networkServiceDescriptor.getLifecycle_event());
        List<PhysicalNetworkFunctionRecord> pnfrs = new ArrayList<PhysicalNetworkFunctionRecord>();
        for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()){
            pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
        }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.INITIAILZED);
        networkServiceRecord.setVnffgr(networkServiceDescriptor.getVnffgd());
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new ArrayList<VirtualLinkRecord>());
        for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()){
            networkServiceRecord.getVlr().add(NSRUtils.createVirtualLinkRecord(virtualLinkDescriptor));
        }
        networkServiceRecord.setVnf_dependency(networkServiceDescriptor.getVnf_dependency());

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

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor vnfd) {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();

        virtualNetworkFunctionRecord.setMonitoring_parameter(vnfd.getMonitoring_parameter());
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(vnfd.getAuto_scale_policy());
        // TODO mange the VirtualLinks and links...
//        virtualNetworkFunctionRecord.setConnected_external_virtual_link(vnfd.getVirtual_link());
        virtualNetworkFunctionRecord.setVdu(vnfd.getVdu());
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(vnfd.getConnection_point());
        // TODO find a way to choose between deployment flavors
//        virtualNetworkFunctionRecord.setDeployment_flavour(vnfd.getDeployment_flavour());
        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(vnfd.getLifecycle_event());
        virtualNetworkFunctionRecord.setVirtual_link(vnfd.getVirtual_link());
        virtualNetworkFunctionRecord.setStatus(Status.INITIAILZED);
        return virtualNetworkFunctionRecord;
    }
}
