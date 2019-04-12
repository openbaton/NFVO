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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.CostituentVNF;
import org.openbaton.catalogue.mano.common.RedundancyModel;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.nfvo.core.api.VNFFGManagement;
import org.openbaton.nfvo.repositories.VNFFGDescriptorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 20/04/15. */
public class VNFFGManagementClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();
  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @InjectMocks private VNFFGManagement vnffgManagement;

  @Mock private VNFFGDescriptorRepository vnffgDescriptorRepository;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    log.info("Starting test");
  }

  @Test
  public void vnffgManagementNotNull() {
    Assert.assertNotNull(vnffgManagement);
  }

  @Test
  public void vnffgManagementUpdateTest() {
    exception.expect(UnsupportedOperationException.class);
    VNFForwardingGraphDescriptor vnffgDescriptor_exp = createVNFFGDescriptor();
    when(vnffgDescriptorRepository.findOne(vnffgDescriptor_exp.getId()))
        .thenReturn(vnffgDescriptor_exp);

    VNFForwardingGraphDescriptor vnffgDescriptor_new = createVNFFGDescriptor();
    vnffgDescriptor_new.setVendor("UpdatedVendor");
    vnffgDescriptor_exp = vnffgManagement.update(vnffgDescriptor_new, vnffgDescriptor_exp.getId());

    assertEqualsVNFFG(vnffgDescriptor_exp, vnffgDescriptor_new);
  }

  private void assertEqualsVNFFG(
      VNFForwardingGraphDescriptor vnffgDescriptor_exp,
      VNFForwardingGraphDescriptor vnffgDescriptor_new) {
    Assert.assertEquals(vnffgDescriptor_exp.getVendor(), vnffgDescriptor_new.getVendor());
    Assert.assertEquals(vnffgDescriptor_exp.getId(), vnffgDescriptor_new.getId());
    Assert.assertEquals(
        vnffgDescriptor_exp.getDescriptor_version(), vnffgDescriptor_new.getDescriptor_version());
  }

  private VNFForwardingGraphDescriptor createVNFFGDescriptor() {
    VNFForwardingGraphDescriptor vnffgDescriptor = new VNFForwardingGraphDescriptor();
    vnffgDescriptor.setVendor("vendor");
    vnffgDescriptor.setConnection_point(new HashSet<ConnectionPoint>());
    ConnectionPoint connectionPoint = new ConnectionPoint();
    connectionPoint.setType("type");
    vnffgDescriptor.getConnection_point().add(connectionPoint);
    HashSet<CostituentVNF> constituent_vnfs = new HashSet<>();
    CostituentVNF costituentVNF = new CostituentVNF();
    costituentVNF.setAffinity("affinity");
    costituentVNF.setCapability("capability");
    costituentVNF.setNumber_of_instances(3);
    costituentVNF.setRedundancy_model(RedundancyModel.ACTIVE);
    costituentVNF.setVnf_flavour_id_reference("flavor_id");
    costituentVNF.setVnf_reference("vnf_id");
    constituent_vnfs.add(costituentVNF);
    vnffgDescriptor.setConstituent_vnfs(constituent_vnfs);
    vnffgDescriptor.setNumber_of_endpoints(2);
    vnffgDescriptor.setVersion("version");
    vnffgDescriptor.setNumber_of_virtual_links(2);
    HashSet<VirtualLinkDescriptor> dependent_virtual_link = new HashSet<>();
    VirtualLinkDescriptor virtualLinkDescriptor = new VirtualLinkDescriptor();
    virtualLinkDescriptor.setVld_security(new Security());
    virtualLinkDescriptor.setVendor("vendor");
    virtualLinkDescriptor.setTest_access(
        new HashSet<String>() {
          {
            add("test_access");
          }
        });
    virtualLinkDescriptor.setLeaf_requirement("leaf_requirement");
    virtualLinkDescriptor.setNumber_of_endpoints(1);
    virtualLinkDescriptor.setDescriptor_version("version");
    virtualLinkDescriptor.setConnectivity_type("tyxpe");
    virtualLinkDescriptor.setQos(
        new HashSet<String>() {
          {
            add("qos");
          }
        });
    virtualLinkDescriptor.setConnection(
        new HashSet<String>() {
          {
            add("connection");
          }
        });
    virtualLinkDescriptor.setRoot_requirement("root_requirement");
    dependent_virtual_link.add(virtualLinkDescriptor);
    vnffgDescriptor.setDependent_virtual_link(dependent_virtual_link);
    vnffgDescriptor.setVnffgd_security(new Security());
    return vnffgDescriptor;
  }

  @Test
  public void vnffgManagementAddTest() {
    VNFForwardingGraphDescriptor vnffgDescriptor_exp = createVNFFGDescriptor();
    when(vnffgDescriptorRepository.save(any(VNFForwardingGraphDescriptor.class)))
        .thenReturn(vnffgDescriptor_exp);
    VNFForwardingGraphDescriptor vnffgDescriptor_new = vnffgManagement.add(vnffgDescriptor_exp);

    assertEqualsVNFFG(vnffgDescriptor_exp, vnffgDescriptor_new);
  }

  @Test
  public void vnffgManagementQueryTest() {
    when(vnffgDescriptorRepository.findAll())
        .thenReturn(new ArrayList<VNFForwardingGraphDescriptor>());

    Assert.assertEquals(false, vnffgManagement.query().iterator().hasNext());

    VNFForwardingGraphDescriptor vnffgDescriptor_exp = createVNFFGDescriptor();
    when(vnffgDescriptorRepository.findOne(vnffgDescriptor_exp.getId()))
        .thenReturn(vnffgDescriptor_exp);
    VNFForwardingGraphDescriptor vnffgDescriptor_new =
        vnffgManagement.query(vnffgDescriptor_exp.getId());
    assertEqualsVNFFG(vnffgDescriptor_exp, vnffgDescriptor_new);
  }

  @Test
  public void vnffgManagementDeleteTest() {
    VNFForwardingGraphDescriptor vnffgDescriptor_exp = createVNFFGDescriptor();
    when(vnffgDescriptorRepository.findOne(vnffgDescriptor_exp.getId()))
        .thenReturn(vnffgDescriptor_exp);
    vnffgManagement.delete(vnffgDescriptor_exp.getId());
    when(vnffgDescriptorRepository.findOne(vnffgDescriptor_exp.getId())).thenReturn(null);
    VNFForwardingGraphDescriptor vnffgDescriptor_new =
        vnffgManagement.query(vnffgDescriptor_exp.getId());
    Assert.assertNull(vnffgDescriptor_new);
  }
}
