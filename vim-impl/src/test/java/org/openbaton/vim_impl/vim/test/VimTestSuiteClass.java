/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.vim_impl.vim.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.plugin.utils.RabbitPluginBroker;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.vim_impl.vim.AmazonVIM;
import org.openbaton.vim_impl.vim.OpenstackVIM;
import org.openbaton.vim_impl.vim.TestVIM;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
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

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

//import org.openbaton.nfvo.common.exceptions.VimException;

/**
 * Created by lto on 21/05/15.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(MockitoJUnitRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = {"mocked_id=1234567890", "port: 4242"})
@PrepareForTest({Vim.class})
public class VimTestSuiteClass {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ConfigurableApplicationContext context;

    @Mock
    private VimDriverCaller vimDriverCaller;

    @Mock
    private RabbitPluginBroker rabbitPluginBroker;

//    @InjectMocks
    //@Qualifier("OpenstackVim")
    private OpenstackVIM openstackVIM;

    /**
     * TODO add all other tests
     */

    @Autowired
    private Environment environment;

    @Autowired
    private VimBroker vimBroker;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(VimDriverCaller.class).withParameterTypes(String.class,String.class).withArguments("openstack","15672").thenReturn(vimDriverCaller);
        openstackVIM = new OpenstackVIM("15672");
        openstackVIM.setClient(vimDriverCaller);
    }

    @Test
    public void testVimBrokers() throws PluginException {
        Assert.assertNotNull(vimBroker);
        Vim testVIM = vimBroker.getVim("test");
        Assert.assertEquals(testVIM.getClass(), TestVIM.class);
        Vim openstackVIM = vimBroker.getVim("openstack");
        Assert.assertEquals(openstackVIM.getClass(), OpenstackVIM.class);
        Assert.assertEquals(vimBroker.getVim("amazon").getClass(), AmazonVIM.class);
        exception.expect(UnsupportedOperationException.class);
        vimBroker.getVim("throw_exception");
    }

    @Test
    public void testVimOpenstack() throws VimDriverException, VimException, RemoteException {
        VirtualDeploymentUnit vdu = createVDU();
        VirtualNetworkFunctionRecord vnfr = createVNFR();
        ArrayList<String> networks = new ArrayList<>();
        networks.add("network1");
        ArrayList<String> secGroups = new ArrayList<>();
        secGroups.add("secGroup1");

        Server server = new Server();
        server.setExtId(environment.getProperty("mocked_id"));
        server.setIps(new HashMap<String, List<String>>());
        //TODO use the method launchInstanceAndWait properly
        when(vimDriverCaller.launchInstanceAndWait(any(VimInstance.class), anyString(), anyString(), anyString(), anyString(), anySet(), anySet(), anyString(), anyMap())).thenReturn(server);
        VimInstance vimInstance = createVIM();
        try {

            Future<VNFCInstance> id = openstackVIM.allocate(vimInstance,vdu, vnfr, vdu.getVnfc().iterator().next(), "", new HashMap<String, String>());
            String expectedId = id.get().getVc_id();
            log.debug(expectedId + " == " + environment.getProperty("mocked_id"));
            Assert.assertEquals(expectedId, environment.getProperty("mocked_id"));
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

        vdu.getVm_image().removeAll(vdu.getVm_image());

        exception.expect(VimException.class);
        openstackVIM.allocate(vimInstance, vdu, vnfr, vdu.getVnfc().iterator().next(), "", new HashMap<String, String>());
    }

    @Test
    @Ignore
    public void testVimAmazon() {
    }

    @Test
    @Ignore
    public void testVimTest() {
    }

    @Test
    @Ignore
    public void testOpenstackClient() {
    }

    private VirtualNetworkFunctionRecord createVNFR() {
        VirtualNetworkFunctionRecord vnfr = new VirtualNetworkFunctionRecord();
        vnfr.setName("testVnfr");
        vnfr.setStatus(Status.INITIALIZED);
        vnfr.setAudit_log("audit_log");
        vnfr.setDescriptor_reference("test_dr");
        VNFDeploymentFlavour deployment_flavour = new VNFDeploymentFlavour();
        deployment_flavour.setFlavour_key("m1.small");
        vnfr.setDeployment_flavour_key("m1.small");
        return vnfr;
    }

    private VirtualDeploymentUnit createVDU() {
        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
        VimInstance vimInstance = createVIM();
        HashSet<VNFComponent> vnfc = new HashSet<>();
        vnfc.add(new VNFComponent());
        vdu.setVnfc(vnfc);
        Set<String> monitoring_parameter = new HashSet<>();
        monitoring_parameter.add("parameter_1");
        monitoring_parameter.add("parameter_2");
        monitoring_parameter.add("parameter_3");
        vdu.setMonitoring_parameter(monitoring_parameter);
        vdu.setComputation_requirement("computation_requirement");
        Set<String> vm_images = new HashSet<>();
        vm_images.add("image_1234");
        vdu.setVm_image(vm_images);
        vimInstance.setFlavours(new HashSet<DeploymentFlavour>());
        DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
        deploymentFlavour.setExtId("ext_id");
        deploymentFlavour.setFlavour_key("m1.small");
        vimInstance.getFlavours().add(deploymentFlavour);
        return vdu;
    }

    private VimInstance createVIM() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setSecurityGroups(new HashSet<String>() {{
            add("mock_vim_instance");
        }});
        vimInstance.setKeyPair("test");
        HashSet<DeploymentFlavour> flavours = new HashSet<>();
        DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
        deploymentFlavour.setExtId("ext_id1");
        deploymentFlavour.setFlavour_key("m1.small");
        flavours.add(deploymentFlavour);
        vimInstance.setFlavours(flavours);
        vimInstance.setImages(new HashSet<NFVImage>() {{
            NFVImage nfvImage = new NFVImage();
            nfvImage.setName("image_1234");
            nfvImage.setExtId("ext_id");
            add(nfvImage);
        }});
        return vimInstance;
    }
}
