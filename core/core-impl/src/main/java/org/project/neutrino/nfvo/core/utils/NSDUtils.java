package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
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

    public void fetchData(NetworkServiceDescriptor networkServiceDescriptor) throws NoResultException, NotFoundException {

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
}
