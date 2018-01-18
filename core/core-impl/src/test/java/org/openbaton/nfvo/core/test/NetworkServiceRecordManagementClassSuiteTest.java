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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.openbaton.nfvo.core.test.TestUtils.createQuota;
import static org.openbaton.nfvo.core.test.TestUtils.createVimInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.QuotaExceededException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.core.api.ConfigurationManagement;
import org.openbaton.nfvo.core.api.NetworkServiceRecordManagement;
import org.openbaton.nfvo.core.api.VimManagement;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.junit4.SpringRunner;

/** Created by lto on 20/04/15. */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NetworkServiceRecordManagementClassSuiteTest.class)
@org.springframework.context.annotation.Configuration
@SuppressWarnings({"unsafe", "unchecked"})
public class NetworkServiceRecordManagementClassSuiteTest {

  private static final String projectId = "project-id";

  @InjectMocks private NetworkServiceRecordManagement nsrManagement;
  @Rule public ExpectedException exception = ExpectedException.none();

  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @Mock private ConfigurationManagement configurationManagement;
  @Mock private VnfPackageRepository vnfPackageRepository;
  @Mock private VimManagement vimManagement;
  @Mock private VimBroker vimBroker;
  @Mock private VimRepository vimRepository;
  @Mock private NetworkServiceDescriptorRepository nsdRepository;
  @Mock private NetworkServiceRecordRepository nsrRepository;
  @Mock private ResourceManagement resourceManagement;
  @Mock private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Mock private Vim vim;
  @Mock private VNFLifecycleOperationGranting vnfLifecycleOperationGranting;
  @Mock private NSDUtils nsdUtils;
  @Mock private ConfigurationRepository configurationRepository;
  @Mock private VnfmManager vnfmManager;
  @Mock private EventDispatcher publisher;

  @Before
  public void init()
      throws ExecutionException, InterruptedException, VimDriverException, VimException,
          PluginException, BadRequestException, IOException, AlreadyExistingException {
    MockitoAnnotations.initMocks(this);
    BaseVimInstance vimInstance = createVimInstance();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionRecord =
        createVirtualNetworkFunctionDescriptor();
    when(resourceManagement.allocate(
            any(VirtualDeploymentUnit.class),
            any(VirtualNetworkFunctionRecord.class),
            any(BaseVimInstance.class),
            anyString(),
            anySet()))
        .thenReturn(new AsyncResult<List<String>>(new ArrayList<>()));
    when(vimBroker.getVim(anyString())).thenReturn(vim);
    when(vimBroker.getLeftQuota(any(BaseVimInstance.class))).thenReturn(createQuota());
    VNFCInstance vnfcInstance = new VNFCInstance();
    when(vim.allocate(
            any(BaseVimInstance.class),
            any(VirtualDeploymentUnit.class),
            any(VirtualNetworkFunctionRecord.class),
            any(VNFComponent.class),
            anyString(),
            anyMap(),
            anySet()))
        .thenReturn(new AsyncResult<>(vnfcInstance));
    Map<String, BaseVimInstance> res = new HashMap<>();
    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      res.put(vdu.getId(), vimInstance);
    }
    when(vnfLifecycleOperationGranting.grantLifecycleOperation(
            any(VirtualNetworkFunctionRecord.class)))
        .thenReturn(res);

