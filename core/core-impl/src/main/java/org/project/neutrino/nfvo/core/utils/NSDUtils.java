package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFRecordDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.BadFormatException;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope("prototype")
public class NSDUtils {

    @Autowired
    @Qualifier("vimRepository")
    private GenericRepository<VimInstance> vimRepository;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void fetchVimInstances(NetworkServiceDescriptor networkServiceDescriptor) throws NoResultException, NotFoundException, BadFormatException {

        /**
         * Fetching VimInstances
         */
        List<VimInstance> vimInstances = vimRepository.findAll();
        if (vimInstances.size() == 0){
            throw new NotFoundException("No VimInstances in the Database");
        }

        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {

                String name_id = vdu.getVimInstance().getName() != null ? vdu.getVimInstance().getName() : vdu.getVimInstance().getId();

                for(VimInstance vimInstance : vimInstances){
                    if ((vimInstance.getName() != null && vimInstance.getName().equals(name_id)) || (vimInstance.getId() != null && vimInstance.getId().equals(name_id))){
                        vdu.setVimInstance(vimInstance);
                        log.debug("Found vimInstance: "+ vimInstance);
                        break;
                    }
                }
                if (vdu.getVimInstance() == null){
                    throw new NotFoundException("No VimInstance with name or id equals to " + name_id);
                }
            }
        }
    }

    public void fetchDependencies(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        /**
         * Fetching dependencies
         */

        for (VNFDependency vnfDependency:networkServiceDescriptor.getVnf_dependency()){
            log.trace(""+vnfDependency);
            VirtualNetworkFunctionDescriptor source = vnfDependency.getSource();
            VirtualNetworkFunctionDescriptor target = vnfDependency.getTarget();

            if(source == null || target == null || source.getName() == null || target.getName() == null){
                throw new BadFormatException("Source name and Target name must be defined in the request json file");
            }
            boolean sourceFound = false;
            boolean targetFound = false;

            for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()){
                if (virtualNetworkFunctionDescriptor.getName().equals(source.getName())){
                    vnfDependency.setSource(virtualNetworkFunctionDescriptor);
                    sourceFound = true;
                    log.trace("Found " + virtualNetworkFunctionDescriptor);
                }
                else if (virtualNetworkFunctionDescriptor.getName().equals(target.getName())){
                    vnfDependency.setTarget(virtualNetworkFunctionDescriptor);
                    targetFound = true;
                    log.trace("Found " + virtualNetworkFunctionDescriptor);
                }
            }

            if (!(sourceFound || targetFound)) {
                String name = sourceFound ? target.getName() : source.getName();
                throw new NotFoundException(name + " was not found in the NetworkServiceDescriptor");
            }
        }
    }
}
