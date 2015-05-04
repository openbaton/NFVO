package org.project.neutrino.nfvo.repositories.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.repositories.NSDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 30/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class RepositoriesClassSuiteTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext ctx;

    private NSDRepository nsdRepository;

    @BeforeClass
    public void init(){
        nsdRepository = new NSDRepository();
    }

    @Test
    public void repositoryNotNullTest(){
        Assert.assertNotNull(nsdRepository);
    }

    @Test
    public void createEntityTest(){
        NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

        nsdRepository.create(nsd);

        Assert.assertNotNull(nsd);
        Assert.assertNotNull(nsd.getId());
        log.debug("Id is: " + nsd.getId());

    }

    @Test
    public void findEntityTest(){
        NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

        nsdRepository.create(nsd);

        Assert.assertNotNull(nsd);
        Assert.assertNotNull(nsd.getId());
        log.debug("Id is: " + nsd.getId());

        List<NetworkServiceDescriptor> all = nsdRepository.findAll();
        log.debug("" + all);
        for (NetworkServiceDescriptor n : all){
            log.debug(n.toString());
        }

        NetworkServiceDescriptor new_nsd = (NetworkServiceDescriptor) nsdRepository.find(nsd.getId());

        Assert.assertNotNull(new_nsd);
        Assert.assertNotNull(new_nsd.getId());
    }

    private NetworkServiceDescriptor createNetworkServiceDescriptor() {
        NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
        nsd.setVendor("FOKUS");
        List<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new ArrayList<VirtualNetworkFunctionDescriptor>();
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
        virtualNetworkFunctionDescriptor.setMonitoring_parameter( new ArrayList<String>() {{add("monitor1");add("monitor2");add("monitor3");}});
        final VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
        vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
        vdu.setComputation_requirement("high_requirements");
        virtualNetworkFunctionDescriptor.setVdu(new ArrayList<VirtualDeploymentUnit>() {{add(vdu);}});
        virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
        nsd.setVnfd(virtualNetworkFunctionDescriptors);
        return nsd;
    }

}
