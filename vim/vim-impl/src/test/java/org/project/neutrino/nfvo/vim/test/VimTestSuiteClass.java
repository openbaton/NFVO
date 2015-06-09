package org.project.neutrino.nfvo.vim.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.Status;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim.AmazonVIM;
import org.project.neutrino.nfvo.vim.OpenstackVIM;
import org.project.neutrino.nfvo.vim.TestVIM;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 21/05/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = { "mocked_id=1234567890", "port: 4242" })
public class VimTestSuiteClass {

    /**
     * TODO add all other tests
     */

    @Autowired
    private Environment environment;

    @Mock
    ClientInterfaces clientInterfaces;

    @InjectMocks
    OpenstackVIM openstackVIM;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private VimBroker<ResourceManagement> resourceManagementVimBroker;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testVimBrokers(){

        Assert.assertNotNull(resourceManagementVimBroker);
        ResourceManagement testVIM = resourceManagementVimBroker.getVim("test");
        Assert.assertEquals(testVIM.getClass(), TestVIM.class);
        ResourceManagement openstackVIM = resourceManagementVimBroker.getVim("openstack");
        Assert.assertEquals(openstackVIM.getClass(), OpenstackVIM.class);
        Assert.assertEquals(resourceManagementVimBroker.getVim("amazon").getClass(), AmazonVIM.class);
        exception.expect(UnsupportedOperationException.class);
        resourceManagementVimBroker.getVim("throw_exception");
    }

    @Test
    public void testVimOpenstack() throws VimException {
        VirtualDeploymentUnit vdu = createVDU();
        VirtualNetworkFunctionRecord vnfr = createVNFR();
        ArrayList<String> networks = new ArrayList<>();
        networks.add("network1");
        ArrayList<String> secGroups = new ArrayList<>();
        secGroups.add("secGroup1");

        Server server = new Server();
        server.setExtId(environment.getProperty("mocked_id"));
        when(clientInterfaces.launchInstanceAndWait(anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString())).thenReturn(server);

        try {
            Future<String> id = openstackVIM.allocate(vdu, vnfr);
            String expectedId = id.get();
            log.debug(expectedId + " == " + environment.getProperty("mocked_id"));
            Assert.assertEquals(expectedId, environment.getProperty("mocked_id"));
            Assert.assertEquals(vdu.getHostname(), vnfr.getName() + "-" + vdu.getId().substring((vdu.getId().length()-5), vdu.getId().length()-1));
        } catch (VimException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Assert.fail();
        }

        vdu.getVm_image().remove(0);

        exception.expect(VimException.class);
        openstackVIM.allocate(vdu, vnfr);
    }

    @Test
    @Ignore
    public void testVimAmazon(){}

    @Test
    @Ignore
    public void testVimTest(){}

    @Test
    @Ignore
    public void testOpenstackClient(){}

    private VirtualNetworkFunctionRecord createVNFR(){
        VirtualNetworkFunctionRecord vnfr = new VirtualNetworkFunctionRecord();
        vnfr.setName("testVnfr");
        vnfr.setStatus(Status.INITIAILZED);
        vnfr.setAudit_log("audit_log");
        vnfr.setDescriptor_reference("test_dr");
        VNFDeploymentFlavour deployment_flavour = new VNFDeploymentFlavour();
        deployment_flavour.setFlavour_key("m1.small");
        vnfr.setDeployment_flavour_key("m1.small");
        return vnfr;
    }

    private VirtualDeploymentUnit createVDU() {
        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setImages(new ArrayList<NFVImage>(){{
            NFVImage nfvImage = new NFVImage();
            nfvImage.setName("image_1234");
            add(nfvImage);}});
        vdu.setVimInstance(vimInstance);
        ArrayList<String> monitoring_parameter = new ArrayList<>();
        monitoring_parameter.add("parameter_1");
        monitoring_parameter.add("parameter_2");
        monitoring_parameter.add("parameter_3");
        vdu.setMonitoring_parameter(monitoring_parameter);
        vdu.setComputation_requirement("computation_requirement");
        ArrayList<String> vm_images = new ArrayList<>();
        vm_images.add("image_1234");
        vdu.setVm_image(vm_images);
        return vdu;
    }
}
