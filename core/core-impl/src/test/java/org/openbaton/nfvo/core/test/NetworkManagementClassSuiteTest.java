/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.core.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.openbaton.nfvo.core.test.TestUtils.createVimInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.core.NetworkManagement;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/** Created by lto on 20/04/15. */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NetworkServiceRecordManagementClassSuiteTest.class})
@Configuration
public class NetworkManagementClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Mock public VimBroker vimBroker;

  @Mock private VimDriverCaller vimDriverCaller;

  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @InjectMocks private NetworkManagement networkManagement;

  @InjectMocks private MyVim myVim;

  @Mock private NetworkRepository networkRepository;

  @Autowired private ConfigurableApplicationContext context;

  @AfterClass
  public static void shutdown() {
    // TODO Teardown to avoid exceptions during test shutdown
  }

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    log.info("Starting test");
  }

  @Test
  public void nfvImageManagementNotNull() {
    Assert.assertNotNull(networkManagement);
  }

  @Test
  public void networkManagementUpdateTest() throws VimException, PluginException {
    when(vimBroker.getVim(anyString())).thenReturn(myVim);
    Network network = createNetwork();
    network.setName("UpdatedName");
    network.setExternal(true);
    //		Vim vim = vimBroker.getVim("mocked_vim");
    //		when(vim.update(any(VimInstance.class), any(Network.class))).thenReturn(network);

    BaseNetwork updated_network = networkManagement.update(createVimInstance(), network);

    Assert.assertEquals(updated_network.getName(), network.getName());
    Assert.assertEquals(updated_network.getExtId(), network.getExtId());
    //    Assert.assertEquals(updated_network.getExternal(), network.getExternal());
  }

  private Network createNetwork() {
    Network network = new Network();
    network.setName("network_name");
    network.setExtId("ext_id");
    network.setExternal(false);
    network.setShared(false);
    network.setSubnets(
        new HashSet<Subnet>() {
          {
            add(createSubnet());
          }
        });
    return network;
  }

  private Subnet createSubnet() {
    final Subnet subnet = new Subnet();
    subnet.setName("subnet_name");
    subnet.setExtId("ext_id");
    subnet.setCidr("192.168.1.0/24");
    subnet.setNetworkId("network_id");
    return subnet;
  }

  @Test
  public void networkManagementAddTest()
      throws VimException, PluginException, BadRequestException, AlreadyExistingException,
          ExecutionException, InterruptedException, IOException {
    Network network_exp = createNetwork();
    when(networkRepository.save(any(Network.class))).thenReturn(network_exp);
    when(vimBroker.getVim(anyString())).thenReturn(myVim);

    BaseNetwork network_new = networkManagement.add(createVimInstance(), network_exp);

    Assert.assertEquals(network_exp.getId(), network_new.getId());
    Assert.assertEquals(network_exp.getName(), network_new.getName());
    Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
  }

  @Test
  public void networkManagementQueryTest() {
    when(networkRepository.findAll()).thenReturn(new ArrayList<>());

    //Assert.assertEquals(0, networkManagement.query().size());

    Network network_exp = createNetwork();
    when(networkRepository.findOne(network_exp.getId())).thenReturn(network_exp);
    BaseNetwork networkNew = networkManagement.query(network_exp.getId());
    Assert.assertEquals(network_exp.getId(), networkNew.getId());
    Assert.assertEquals(network_exp.getName(), networkNew.getName());
    Assert.assertEquals(network_exp.getExtId(), networkNew.getExtId());
    //    Assert.assertEquals(network_exp.getExternal(), networkNew.getExternal());
  }

  @Test
  public void networkManagementDeleteTest() throws VimException, PluginException {
    BaseNetwork network_exp = createNetwork();
    when(vimBroker.getVim(anyString())).thenReturn(myVim);
    when(networkRepository.findOne(network_exp.getId())).thenReturn(network_exp);
    networkManagement.delete(createVimInstance(), network_exp);
    when(networkRepository.findOne(anyString())).thenReturn(null);
    BaseNetwork network_new = networkManagement.query(network_exp.getId());
    Assert.assertNull(network_new);
  }
}
