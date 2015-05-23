package org.project.neutrino.nfvo.repositories.tests;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

//import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;

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

    @Autowired
    @Qualifier("NSDRepository")
    private GenericRepository<NetworkServiceDescriptor> nsdRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void init(){
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
        log.debug("id is: " + nsd.getId());

    }

    @Test
    public void findEntityTest(){
        NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

        nsdRepository.create(nsd);

        Assert.assertNotNull(nsd);
        Assert.assertNotNull(nsd.getId());
        log.debug("id is: " + nsd.getId());

        List<NetworkServiceDescriptor> all = nsdRepository.findAll();
        log.debug("" + all);
        for (NetworkServiceDescriptor n : all){
            log.debug(n.toString());
        }

        NetworkServiceDescriptor new_nsd = null;
        new_nsd = nsdRepository.find(nsd.getId());

        Assert.assertNotNull(new_nsd);
        Assert.assertNotNull(new_nsd.getId());
    }


    @Test
    public void nsdRepositoryFindTest() {
        Assert.assertEquals(0, nsdRepository.findAll().size());
    }

    @Test
    public void nsdRepositoryMergeTest() {
        NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();
        NetworkServiceDescriptor nsd_new;
        nsd.setVendor("0");
        nsdRepository.create(nsd);

        for (int i = 0; i < 10; i++) {
            nsd.setVendor("" + i);
            int version = nsd.getHb_version();
            nsd_new = nsdRepository.merge(nsd);
            Assert.assertEquals(nsd_new.getVendor(), "" + i);
            int new_version = nsd_new.getHb_version();
            log.warn("Expected " + (1 + version) + " but was " + new_version);
            // Assert.assertEquals(new_version, (version));
            nsd = nsd_new;
        }

        nsdRepository.remove(nsd);
    }

    @Test
    public void nsdRepositoryPersistTest() {
        NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

        nsdRepository.create(nsd);

        String id = nsd.getId();

        Assert.assertNotNull(id);

        NetworkServiceDescriptor nsd_new = null;
        nsd_new = nsdRepository.find(id);

        Assert.assertEquals(nsd.getId(), nsd_new.getId());
        Assert.assertEquals(nsd.getVersion(), nsd_new.getVersion());
        Assert.assertEquals(nsd.getVendor(), nsd_new.getVendor());
        for (int i = 0; i < nsd.getVnfd().size(); i++) {
            Assert.assertEquals(((VirtualNetworkFunctionDescriptor) nsd
                            .getVnfd().toArray()[i]).getId(),
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd()
                            .toArray()[i]).getId());
            Assert.assertEquals(((VirtualNetworkFunctionDescriptor) nsd
                            .getVnfd().toArray()[i]).getVersion(),
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd()
                            .toArray()[i]).getVersion());
            for (int k = 0; k < ((VirtualNetworkFunctionDescriptor) nsd
                    .getVnfd().toArray()[i]).getMonitoring_parameter().size(); k++) {
                Assert.assertEquals(
                        ((VirtualNetworkFunctionDescriptor) nsd.getVnfd()
                                .toArray()[i]).getMonitoring_parameter().get(k),
                        ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd()
                                .toArray()[i]).getMonitoring_parameter().get(k));
            }
            for (int j = 0; j < ((VirtualNetworkFunctionDescriptor) nsd
                    .getVnfd().toArray()[i]).getVdu().size(); j++) {
                Assert.assertEquals(
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getId(),
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd_new
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getId());
                Assert.assertEquals(
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getVersion(),
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd_new
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getVersion());
                Assert.assertEquals(
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getComputation_requirement(),
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd_new
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getComputation_requirement());
                Assert.assertEquals(
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getHigh_availability(),
                        ((VirtualDeploymentUnit) ((VirtualNetworkFunctionDescriptor) nsd_new
                                .getVnfd().toArray()[i]).getVdu().toArray()[j])
                                .getHigh_availability());
            }
        }

        nsdRepository.remove(nsd);

        NetworkServiceDescriptor nsd_null = null;

        exception.expect(javax.persistence.NoResultException.class);
        nsd_null = nsdRepository.find(id);

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
