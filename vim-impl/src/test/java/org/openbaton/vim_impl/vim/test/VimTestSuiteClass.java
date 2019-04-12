/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.ImageStatus;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.plugin.utils.RabbitPluginBroker;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.openbaton.vim_impl.vim.GenericVIM;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

// import org.openbaton.nfvo.common.exceptions.VimException;

/** Created by lto on 21/05/15. */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationTest.class})
@PowerMockRunnerDelegate(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@TestPropertySource(properties = {"mocked_id=1234567890", "port: 4242"})
@PrepareForTest({Vim.class})
@SuppressWarnings({"unsafe", "unchecked"})
public class VimTestSuiteClass {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Autowired private ConfigurableApplicationContext context;

  @Mock private VimDriverCaller vimDriverCaller;

  @Mock private RabbitPluginBroker rabbitPluginBroker;

  private GenericVIM genericVIM;

  /** TODO add all other tests */
  @Autowired private Environment environment;

  @Autowired private VimBroker vimBroker;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
    PowerMockito.whenNew(VimDriverCaller.class)
        .withParameterTypes(
            String.class,
            String.class,
            String.class,
            int.class,
            String.class,
            String.class,
            String.class,
            String.class,
            int.class)
        .withArguments(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyInt())
        .thenReturn(vimDriverCaller);
    genericVIM = new GenericVIM();
    genericVIM.setClient(vimDriverCaller);
  }

  @Test
  public void testVimBrokers() {
    //    Assert.assertNotNull(vimBroker);
    //    Vim genericVim = vimBroker.getVim("generic");
    //    Assert.assertEquals(genericVim.getClass(), GenericVIM.class);
    //    //        exception.expect(UnsupportedOperationException.class);
    //    Assert.assertEquals(vimBroker.getVim("throw_exception").getClass(), GenericVIM.class);
  }

  @Test
  public void testVimOpenstack() throws VimDriverException, VimException {
    VirtualDeploymentUnit vdu = createVDU();
    VirtualNetworkFunctionRecord vnfr = createVNFR();

    Server server = new Server();
    server.setExtId(environment.getProperty("mocked_id"));
    server.setIps(new HashMap<>());
    server.setFloatingIps(new HashMap<>());
    // TODO use the method launchInstanceAndWait properly
    when(vimDriverCaller.launchInstanceAndWait(
            any(BaseVimInstance.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anySet(),
            anySet(),
            anyString(),
            anyMap(),
            anySet()))
        .thenReturn(server);
    BaseVimInstance vimInstance = createVIM();
    try {

      Future<VNFCInstance> id =
          genericVIM.allocate(
              vimInstance,
              vdu,
              vnfr,
              vdu.getVnfc().iterator().next(),
              "",
              new HashMap<>(),
              new HashSet<>());
      String expectedId = id.get().getVc_id();
      log.debug(expectedId + " == " + environment.getProperty("mocked_id"));
      Assert.assertEquals(expectedId, environment.getProperty("mocked_id"));
    } catch (VimException | ExecutionException | InterruptedException e) {
      e.printStackTrace();
      Assert.fail();
    }

    vdu.getVm_image().removeAll(vdu.getVm_image());

    exception.expect(VimException.class);
    genericVIM.allocate(
        vimInstance,
        vdu,
        vnfr,
        vdu.getVnfc().iterator().next(),
        "",
        new HashMap<>(),
        new HashSet<>());
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
    OpenstackVimInstance vimInstance = createVIM();
    HashSet<VNFComponent> vnfcs = new HashSet<>();
    VNFComponent vnfc = new VNFComponent();
    Set<VNFDConnectionPoint> vnfdCps = new HashSet<>();
    VNFDConnectionPoint vnfcCp = new VNFDConnectionPoint();
    vnfcCp.setVirtual_link_reference("network1");
    vnfdCps.add(vnfcCp);
    vnfc.setConnection_point(vnfdCps);
    vnfcs.add(vnfc);
    vdu.setVnfc(vnfcs);
    Set<String> monitoring_parameter = new HashSet<>();
    monitoring_parameter.add("parameter_1");
    monitoring_parameter.add("parameter_2");
    monitoring_parameter.add("parameter_3");
    vdu.setMonitoring_parameter(monitoring_parameter);
    vdu.setComputation_requirement("m1.small");
    List<String> vm_images = new ArrayList<>();
    vm_images.add("image_1234");
    vdu.setVm_image(vm_images);
    vimInstance.setFlavours(new HashSet<>());
    DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
    deploymentFlavour.setExtId("ext_id");
    deploymentFlavour.setFlavour_key("m1.small");
    vimInstance.getFlavours().add(deploymentFlavour);
    return vdu;
  }

  private OpenstackVimInstance createVIM() {
    OpenstackVimInstance vimInstance = new OpenstackVimInstance();
    vimInstance.setName("mock_vim_instance");
    vimInstance.setSecurityGroups(
        new HashSet<String>() {
          {
            add("mock_vim_instance");
          }
        });
    vimInstance.setKeyPair("test");
    HashSet<DeploymentFlavour> flavours = new HashSet<>();
    DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
    deploymentFlavour.setExtId("ext_id1");
    deploymentFlavour.setFlavour_key("m1.small");
    flavours.add(deploymentFlavour);
    vimInstance.setFlavours(flavours);
    vimInstance.setImages(
        new HashSet<NFVImage>() {
          {
            NFVImage nfvImage = new NFVImage();
            nfvImage.setName("image_1234");
            nfvImage.setExtId("ext_id");
            nfvImage.setStatus(ImageStatus.ACTIVE.name());
            add(nfvImage);
          }
        });
    Network network = new Network();
    network.setName("network1");
    network.setExtId("mocked_ext_id");
    Set<Network> networks = new HashSet<>();
    networks.add(network);
    vimInstance.setNetworks(networks);
    return vimInstance;
  }
}
