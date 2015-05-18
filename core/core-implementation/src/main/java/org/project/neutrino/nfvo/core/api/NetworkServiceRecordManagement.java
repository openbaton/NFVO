package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.core.utils.NSDUtils;
import org.project.neutrino.nfvo.core.utils.NSRUtils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NetworkServiceRecordManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private NSDUtils nsdUtils;

    @Override
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) {

//        for (String s : context.getBeanDefinitionNames())
//                log.debug(s);

        /*
        Create NSR
         */
        nsdUtils.fetchData(networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);
        log.trace("Deploying " + networkServiceRecord);
        nsrRepository.create(networkServiceRecord);


        log.debug("created NetworkServiceRecord with id " + networkServiceRecord.getId());

        /*
         * Getting the vim based on the VDU datacenter type
         * Calling the vim to create the Resources
         */
        List<Future<String>> ids = new ArrayList<>();
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
            for(VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                ResourceManagement vim;
                switch (virtualDeploymentUnit.getDatacenter().getType()) {
                    case "test":
                        vim = (ResourceManagement) context.getBean("testVIM");
                        break;
                    case "openstack":
                        vim = (ResourceManagement) context.getBean("openstackVIM");
                        break;
                    case "amazon":
                        vim = (ResourceManagement) context.getBean("amazonVIM");
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                ids.add(vim.allocate(virtualDeploymentUnit,virtualNetworkFunctionRecord));
            }
        }
        return networkServiceRecord;
    }

    @Override
    public NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NetworkServiceRecord> query() {
        return nsrRepository.findAll();
    }

    @Override
    public NetworkServiceRecord query(String id) {
        return nsrRepository.find(id);
    }

    @Override
    public void delete(String id) {
        nsrRepository.remove(nsrRepository.find(id));
    }
}
