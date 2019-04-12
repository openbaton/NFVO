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

package org.openbaton.nfvo.core.test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openbaton.nfvo.core.test.TestUtils.createVimInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.api.VimManagement;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unsafe", "unchecked"})
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
  //  @Mock private VimManagement.AsyncVimManagement asyncVimManagement;

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
      throws VimException, PluginException, IOException, ExecutionException, InterruptedException {
    initMocks();
    BaseVimInstance vimInstance = createVimInstance();
    when(vimRepository.save(any(BaseVimInstance.class))).thenReturn(vimInstance);
    vimManagement.refresh(vimInstance, false).get();

    //    Assert.assertEquals(2, vimInstance.getFlavours().size());
    Assert.assertEquals(2, vimInstance.getImages().size());
    Assert.assertEquals(1, vimInstance.getNetworks().size());
  }

  @Test
  public void vimManagementUpdateTest()
      throws VimException, PluginException, IOException, BadRequestException, NotFoundException,
          ExecutionException, InterruptedException {
    initMocks();
    OpenstackVimInstance vimInstanceExp = createVimInstance();
    when(vimRepository.findFirstByIdAndProjectId(vimInstanceExp.getId(), projectId))
        .thenReturn(vimInstanceExp);
    when(vimRepository.save(vimInstanceExp)).thenReturn(vimInstanceExp);
    OpenstackVimInstance vimInstance_new = createVimInstance();
    vimInstance_new.setName("UpdatedName");
    vimInstance_new.setTenant("UpdatedTenant");
    vimInstance_new.setUsername("UpdatedUsername");
    when(vimRepository.save(any(BaseVimInstance.class))).thenReturn(vimInstance_new);
    when(vnfdRepository.findByProjectId(anyString())).thenReturn(new ArrayList<>());
    when(vnfrRepository.findByProjectId(anyString())).thenReturn(new ArrayList<>());

    vimInstanceExp =
        (OpenstackVimInstance)
            vimManagement.update(vimInstance_new, vimInstanceExp.getId(), projectId).get();

    Assert.assertEquals(vimInstanceExp.getName(), vimInstance_new.getName());
    Assert.assertEquals(vimInstanceExp.getTenant(), vimInstance_new.getTenant());
    Assert.assertEquals(vimInstanceExp.getType(), vimInstance_new.getType());
    Assert.assertEquals(vimInstanceExp.getKeyPair(), vimInstance_new.getKeyPair());
    Assert.assertEquals(vimInstanceExp.getUsername(), vimInstance_new.getUsername());
    Assert.assertEquals(vimInstanceExp.getAuthUrl(), vimInstance_new.getAuthUrl());
    Assert.assertEquals(vimInstanceExp.getPassword(), vimInstance_new.getPassword());
    Assert.assertEquals(
        vimInstanceExp.getLocation().getName(), vimInstance_new.getLocation().getName());
    Assert.assertEquals(
        vimInstanceExp.getLocation().getLatitude(), vimInstance_new.getLocation().getLatitude());
    Assert.assertEquals(
        vimInstanceExp.getLocation().getLongitude(), vimInstance_new.getLocation().getLongitude());
    Assert.assertEquals(vimInstanceExp.getFlavours().size(), 2);
    Assert.assertEquals(vimInstanceExp.getImages().size(), 2);
    Assert.assertEquals(vimInstanceExp.getNetworks().size(), 1);
  }

  @Test
  public void nfvImageManagementAddTest()
      throws VimException, PluginException, IOException, BadRequestException, ExecutionException,
          InterruptedException {
    initMocks();
    OpenstackVimInstance vimInstance_exp = createVimInstance();
    System.out.println(vimInstance_exp);
    when(vimRepository.save(any(BaseVimInstance.class))).thenReturn(vimInstance_exp);
    OpenstackVimInstance vimInstance_new =
        (OpenstackVimInstance) vimManagement.add(vimInstance_exp, projectId).get();

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
    when(vim.queryImages(any(BaseVimInstance.class))).thenReturn(new ArrayList<>());
    when(vimBroker.getVim(anyString())).thenReturn(vim);
    //    try {
    //      Future<Set<DeploymentFlavour>> futureFlavours = mock(AsyncResult.class);
    //      Future<Set<BaseNetwork>> futureNetworks = mock(AsyncResult.class);
    //      Future<Set<BaseNfvImage>> futureImages = mock(AsyncResult.class);
    //
    // when(asyncVimManagement.updateFlavors(any(BaseVimInstance.class))).thenReturn(futureFlavours);
    //      when(asyncVimManagement.updateFlavors(any(BaseVimInstance.class)).get())
    //          .thenReturn(new HashSet<>());
    //
    // when(asyncVimManagement.updateImages(any(BaseVimInstance.class))).thenReturn(futureImages);
    //      when(asyncVimManagement.updateImages(any(BaseVimInstance.class)).get())
    //          .thenReturn(new HashSet<>());
    //      when(asyncVimManagement.updateNetworks(any(BaseVimInstance.class)))
    //          .thenReturn(futureNetworks);
    //      when(asyncVimManagement.updateNetworks(any(BaseVimInstance.class)).get())
    //          .thenReturn(new HashSet<>());
    //    } catch (InterruptedException | ExecutionException | BadRequestException e) {
    //      e.printStackTrace();
    //    }
    doNothing().when(imageRepository).delete(any(NFVImage.class));
    doNothing().when(networkRepository).delete(any(Network.class));
  }

  @Test
  public void nfvImageManagementQueryTest() {

    OpenstackVimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    when(vimRepository.findFirstByIdAndProjectId(vimInstance_exp.getId(), projectId))
        .thenReturn(vimInstance_exp);
    OpenstackVimInstance vimInstance_new =
        (OpenstackVimInstance) vimManagement.query(vimInstance_exp.getId(), projectId);
    Assert.assertEquals(vimInstance_exp.getId(), vimInstance_new.getId());
    Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
    Assert.assertEquals(vimInstance_exp.getFlavours().size(), vimInstance_new.getFlavours().size());
    Assert.assertEquals(vimInstance_exp.getImages().size(), vimInstance_new.getImages().size());
    Assert.assertEquals(vimInstance_exp.getNetworks().size(), vimInstance_new.getNetworks().size());
  }

  @Test
  public void nfvImageManagementDeleteTest() throws NotFoundException, BadRequestException {
    OpenstackVimInstance vimInstance_exp = createVimInstance();
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
    when(vimRepository.findFirstByIdAndProjectId(vimInstance_exp.getId(), projectId))
        .thenReturn(vimInstance_exp);
    vimManagement.delete(vimInstance_exp.getId(), projectId);
    when(vimRepository.findOne(vimInstance_exp.getId())).thenReturn(null);
    when(vimRepository.findFirstByIdAndProjectId(vimInstance_exp.getId(), projectId))
        .thenReturn(null);
    BaseVimInstance vimInstance_new = vimManagement.query(vimInstance_exp.getId(), projectId);
    Assert.assertNull(vimInstance_new);
  }
}