    when(vnfmManagerEndpointRepository.findAll())
        .thenReturn(
            new ArrayList<VnfmManagerEndpoint>() {
              {
                VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
                vnfmManagerEndpoint.setEndpoint("test");
                vnfmManagerEndpoint.setType("test");
                vnfmManagerEndpoint.setActive(true);
                vnfmManagerEndpoint.setEnabled(true);
                add(vnfmManagerEndpoint);
              }
            });
    when(vimManagement.refresh(any(BaseVimInstance.class), anyBoolean()))
        .thenReturn(new AsyncResult<>(vimInstance));
    when(vnfPackageRepository.findFirstById(anyString())).thenReturn(createVNFPackage());
    log.info("Starting test");
  }

  private VNFPackage createVNFPackage() {
    VNFPackage vnfPackage = new VNFPackage();
    LinkedHashSet<String> vimTypes = new LinkedHashSet<>();
    vimTypes.add("test");
    vnfPackage.setVimTypes(vimTypes);
    return vnfPackage;
  }

  @Test
  public void nsrManagementNotNull() {
    Assert.assertNotNull(nsrManagement);
  }

  @Test
  public void nsrManagementQueryTest() throws NotFoundException {
    when(nsrRepository.findAll()).thenReturn(new ArrayList<NetworkServiceRecord>());
    Iterable<NetworkServiceRecord> nsds = nsrManagement.query();
    Assert.assertEquals(nsds.iterator().hasNext(), false);
    final NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
    when(nsrRepository.findAll())
        .thenReturn(
            new ArrayList<NetworkServiceRecord>() {
              {
                add(nsd_exp);
              }
            });
    nsds = nsrManagement.query();
    Assert.assertEquals(nsds.iterator().hasNext(), true);

    when(nsrRepository.findOne(nsd_exp.getId())).thenReturn(nsd_exp);
    assertEqualsNSR(nsd_exp);
  }

  @Test
  public void nsrManagementDeleteTest()
      throws VimException, InterruptedException, ExecutionException, NamingException,
          NotFoundException, WrongStatusException, PluginException, BadFormatException {
    NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
    when(resourceManagement.release(any(VirtualDeploymentUnit.class), any(VNFCInstance.class)))
        .thenReturn(new AsyncResult<Void>(null));
    when(nsrRepository.findFirstByIdAndProjectId(nsd_exp.getId(), projectId)).thenReturn(nsd_exp);
    Configuration system = new Configuration();
    system.setConfigurationParameters(new HashSet<>());
    ConfigurationParameter configurationParameter = new ConfigurationParameter();
    configurationParameter.setConfKey("delete-on-all-status");
    configurationParameter.setValue("true");
    when(configurationManagement.queryByName("system")).thenReturn(system);
    nsrManagement.delete(nsd_exp.getId(), projectId);
  }

  @Test
  public void nsrManagementOnboardTest1()
      throws NotFoundException, InterruptedException, ExecutionException, NamingException,
          VimException, VimDriverException, BadFormatException, QuotaExceededException,
          PluginException, MissingParameterException, BadRequestException, IOException,
          AlreadyExistingException {
    final NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
    when(nsrRepository.save(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });

    when(nsrRepository.saveCascade(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });

    when(vimRepository.findByProjectIdAndName(anyString(), anyString()))
        .thenReturn(createVimInstance());

    when(vimRepository.findAll())
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(createVimInstance());
              }
            });

    when(vnfmManagerEndpointRepository.findAll())
        .thenReturn(
            new ArrayList<VnfmManagerEndpoint>() {
              {
                VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
                vnfmManagerEndpoint.setEndpoint("test");
                vnfmManagerEndpoint.setType("test");
                vnfmManagerEndpoint.setActive(true);
                vnfmManagerEndpoint.setEnabled(true);
                add(vnfmManagerEndpoint);
              }
            });
    nsrManagement.onboard(nsd_exp, projectId, null, null, null, null);
  }

  @Test
  public void nsrManagementOnboardTest2()
      throws NotFoundException, InterruptedException, ExecutionException, NamingException,
          VimException, VimDriverException, BadFormatException, QuotaExceededException,
          PluginException, MissingParameterException, BadRequestException, IOException,
          AlreadyExistingException {
    /** Initial settings */
    when(vimRepository.findByProjectIdAndName(anyString(), anyString()))
        .thenReturn(createVimInstance());
    NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();

    when(nsrRepository.save(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });
    when(nsrRepository.saveCascade(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });

    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        networkServiceDescriptor.getVnfd().iterator().next();
    LifecycleEvent event = new LifecycleEvent();
    event.setEvent(Event.INSTANTIATE);
    event.setLifecycle_events(new ArrayList<>());
    event.getLifecycle_events().add("command_1");
    virtualNetworkFunctionDescriptor.getLifecycle_event().add(event);
    final BaseVimInstance vimInstance = createVimInstance();
    when(vnfmManagerEndpointRepository.findAll())
        .thenReturn(
            new ArrayList<VnfmManagerEndpoint>() {
              {
                VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
                vnfmManagerEndpoint.setEndpoint("test");
                vnfmManagerEndpoint.setType("test");
                vnfmManagerEndpoint.setActive(true);
                vnfmManagerEndpoint.setEnabled(true);
                add(vnfmManagerEndpoint);
              }
            });
    when(nsdRepository.findFirstByIdAndProjectId(anyString(), eq(projectId)))
        .thenReturn(networkServiceDescriptor);
    when(vimRepository.findAll())
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(vimInstance);
              }
            });
    when(vimRepository.findByProjectId(anyString()))
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(createVimInstance());
              }
            });
    /** Real Method */
    nsrManagement.onboard(networkServiceDescriptor.getId(), projectId, null, null, null, null);
  }

  @Test
  public void nsrManagementOnboardTest3()
      throws NotFoundException, InterruptedException, ExecutionException, NamingException,
          VimException, VimDriverException, BadFormatException, QuotaExceededException,
          PluginException, MissingParameterException, BadRequestException, IOException,
          AlreadyExistingException {
    /** Initial settings */
    when(vimRepository.findByProjectIdAndName(anyString(), anyString()))
        .thenReturn(createVimInstance());
    when(nsrRepository.save(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });
    when(nsrRepository.saveCascade(any(NetworkServiceRecord.class)))
        .thenAnswer(
            (Answer<NetworkServiceRecord>)
                invocation -> {
                  Object[] args = invocation.getArguments();
                  return (NetworkServiceRecord) args[0];
                });
    NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();
    when(nsdRepository.findFirstByIdAndProjectId(anyString(), eq(projectId)))
        .thenReturn(networkServiceDescriptor);
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        networkServiceDescriptor.getVnfd().iterator().next();
    LifecycleEvent event = new LifecycleEvent();
    event.setEvent(Event.ALLOCATE);
    event.setLifecycle_events(new ArrayList<>());
    event.getLifecycle_events().add("command_1");
    virtualNetworkFunctionDescriptor.getLifecycle_event().add(event);
    final BaseVimInstance vimInstance = createVimInstance();
    when(vimRepository.findAll())
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(vimInstance);
              }
            });
    when(vimRepository.findByProjectId(anyString()))
        .thenReturn(
            new ArrayList<BaseVimInstance>() {
              {
                add(vimInstance);
              }
            });

    when(vnfmManagerEndpointRepository.findAll())
        .thenReturn(
            new ArrayList<VnfmManagerEndpoint>() {
              {
                VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
                vnfmManagerEndpoint.setEndpoint("test");
                vnfmManagerEndpoint.setType("test");
                vnfmManagerEndpoint.setActive(true);
                vnfmManagerEndpoint.setEnabled(true);
                add(vnfmManagerEndpoint);
              }
            });

    nsrManagement.onboard(networkServiceDescriptor.getId(), projectId, null, null, null, null);
  }

  @Test
  public void nsrManagementUpdateTest() throws NotFoundException {
    final NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
    when(nsrRepository.findOne(nsd_exp.getId())).thenReturn(nsd_exp);
    when(nsrRepository.findFirstByIdAndProjectId(nsd_exp.getId(), projectId)).thenReturn(nsd_exp);
    NetworkServiceRecord new_nsr = createNetworkServiceRecord();
    new_nsr.setName("UpdatedName");
    nsrManagement.update(new_nsr, nsd_exp.getId(), projectId);
    new_nsr.setId(nsd_exp.getId());
    assertEqualsNSR(new_nsr);
  }

  private void assertEqualsNSR(NetworkServiceRecord nsr_exp)
      throws NoResultException, NotFoundException {
    when(nsrRepository.findFirstByIdAndProjectId(nsr_exp.getId(), projectId)).thenReturn(nsr_exp);
    NetworkServiceRecord networkServiceRecord = nsrManagement.query(nsr_exp.getId(), projectId);
    Assert.assertEquals(nsr_exp.getId(), networkServiceRecord.getId());
    Assert.assertEquals(nsr_exp.getName(), networkServiceRecord.getName());
    Assert.assertEquals(nsr_exp.getVendor(), networkServiceRecord.getVendor());
    Assert.assertEquals(nsr_exp.getVersion(), networkServiceRecord.getVersion());
  }

  private NetworkServiceDescriptor createNetworkServiceDescriptor() {
    final NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
    nsd.setVendor("FOKUS");
    nsd.setName("TestNSD");
    nsd.setMonitoring_parameter(new HashSet<String>());
    nsd.getMonitoring_parameter().add("monitor1");
    nsd.getMonitoring_parameter().add("monitor2");
    nsd.getMonitoring_parameter().add("monitor3");
    nsd.setProjectId(projectId);
    //nsd.setLifecycle_event(new HashSet<LifecycleEvent>());
    nsd.setPnfd(new HashSet<PhysicalNetworkFunctionDescriptor>());
    nsd.setVnffgd(new HashSet<VNFForwardingGraphDescriptor>());
    nsd.setVld(new HashSet<VirtualLinkDescriptor>());
    nsd.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
    nsd.setVnf_dependency(new HashSet<VNFDependency>());
    Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<>();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor1 =
        createVirtualNetworkFunctionDescriptor();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor2 =
        createVirtualNetworkFunctionDescriptor();
    virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor1);
    virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor2);

    nsd.setVnfd(virtualNetworkFunctionDescriptors);

    VNFDependency vnfDependency = new VNFDependency();
    vnfDependency.setSource(virtualNetworkFunctionDescriptor1.getName());
    vnfDependency.setTarget(virtualNetworkFunctionDescriptor2.getName());
    vnfDependency.setParameters(new HashSet<String>());
    nsd.getVnf_dependency().add(vnfDependency);

    return nsd;
  }

  private VirtualNetworkFunctionDescriptor createVirtualNetworkFunctionDescriptor() {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        new VirtualNetworkFunctionDescriptor();
    virtualNetworkFunctionDescriptor.setType("test");
    virtualNetworkFunctionDescriptor.setProjectId(projectId);
    virtualNetworkFunctionDescriptor.setEndpoint("test");
    virtualNetworkFunctionDescriptor.setName("" + ((int) (Math.random() * 10000)));
    virtualNetworkFunctionDescriptor.setMonitoring_parameter(new HashSet<String>());
    virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor1");
    virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor2");
    virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor3");
    virtualNetworkFunctionDescriptor.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
    virtualNetworkFunctionDescriptor.setConnection_point(new HashSet<ConnectionPoint>());
    virtualNetworkFunctionDescriptor.setVirtual_link(new HashSet<InternalVirtualLink>());
    virtualNetworkFunctionDescriptor.setLifecycle_event(new HashSet<LifecycleEvent>());

    virtualNetworkFunctionDescriptor.setDeployment_flavour(
        new HashSet<VNFDeploymentFlavour>() {
          {
            VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
            vdf.setExtId("ext_id");
            vdf.setFlavour_key("flavor_name");
            add(vdf);
          }
        });
    virtualNetworkFunctionDescriptor.setVdu(
        new HashSet<VirtualDeploymentUnit>() {
          {
            VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
            vdu.setVm_image(
                new HashSet<String>() {
                  {
                    add("mocked_image");
                  }
                });
            HighAvailability highAvailability = new HighAvailability();
            highAvailability.setRedundancyScheme("1:N");
            highAvailability.setResiliencyLevel(ResiliencyLevel.ACTIVE_STANDBY_STATELESS);
            vdu.setHigh_availability(highAvailability);
            vdu.setComputation_requirement("high_requirements");
            vdu.setVnfc(new HashSet<VNFComponent>());
            vdu.setLifecycle_event(new HashSet<LifecycleEvent>());
            vdu.setMonitoring_parameter(
                new HashSet<String>() {
                  {
                    add("monitor1");
                    add("monitor2");
                    add("monitor3");
                  }
                });
            BaseVimInstance vimInstance = new OpenstackVimInstance();
            vimInstance.setName("vim_instance");
            vimInstance.setType("test");
            Set<String> vimInstanceNames = new LinkedHashSet<>();
            vimInstanceNames.add("vim_instance");
            vdu.setVimInstanceName(vimInstanceNames);
            add(vdu);
          }
        });
    return virtualNetworkFunctionDescriptor;
  }

  private NetworkServiceRecord createNetworkServiceRecord() {
    final NetworkServiceRecord nsr = new NetworkServiceRecord();
    nsr.setVendor("FOKUS");
    nsr.setProjectId(projectId);
    nsr.setStatus(Status.ACTIVE);
    nsr.setMonitoring_parameter(new HashSet<String>());
    nsr.getMonitoring_parameter().add("monitor1");
    nsr.getMonitoring_parameter().add("monitor2");
    nsr.getMonitoring_parameter().add("monitor3");
    HashSet<VirtualNetworkFunctionRecord> virtualNetworkFunctionRecords = new HashSet<>();
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
    virtualNetworkFunctionRecord.setName("mocked_vnfr_name");
    virtualNetworkFunctionRecord.setType("test");
    virtualNetworkFunctionRecord.setMonitoring_parameter(
        new HashSet<String>() {
          {
            add("monitor1");
            add("monitor2");
            add("monitor3");
          }
        });
    VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
    vdf.setExtId("ext_id");
    vdf.setFlavour_key("flavor_name");
    virtualNetworkFunctionRecord.setDeployment_flavour_key(vdf.getFlavour_key());
    virtualNetworkFunctionRecord.setVdu(
        new HashSet<VirtualDeploymentUnit>() {
          {
            VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
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
            vdu.setVnfc(new HashSet<VNFComponent>());
            vdu.setVnfc_instance(new HashSet<VNFCInstance>());
            vdu.setLifecycle_event(new HashSet<LifecycleEvent>());
            vdu.setMonitoring_parameter(
                new HashSet<String>() {
                  {
                    add("monitor1");
                    add("monitor2");
                    add("monitor3");
                  }
                });
            BaseVimInstance vimInstance = new OpenstackVimInstance();
            vimInstance.setName("vim_instance");
            vimInstance.setType("test");
            add(vdu);
          }
        });
    virtualNetworkFunctionRecords.add(virtualNetworkFunctionRecord);
    nsr.setVnfr(virtualNetworkFunctionRecords);
    return nsr;
  }
}
