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

package org.openbaton.nfvo.repositories.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;

//import GenericRepository;

/**
 * Created by lto on 30/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = {"timezone = GMT", "port: 4242"})
public class RepositoriesClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();
  private JdbcTemplate jdbcTemplate;
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private ConfigurableApplicationContext ctx;

  @Autowired private NetworkServiceDescriptorRepository nsdRepository;

  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Test
  public void repositoryNotNullTest() {
    Assert.assertNotNull(nsdRepository);
  }

  @Test
  public void createEntityTest() {
    NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

    int count = countRowsInTable(jdbcTemplate, "NETWORK_SERVICE_DESCRIPTOR");

    nsd = nsdRepository.save(nsd);

    Assert.assertNotNull(nsd);
    Assert.assertNotNull(nsd.getId());
    log.debug("id is: " + nsd.getId());

    Assert.assertEquals((count + 1), countRowsInTable(jdbcTemplate, "NETWORK_SERVICE_DESCRIPTOR"));

    // Clean
    nsdRepository.delete(nsd);
  }

  @Test
  @Transactional
  public void findEntityTest() {
    NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

    nsd = nsdRepository.save(nsd);

    Assert.assertNotNull(nsd);
    Assert.assertNotNull(nsd.getId());
    log.debug("id is: " + nsd.getId());

    Iterable<NetworkServiceDescriptor> all = nsdRepository.findAll();
    log.debug("" + all);
    for (NetworkServiceDescriptor n : all) {
      log.debug(n.toString());
    }

    NetworkServiceDescriptor new_nsd = null;
    new_nsd = nsdRepository.findFirstById(nsd.getId());

    Assert.assertNotNull(new_nsd);
    Assert.assertNotNull(new_nsd.getId());

    // Clean
    nsdRepository.delete(new_nsd);
  }

  @Test
  public void nsdRepositoryFindTest() {
    int i = 0;
    for (NetworkServiceDescriptor networkServiceDescriptor : nsdRepository.findAll()) i++;
    Assert.assertEquals(0, i);
  }

  @Test
  public void nsdRepositoryMergeTest() {
    NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();
    NetworkServiceDescriptor nsd_new;
    nsd.setVendor("0");
    nsd = nsdRepository.save(nsd);

    for (int i = 0; i < 10; i++) {
      nsd.setVendor("" + i);
      int version = nsd.getHb_version();
      nsd_new = nsdRepository.save(nsd);
      Assert.assertEquals(nsd_new.getVendor(), "" + i);
      int new_version = nsd_new.getHb_version();
      log.warn("Expected " + (1 + version) + " but was " + new_version);
      // Assert.assertEquals(new_version, (version));
      nsd = nsd_new;
    }

    // Clean
    nsdRepository.delete(nsd);
  }

  @Test
  public void nsdRepositoryPersistTest() {
    NetworkServiceDescriptor nsd = createNetworkServiceDescriptor();

    nsd = nsdRepository.save(nsd);

    String id = nsd.getId();

    Assert.assertNotNull(id);

    NetworkServiceDescriptor nsd_new = null;
    nsd_new = nsdRepository.findOne(id);

    Assert.assertEquals(nsd.getId(), nsd_new.getId());
    Assert.assertEquals(nsd.getVersion(), nsd_new.getVersion());
    Assert.assertEquals(nsd.getVendor(), nsd_new.getVendor());
    for (int i = 0; i < nsd.getVnfd().size(); i++) {
      Assert.assertEquals(
          ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i]).getId(),
          ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i]).getId());
      Assert.assertEquals(
          ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i]).getVersion(),
          ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i]).getVersion());
      //            for (int k = 0; k < ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i]).getMonitoring_parameter().size(); k++) {
      //                Assert.assertEquals(
      //                        ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i]).getMonitoring_parameter().get(k),
      //                        ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i]).getMonitoring_parameter().get(k));
      //            }
      for (int j = 0;
          j < ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i]).getVdu().size();
          j++) {
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getId(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getId());
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getVersion(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getVersion());
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getComputation_requirement(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getComputation_requirement());
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .getRedundancyScheme(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .getRedundancyScheme());
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .getResiliencyLevel(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .getResiliencyLevel());
        Assert.assertEquals(
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .isGeoRedundancy(),
            ((VirtualDeploymentUnit)
                    ((VirtualNetworkFunctionDescriptor) nsd_new.getVnfd().toArray()[i])
                            .getVdu()
                            .toArray()[
                        j])
                .getHigh_availability()
                .isGeoRedundancy());
      }
    }

    // Clean
    nsdRepository.delete(nsd);

    NetworkServiceDescriptor nsd_null = null;

    nsd_null = nsdRepository.findFirstById(id);
    Assert.assertNull(nsd_null);
  }

  private NetworkServiceDescriptor createNetworkServiceDescriptor() {
    NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
    nsd.setVendor("FOKUS");
    Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<>();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        new VirtualNetworkFunctionDescriptor();
    virtualNetworkFunctionDescriptor.setType("test");
    virtualNetworkFunctionDescriptor.setMonitoring_parameter(
        new HashSet<String>() {
          {
            add("monitor1");
            add("monitor2");
            add("monitor3");
          }
        });
    final VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
    vdu.setVnfc(new HashSet<VNFComponent>());
    vdu.setVnfc_instance(new HashSet<VNFCInstance>());
    vdu.setVimInstanceName(Collections.singletonList("test"));
    HighAvailability highAvailability = new HighAvailability();
    highAvailability.setGeoRedundancy(false);
    highAvailability.setRedundancyScheme("1:N");
    highAvailability.setResiliencyLevel(ResiliencyLevel.ACTIVE_STANDBY_STATELESS);
    vdu.setHigh_availability(highAvailability);
    vdu.setComputation_requirement("high_requirements");
    virtualNetworkFunctionDescriptor.setVdu(
        new HashSet<VirtualDeploymentUnit>() {
          {
            add(vdu);
          }
        });
    virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
    nsd.setVnfd(virtualNetworkFunctionDescriptors);
    return nsd;
  }
}
