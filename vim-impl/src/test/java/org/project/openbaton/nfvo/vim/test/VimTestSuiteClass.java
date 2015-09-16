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

package org.project.openbaton.nfvo.vim.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.Server;
import org.project.openbaton.catalogue.nfvo.VimInstance;
//import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.vim.AmazonVIM;
import org.project.openbaton.nfvo.vim.OpenstackVIM;
import org.project.openbaton.nfvo.vim.TestVIM;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    private VimBroker vimBroker;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

//    @Test
//    public void testVimBrokers(){
//
//        Assert.assertNotNull(vimBroker);
//        Vim testVIM = vimBroker.getVim("test");
//        Assert.assertEquals(testVIM.getClass(), TestVIM.class);
//        Vim openstackVIM = vimBroker.getVim("openstack");
//        Assert.assertEquals(openstackVIM.getClass(), OpenstackVIM.class);
//        Assert.assertEquals(vimBroker.getVim("amazon").getClass(), AmazonVIM.class);
//        exception.expect(UnsupportedOperationException.class);
//        vimBroker.getVim("throw_exception");
//    }
//
//    @Ignore
//    @Test
//    public void testVimOpenstack() throws VimDriverException, VimException {
//        VirtualDeploymentUnit vdu = createVDU();
//        VirtualNetworkFunctionRecord vnfr = createVNFR();
//        ArrayList<String> networks = new ArrayList<>();
//        networks.add("network1");
//        ArrayList<String> secGroups = new ArrayList<>();
//        secGroups.add("secGroup1");
//
//        Server server = new Server();
//        server.setExtId(environment.getProperty("mocked_id"));
//        //TODO use the method launchInstanceAndWait properly
//        //when(clientInterfaces.launchInstanceAndWait(anyString(), anyString(), anyString(), anyString(), anySet(), anySet(), anyString())).thenReturn(server);
//
//        try {
//            Future<String> id = openstackVIM.allocate(vdu, vnfr, );
//            String expectedId = id.get();
//            log.debug(expectedId + " == " + environment.getProperty("mocked_id"));
//            Assert.assertEquals(expectedId, environment.getProperty("mocked_id"));
//            Assert.assertEquals(vdu.getHostname(), vnfr.getName() + "-" + vdu.getId().substring((vdu.getId().length()-5), vdu.getId().length()-1));
//        } catch (VimException e) {
//            e.printStackTrace();
//            Assert.fail();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//        vdu.getVm_image().remove(0);
//
//        exception.expect(VimException.class);
//        openstackVIM.allocate(vdu, vnfr, );
//    }

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
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setImages(new HashSet<NFVImage>() {{
            NFVImage nfvImage = new NFVImage();
            nfvImage.setName("image_1234");
            nfvImage.setExtId("ext_id");
            add(nfvImage);
        }});
        vdu.setVimInstance(vimInstance);
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
}
