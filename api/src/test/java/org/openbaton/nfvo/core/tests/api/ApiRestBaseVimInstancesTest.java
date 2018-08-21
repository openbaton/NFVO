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

package org.openbaton.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.api.admin.RestVimInstances;
import org.openbaton.nfvo.core.api.ComponentManager;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.AsyncResult;

public class ApiRestBaseVimInstancesTest {

  @InjectMocks RestVimInstances restVimInstances;
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Mock private VimManagement mock;
  @Mock private ComponentManager componentManager;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findAllVimInstances()
      throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
          NoSuchPaddingException, IllegalBlockSizeException, BadFormatException {
    when(mock.queryByProjectId("pi")).thenReturn(new ArrayList<>());
    assertEquals(mock.queryByProjectId("pi"), restVimInstances.findAll("pi", "Bearer token"));
  }

  @Test
  public void createVimInstance()
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException, ExecutionException, InterruptedException {
    OpenstackVimInstance datacenter = new OpenstackVimInstance();
    datacenter.setId("123");
    datacenter.setName("DC-1");
    datacenter.setType("OpenStack");
    datacenter.setUsername("datacenter_test");
    datacenter.setTenant("tenant");
    datacenter.setKeyPair("keypair");
    datacenter.setPassword("");
    when(mock.add(any(datacenter.getClass()), anyString()))
        .thenReturn(new AsyncResult<>(datacenter));
    log.info("" + restVimInstances.create(datacenter, "pi"));
    BaseVimInstance datacenter2 = restVimInstances.create(datacenter, "pi");
    assertEquals(datacenter, datacenter2);
  }

  @Test
  public void findByIdVimInstance()
      throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
          NoSuchPaddingException, IllegalBlockSizeException, NotFoundException, BadFormatException {
    OpenstackVimInstance datacenter = new OpenstackVimInstance();
    datacenter.setId("123");
    datacenter.setName("DC-1");
    datacenter.setType("OpenStack");
    datacenter.setName("datacenter_test");
    when(mock.query(anyString(), anyString())).thenReturn(datacenter);
    assertEquals(datacenter, restVimInstances.findById(datacenter.getId(), "pi", "Bearer token"));
  }

  @Test
  public void updateVimInstance()
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException, NotFoundException, ExecutionException, InterruptedException {
    OpenstackVimInstance datacenter = new OpenstackVimInstance();
    datacenter.setId("123");
    datacenter.setName("DC-1");
    datacenter.setType("OpenStack");
    datacenter.setName("datacenter_test");
    when(mock.update(any(datacenter.getClass()), anyString(), anyString()))
        .thenReturn(new AsyncResult<>(datacenter));
    assertEquals(datacenter, restVimInstances.update(datacenter, datacenter.getId(), "pi"));
  }

  @Test
  public void deleteVimInstance() throws NotFoundException, BadRequestException {
    mock.delete("123", "pi");
    restVimInstances.delete("123", "pi");
  }
}
