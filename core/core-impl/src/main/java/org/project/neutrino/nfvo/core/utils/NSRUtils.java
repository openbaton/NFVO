package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.*;
import org.project.neutrino.nfvo.catalogue.mano.record.*;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
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
    public static NetworkServiceRecord createNetworkServiceRecord(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException {
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
        networkServiceRecord.setName(networkServiceDescriptor.getName());
        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(networkServiceDescriptor.getMonitoring_parameter());
        networkServiceRecord.setAuto_scale_policy(networkServiceDescriptor.getAuto_scale_policy());
        networkServiceRecord.setVnfr(new ArrayList<VirtualNetworkFunctionRecord>());
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = NSRUtils.createVirtualNetworkFunctionRecord(vnfd);
//            virtualNetworkFunctionRecord.setParent_ns(networkServiceRecord);
            //TODO set dependencies!!!
            networkServiceRecord.getVnfr().add(virtualNetworkFunctionRecord);

        }
        networkServiceRecord.setLifecycle_event(networkServiceDescriptor.getLifecycle_event());
        List<PhysicalNetworkFunctionRecord> pnfrs = new ArrayList<PhysicalNetworkFunctionRecord>();
        if(networkServiceDescriptor.getPnfd() != null)
            for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()){
                pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
            }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.INITIAILZED);
        networkServiceRecord.setVnffgr(networkServiceDescriptor.getVnffgd());
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new ArrayList<VirtualLinkRecord>());
        if(networkServiceDescriptor.getVld() != null)
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

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setName(vnfd.getName());
        virtualNetworkFunctionRecord.setType(vnfd.getType());
        virtualNetworkFunctionRecord.setMonitoring_parameter(vnfd.getMonitoring_parameter());
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(vnfd.getAuto_scale_policy());

        // TODO mange the VirtualLinks and links...
//        virtualNetworkFunctionRecord.setConnected_external_virtual_link(vnfd.getVirtual_link());

        virtualNetworkFunctionRecord.setVdu(vnfd.getVdu());
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(vnfd.getConnection_point());

        // TODO find a way to choose between deployment flavors and create the new one (DONE)
        virtualNetworkFunctionRecord.setDeployment_flavour(getDeployment_flavour(vnfd));

        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(vnfd.getLifecycle_event());
        virtualNetworkFunctionRecord.setVirtual_link(vnfd.getVirtual_link());
        virtualNetworkFunctionRecord.setStatus(Status.INITIAILZED);
        return virtualNetworkFunctionRecord;
    }

    private static VNFDeploymentFlavour getDeployment_flavour(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException {
        List<VimInstance> vimInstances = new ArrayList<VimInstance>();
        for(VirtualDeploymentUnit vdu : vnfd.getVdu()){
            vimInstances.add(vdu.getVimInstance());
        }
        for (VNFDeploymentFlavour deploymentFlavour : vnfd.getDeployment_flavour()){
            for(VimInstance vimInstance : vimInstances){
                log.debug(""+vimInstance);
                for (DeploymentFlavour df : vimInstance.getFlavours()){
                    try {
                        if (deploymentFlavour.getFlavour_key().equals(df.getFlavour_key()) || deploymentFlavour.getExtId().equals(df.getExtId()) || deploymentFlavour.getId().equals(df.getId())) {
                            log.trace("Found DeploymentFlavor: " + df);
                            deploymentFlavour.setFlavour_key(df.getFlavour_key());
                            deploymentFlavour.setExtId(df.getExtId());
                            return deploymentFlavour;
                        }
                    }catch (NullPointerException e){

                    }
                }
            }
        }
        throw new NotFoundException("No deploymentFlavors matching any of: " + vnfd.getDeployment_flavour());
    }
}
