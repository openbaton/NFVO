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
import static org.openbaton.nfvo.core.test.TestUtils.createMaxQuota;
import static org.openbaton.nfvo.core.test.TestUtils.createMinQuota;
import static org.openbaton.nfvo.core.test.TestUtils.createVimInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.core.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

/** Created by lto on 20/04/15. */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VNFLifecycleOperationGrantingClassSuiteTest.class)
@Configuration
public class VNFLifecycleOperationGrantingClassSuiteTest {

  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @InjectMocks private VNFLifecycleOperationGranting vnfLifecycleOperationGranting;

  @Mock private VimRepository vimInstanceRepository;
  @Mock private VimBroker vimBroker;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(vnfLifecycleOperationGranting, "isQuotaCheckEnabled", true);
    ReflectionTestUtils.setField(
        vnfLifecycleOperationGranting, "failingQuotaCheckOnException", true);
    log.info("Starting test");
  }

  @Test
  public void vnfLifecycleOperationGrantingTest() throws VimException, PluginException {
    VirtualNetworkFunctionRecord vnfr = createVirtualNetworkFunctionRecord();
    Map<String, BaseVimInstance> granted;

    when(vimInstanceRepository.findByProjectId(anyString()))
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(createVimInstance());
              }
            });
    when(vimBroker.getLeftQuota(any(BaseVimInstance.class))).thenReturn(createMaxQuota());

    granted = vnfLifecycleOperationGranting.grantLifecycleOperation(vnfr);
    log.debug(granted.size() + " == " + vnfr.getVdu().size());
    Assert.assertTrue(granted.size() == vnfr.getVdu().size());

    when(vimBroker.getLeftQuota(any(BaseVimInstance.class))).thenReturn(createMinQuota());
    granted = vnfLifecycleOperationGranting.grantLifecycleOperation(vnfr);
    Assert.assertFalse(granted.size() == vnfr.getVdu().size());
  }

  private VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord() {
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
    virtualNetworkFunctionRecord.setMonitoring_parameter(
        new HashSet<String>() {
          {
            add("monitor1");
            add("monitor2");
            add("monitor3");
          }
        });
    VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
    vdf.setExtId("mocked_vdu_ext_id");
    vdf.setFlavour_key("mocked_flavor_name_1");
    virtualNetworkFunctionRecord.setName("mocked_vnfr");
    virtualNetworkFunctionRecord.setDeployment_flavour_key(vdf.getFlavour_key());
    virtualNetworkFunctionRecord.setVdu(new HashSet<VirtualDeploymentUnit>());
    BaseVimInstance vimInstance = createVimInstance();
    for (int i = 1; i <= 3; i++) {
      virtualNetworkFunctionRecord.getVdu().add(createVDU(i, vimInstance));
    }
    return virtualNetworkFunctionRecord;
  }

  private VirtualDeploymentUnit createVDU(int suffix, BaseVimInstance vimInstance) {
    VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
    vdu.setId("" + Math.random() * 100000);
    vdu.setHostname("mocked_vdu_hostname_" + suffix);
    HighAvailability highAvailability = new HighAvailability();
    highAvailability.setRedundancyScheme("1:N");
    highAvailability.setResiliencyLevel(ResiliencyLevel.ACTIVE_STANDBY_STATELESS);
    vdu.setHigh_availability(highAvailability);
    vdu.setVm_image(
        new HashSet<String>() {
          {
            add("mocked_image");
          }
        });
    vdu.setComputation_requirement("high_requirements");
    HashSet<VNFComponent> vnfComponents = new HashSet<>();
    vnfComponents.add(new VNFComponent());
    vnfComponents.add(new VNFComponent());
    vdu.setVnfc(vnfComponents);
    HashSet<VNFCInstance> vnfc_instance = new HashSet<>();
    vnfc_instance.add(new VNFCInstance());
    vdu.setVnfc_instance(vnfc_instance);
    vdu.setLifecycle_event(new HashSet<>());
    vdu.setMonitoring_parameter(new HashSet<>());
    Set<String> vimInstanceName = new LinkedHashSet<>();
    vimInstanceName.add(vimInstance.getName());
    vdu.setVimInstanceName(vimInstanceName);
    return vdu;
  }
}
