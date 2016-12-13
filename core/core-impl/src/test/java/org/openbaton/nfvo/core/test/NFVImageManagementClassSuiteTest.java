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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.nfvo.core.api.NFVImageManagement;
import org.openbaton.nfvo.repositories.ImageRepository;

/** Created by lto on 20/04/15. */
public class NFVImageManagementClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks private NFVImageManagement nfvImageManagement;

  @Mock private ImageRepository imageRepository;

  @Test
  public void nfvImageManagementNotNull() {
    Assert.assertNotNull(nfvImageManagement);
  }

  @Test
  public void nfvImageManagementUpdateTest() {
    NFVImage nfvImage_exp = createNfvImage();

    NFVImage nfvImage_new = createNfvImage();
    nfvImage_new.setName("UpdatedName");
    nfvImage_new.setMinRam(2046);
    when(imageRepository.save(any(NFVImage.class))).thenReturn(nfvImage_new);
    nfvImage_exp = nfvImageManagement.update(nfvImage_new, nfvImage_exp.getId());

    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementCopyTest() {
    exception.expect(UnsupportedOperationException.class);
    nfvImageManagement.copy();
  }

  @Test
  public void nfvImageManagementAddTest() {
    NFVImage nfvImage_exp = createNfvImage();
    when(imageRepository.save(any(NFVImage.class))).thenReturn(nfvImage_exp);
    NFVImage nfvImage_new = nfvImageManagement.add(nfvImage_exp);

    Assert.assertEquals(nfvImage_exp.getId(), nfvImage_new.getId());
    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementQueryTest() {
    when(imageRepository.findAll()).thenReturn(new ArrayList<NFVImage>());

    Assert.assertEquals(false, nfvImageManagement.query().iterator().hasNext());

    NFVImage nfvImage_exp = createNfvImage();
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
    NFVImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
    Assert.assertEquals(nfvImage_exp.getId(), nfvImage_new.getId());
    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementDeleteTest() {
    NFVImage nfvImage_exp = createNfvImage();
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
    nfvImageManagement.delete(nfvImage_exp.getId());
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(null);
    NFVImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
    Assert.assertNull(nfvImage_new);
  }

  private NFVImage createNfvImage() {
    NFVImage nfvImage = new NFVImage();
    nfvImage.setName("image_name");
    nfvImage.setExtId("ext_id");
    nfvImage.setMinCPU("1");
    nfvImage.setMinRam(1024);
    return nfvImage;
  }

  private NetworkServiceDescriptor createNetworkServiceDescriptor() {
    final NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
    nsd.setVendor("FOKUS");
    Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<>();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        new VirtualNetworkFunctionDescriptor();
    virtualNetworkFunctionDescriptor.setMonitoring_parameter(
        new HashSet<String>() {
          {
            add("monitor1");
            add("monitor2");
            add("monitor3");
          }
        });
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
            HighAvailability highAvailability = new HighAvailability();
            highAvailability.setGeoRedundancy(false);
            highAvailability.setRedundancyScheme("1:N");
            highAvailability.setResiliencyLevel(ResiliencyLevel.ACTIVE_STANDBY_STATELESS);
            vdu.setHigh_availability(highAvailability);
            vdu.setComputation_requirement("high_requirements");
            VimInstance vimInstance = new VimInstance();
            vimInstance.setName("vim_instance");
            vimInstance.setType("test");
            add(vdu);
          }
        });
    virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
    nsd.setVnfd(virtualNetworkFunctionDescriptors);
    return nsd;
  }

  private VimInstance createVimInstance() {
    VimInstance vimInstance = new VimInstance();
    vimInstance.setName("vim_instance");
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
