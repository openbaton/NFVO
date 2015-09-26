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

package org.project.openbaton.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.common.HighAvailability;
import org.project.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.Network;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.exceptions.BadFormatException;
import org.project.openbaton.exceptions.NetworkServiceIntegrityException;
import org.project.openbaton.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.project.openbaton.nfvo.repositories.VimRepository;
import org.project.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
public class NetworkServiceDescriptorManagementClassSuiteTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private VimRepository vimRepository;
    @Mock
    private NetworkServiceDescriptorRepository nsdRepository;
    @Mock
    private NSDUtils nsdUtils;
    @Mock
    private VnfmEndpointRepository vnfmManagerEndpointRepository;

    private Logger log = LoggerFactory.getLogger(ApplicationTest.class);
    @InjectMocks
    private NetworkServiceDescriptorManagement nsdManagement;

    @AfterClass
    public static void shutdown() {
        // TODO Teardown to avoid exceptions during test shutdown
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
        when(nsdRepository.save(any(NetworkServiceDescriptor.class))).thenReturn(nsd_exp);

        when(vnfmManagerEndpointRepository.findAll()).thenReturn(new ArrayList<VnfmManagerEndpoint>() {{
            VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
            vnfmManagerEndpoint.setEndpoint("test");
            vnfmManagerEndpoint.setType("test");
            add(vnfmManagerEndpoint);
        }});
    }

    @Test
    public void nsdManagementNotNull() {
        Assert.assertNotNull(nsdManagement);
    }

    @Test
    public void nsdManagementEnableTest() throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {
        NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
        when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
            add(createVimInstance());
        }});

        nsdManagement.onboard(nsd_exp);
        when(nsdRepository.findFirstById(anyString())).thenReturn(nsd_exp);
        Assert.assertTrue(nsdManagement.enable(nsd_exp.getId()));
        Assert.assertTrue(nsd_exp.isEnabled());
        nsdManagement.delete(nsd_exp.getId());
    }

    @Test
    public void nsdManagementDisableTest() throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {
        NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
        nsd_exp.setEnabled(true);
        when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
            add(createVimInstance());
        }});

        nsdManagement.onboard(nsd_exp);
        when(nsdRepository.findFirstById(anyString())).thenReturn(nsd_exp);
        Assert.assertFalse(nsdManagement.disable(nsd_exp.getId()));
        Assert.assertFalse(nsd_exp.isEnabled());
        nsdManagement.delete(nsd_exp.getId());
    }

    ;

    @Test
    public void nsdManagementQueryTest() {
        when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
        Iterable<NetworkServiceDescriptor> nsds = nsdManagement.query();
        Assert.assertEquals(nsds.iterator().hasNext(), false);
        final NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
        when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>() {{
            add(nsd_exp);
        }});
        nsds = nsdManagement.query();

        Assert.assertEquals(nsds.iterator().hasNext(), true);
        nsdManagement.delete(nsd_exp.getId());
    }

    @Test
    public void nsdManagementOnboardTest() throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {

        NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();

        when(vnfmManagerEndpointRepository.findAll()).thenReturn(new ArrayList<VnfmManagerEndpoint>());
        exception.expect(NotFoundException.class);
        nsdManagement.onboard(nsd_exp);

        when(vnfmManagerEndpointRepository.findAll()).thenReturn(new ArrayList<VnfmManagerEndpoint>() {{
            VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
            vnfmManagerEndpoint.setEndpoint("test");
            vnfmManagerEndpoint.setType("test");
            add(vnfmManagerEndpoint);
        }});

        when(nsdRepository.save(nsd_exp)).thenReturn(nsd_exp);
        exception = ExpectedException.none();
        nsdManagement.onboard(nsd_exp);
        assertEqualsNSD(nsd_exp);
    }

    @Test
    public void nsdManagementUpdateTest() throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {
        when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
        NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();

        when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
            add(createVimInstance());
        }});

        nsdManagement.onboard(nsd_exp);
        when(nsdRepository.findOne(nsd_exp.getId())).thenReturn(nsd_exp);

        NetworkServiceDescriptor new_nsd = createNetworkServiceDescriptor();
        new_nsd.setName("UpdatedName");
        nsdManagement.update(new_nsd);

        new_nsd.setId(nsd_exp.getId());

        assertEqualsNSD(new_nsd);

        nsdManagement.delete(nsd_exp.getId());
    }

    private void assertEqualsNSD(NetworkServiceDescriptor nsd_exp) throws NoResultException {
        when(nsdRepository.findFirstById(nsd_exp.getId())).thenReturn(nsd_exp);
        NetworkServiceDescriptor nsd = nsdManagement.query(nsd_exp.getId());
        Assert.assertEquals(nsd_exp.getId(), nsd.getId());
        Assert.assertEquals(nsd_exp.getName(), nsd.getName());
        Assert.assertEquals(nsd_exp.getVendor(), nsd.getVendor());
        Assert.assertEquals(nsd_exp.getVersion(), nsd.getVersion());
        Assert.assertEquals(nsd_exp.isEnabled(), nsd.isEnabled());
    }

    private NetworkServiceDescriptor createNetworkServiceDescriptor() {
        final NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
        nsd.setVendor("FOKUS");
        Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<VirtualNetworkFunctionDescriptor>();
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor1 = getVirtualNetworkFunctionDescriptor();
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor2 = getVirtualNetworkFunctionDescriptor();
        virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor1);
        nsd.setVnfd(virtualNetworkFunctionDescriptors);

        VNFDependency vnfDependency = new VNFDependency();
        vnfDependency.setSource(virtualNetworkFunctionDescriptor1);
        vnfDependency.setTarget(virtualNetworkFunctionDescriptor2);
        nsd.getVnf_dependency().add(vnfDependency);

        return nsd;
    }

    private VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor() {
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
        virtualNetworkFunctionDescriptor.setName("" + ((int) (Math.random() * 1000)));
        virtualNetworkFunctionDescriptor.setEndpoint("test");
        virtualNetworkFunctionDescriptor.setMonitoring_parameter(new HashSet<String>() {
            {
                add("monitor1");
                add("monitor2");
                add("monitor3");
            }
        });
        virtualNetworkFunctionDescriptor.setDeployment_flavour(new HashSet<VNFDeploymentFlavour>() {{
            VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
            vdf.setExtId("ext_id");
            vdf.setFlavour_key("flavor_name");
            add(vdf);
        }});
        virtualNetworkFunctionDescriptor
                .setVdu(new HashSet<VirtualDeploymentUnit>() {
                    {
                        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
                        vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
                        vdu.setComputation_requirement("high_requirements");
                        VimInstance vimInstance = new VimInstance();
                        vimInstance.setName("vim_instance");
                        vimInstance.setType("test");
                        vdu.setVimInstance(vimInstance);
                        add(vdu);
                    }
                });
        return virtualNetworkFunctionDescriptor;
    }

    private VimInstance createVimInstance() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("vim_instance");
        vimInstance.setType("test");
        vimInstance.setNetworks(new HashSet<Network>() {{
            Network network = new Network();
            network.setExtId("ext_id");
            network.setName("network_name");
            add(network);
        }});
        vimInstance.setFlavours(new HashSet<DeploymentFlavour>() {{
            DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_1");
            deploymentFlavour.setFlavour_key("flavor_name");
            add(deploymentFlavour);

            deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_2");
            deploymentFlavour.setFlavour_key("m1.tiny");
            add(deploymentFlavour);
        }});
        vimInstance.setImages(new HashSet<NFVImage>() {{
            NFVImage image = new NFVImage();
            image.setExtId("ext_id_1");
            image.setName("ubuntu-14.04-server-cloudimg-amd64-disk1");
            add(image);

            image = new NFVImage();
            image.setExtId("ext_id_2");
            image.setName("image_name_1");
            add(image);
        }});
        return vimInstance;
    }

}
