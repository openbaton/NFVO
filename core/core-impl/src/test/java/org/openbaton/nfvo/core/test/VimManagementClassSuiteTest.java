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

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Location;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.api.VimManagement;
import org.openbaton.nfvo.repositories.*;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 20/04/15. */
public class VimManagementClassSuiteTest {

  private static final String projectId = "project-id";

  @Rule public ExpectedException exception = ExpectedException.none();

  @Mock private VimBroker vimBroker;

  @Mock private VimRepository vimRepository;

  @Mock private ImageRepository imageRepository;

  @Mock private NetworkRepository networkRepository;

  @Mock private VNFDRepository vnfdRepository;

  @Mock private VNFRRepository vnfrRepository;

  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @InjectMocks private VimManagement vimManagement;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    log.info("Starting test");
  }

  @Test
  public void vimManagementNotNull() {
    Assert.assertNotNull(vimManagement);
  }

  @Test
  public void vimManagementRefreshTest()
      throws VimException, PluginException, IOException, EntityUnreachableException,
          BadRequestException, AlreadyExistingException {
    initMocks();
    VimInstance vimInstance = createVimInstance();
    vimManagement.refresh(vimInstance);

    Assert.assertEquals(0, vimInstance.getFlavours().size());
    Assert.assertEquals(0, vimInstance.getImages().size());
    Assert.assertEquals(0, vimInstance.getNetworks().size());
  }

  @Test
  public void vimManagementUpdateTest()
      throws VimException, PluginException, IOException, EntityUnreachableException,
          BadRequestException, AlreadyExistingException {
    initMocks();
    VimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.findFirstById(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    when(vimRepository.save(vimInstance_exp)).thenReturn(vimInstance_exp);
    VimInstance vimInstance_new = createVimInstance();
    vimInstance_new.setName("UpdatedName");
    vimInstance_new.setTenant("UpdatedTenant");
    vimInstance_new.setUsername("UpdatedUsername");
    when(vimRepository.save(vimInstance_new)).thenReturn(vimInstance_new);
    when(vnfdRepository.findByProjectId(anyString()))
        .thenReturn(new ArrayList<VirtualNetworkFunctionDescriptor>());
    when(vnfrRepository.findByProjectId(anyString()))
        .thenReturn(new ArrayList<VirtualNetworkFunctionRecord>());

    vimInstance_exp = vimManagement.update(vimInstance_new, vimInstance_exp.getId(), projectId);

    Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
    Assert.assertEquals(vimInstance_exp.getTenant(), vimInstance_new.getTenant());
    Assert.assertEquals(vimInstance_exp.getType(), vimInstance_new.getType());
    Assert.assertEquals(vimInstance_exp.getKeyPair(), vimInstance_new.getKeyPair());
    Assert.assertEquals(vimInstance_exp.getUsername(), vimInstance_new.getUsername());
    Assert.assertEquals(vimInstance_exp.getAuthUrl(), vimInstance_new.getAuthUrl());
    Assert.assertEquals(vimInstance_exp.getPassword(), vimInstance_new.getPassword());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getName(), vimInstance_new.getLocation().getName());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getLatitude(), vimInstance_new.getLocation().getLatitude());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getLongitude(), vimInstance_new.getLocation().getLongitude());
    Assert.assertEquals(vimInstance_exp.getFlavours().size(), 0);
    Assert.assertEquals(vimInstance_exp.getImages().size(), 0);
    Assert.assertEquals(vimInstance_exp.getNetworks().size(), 0);
  }

  @Test
  public void nfvImageManagementAddTest()
      throws VimException, PluginException, IOException, EntityUnreachableException,
          BadRequestException, AlreadyExistingException {
    initMocks();
    VimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.save(any(VimInstance.class))).thenReturn(vimInstance_exp);
    VimInstance vimInstance_new = vimManagement.add(vimInstance_exp, projectId);

    Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
    Assert.assertEquals(vimInstance_exp.getTenant(), vimInstance_new.getTenant());
    Assert.assertEquals(vimInstance_exp.getType(), vimInstance_new.getType());
    Assert.assertEquals(vimInstance_exp.getKeyPair(), vimInstance_new.getKeyPair());
    Assert.assertEquals(vimInstance_exp.getUsername(), vimInstance_new.getUsername());
    Assert.assertEquals(vimInstance_exp.getAuthUrl(), vimInstance_new.getAuthUrl());
    Assert.assertEquals(vimInstance_exp.getPassword(), vimInstance_new.getPassword());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getName(), vimInstance_new.getLocation().getName());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getLatitude(), vimInstance_new.getLocation().getLatitude());
    Assert.assertEquals(
        vimInstance_exp.getLocation().getLongitude(), vimInstance_new.getLocation().getLongitude());
    Assert.assertEquals(vimInstance_exp.getFlavours().size(), vimInstance_new.getFlavours().size());
    Assert.assertEquals(vimInstance_exp.getImages().size(), vimInstance_new.getImages().size());
    Assert.assertEquals(vimInstance_exp.getNetworks().size(), vimInstance_new.getNetworks().size());
  }

  private void initMocks() throws VimException, PluginException {
    Vim vim = mock(Vim.class);
    when(vim.queryImages(any(VimInstance.class))).thenReturn(new ArrayList<NFVImage>());
    when(vimBroker.getVim(anyString())).thenReturn(vim);
    doNothing().when(imageRepository).delete(any(NFVImage.class));
    doNothing().when(networkRepository).delete(any(Network.class));
  }

  @Test
  public void nfvImageManagementQueryTest() {

    VimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    when(vimRepository.findFirstById(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    VimInstance vimInstance_new = vimManagement.query(vimInstance_exp.getId(), projectId);
    Assert.assertEquals(vimInstance_exp.getId(), vimInstance_new.getId());
    Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
    Assert.assertEquals(vimInstance_exp.getFlavours().size(), vimInstance_new.getFlavours().size());
    Assert.assertEquals(vimInstance_exp.getImages().size(), vimInstance_new.getImages().size());
    Assert.assertEquals(vimInstance_exp.getNetworks().size(), vimInstance_new.getNetworks().size());
  }

  @Test
  public void nfvImageManagementDeleteTest() throws NotFoundException, BadRequestException {
    VimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    when(vimRepository.findFirstById(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    vimManagement.delete(vimInstance_exp.getId(), projectId);
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(null);
    when(vimRepository.findFirstById(vimInstance_exp.getId())).thenReturn(null);
    VimInstance vimInstance_new = vimManagement.query(vimInstance_exp.getId(), projectId);
    Assert.assertNull(vimInstance_new);
  }

  private VimInstance createVimInstance() {
    VimInstance vimInstance = new VimInstance();
    vimInstance.setActive(true);
    vimInstance.setProjectId(projectId);
    vimInstance.setName("vim_instance");
    vimInstance.setPassword("password");
    Location location = new Location();
    location.setName("LocationName");
    location.setLatitude("Latitude");
    location.setLongitude("Longitude");
    vimInstance.setLocation(location);
    vimInstance.setType("test");
    vimInstance.setNetworks(
        new HashSet<Network>() {
          {
            Network network = new Network();
            network.setExtId("ext_id");
            network.setName("network_name");
            add(network);
          }
        });
    vimInstance.setFlavours(
        new HashSet<DeploymentFlavour>() {
          {
            DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_1");
            deploymentFlavour.setFlavour_key("flavor_name");
            add(deploymentFlavour);

            deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_2");
            deploymentFlavour.setFlavour_key("m1.tiny");
            add(deploymentFlavour);
          }
        });
    vimInstance.setImages(
        new HashSet<NFVImage>() {
          {
            NFVImage image = new NFVImage();
            image.setExtId("ext_id_1");
            image.setName("ubuntu-14.04-server-cloudimg-amd64-disk1");
            add(image);

            image = new NFVImage();
            image.setExtId("ext_id_2");
            image.setName("image_name_1");
            add(image);
          }
        });
    return vimInstance;
  }
}
