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

package org.openbaton.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.core.NetworkManagement;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vim.drivers.exceptions.VimDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.*;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
public class NetworkManagementClassSuiteTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    public VimBroker vimBroker;

    private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

    @InjectMocks
    private NetworkManagement networkManagement;

    @Mock
    private NetworkRepository networkRepository;

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
    public void networkManagementUpdateTest() throws VimException {
        when(vimBroker.getVim(anyString())).thenReturn(new MyVim("test"));
        Network network = createNetwork();
        network.setName("UpdatedName");
        network.setExternal(true);
//		Vim vim = vimBroker.getVim("mocked_vim");
//		when(vim.update(any(VimInstance.class), any(Network.class))).thenReturn(network);

        Network updated_network = networkManagement.update(createVimInstance(), network);

        Assert.assertEquals(updated_network.getName(), network.getName());
        Assert.assertEquals(updated_network.getExtId(), network.getExtId());
        Assert.assertEquals(updated_network.isExternal(), network.isExternal());
    }

    private Network createNetwork() {
        Network network = new Network();
        network.setName("network_name");
        network.setExtId("ext_id");
        network.setExternal(false);
        network.setShared(false);
        network.setSubnets(new HashSet<Subnet>() {{
            add(createSubnet());
        }});
        return network;
    }

    private Subnet createSubnet() {
        final Subnet subnet = new Subnet();
        subnet.setName("subnet_name");
        subnet.setExtId("ext_id");
        subnet.setCidr("cidr");
        subnet.setNetworkId("network_id");
        return subnet;
    }

    @Test
    public void networkManagementAddTest() throws VimException {
        Network network_exp = createNetwork();
        when(networkRepository.save(any(Network.class))).thenReturn(network_exp);
        when(vimBroker.getVim(anyString())).thenReturn(new MyVim("test"));

        Network network_new = networkManagement.add(createVimInstance(), network_exp);

        Assert.assertEquals(network_exp.getId(), network_new.getId());
        Assert.assertEquals(network_exp.getName(), network_new.getName());
        Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
        Assert.assertEquals(network_exp.isExternal(), network_new.isExternal());
    }

    @Test
    public void networkManagementQueryTest() {
        when(networkRepository.findAll()).thenReturn(new ArrayList<Network>());

        //Assert.assertEquals(0, networkManagement.query().size());

        Network network_exp = createNetwork();
        when(networkRepository.findOne(network_exp.getId())).thenReturn(network_exp);
        Network network_new = networkManagement.query(network_exp.getId());
        Assert.assertEquals(network_exp.getId(), network_new.getId());
        Assert.assertEquals(network_exp.getName(), network_new.getName());
        Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
        Assert.assertEquals(network_exp.isExternal(), network_new.isExternal());
    }

    @Test
    public void networkManagementDeleteTest() throws VimException {
        Network network_exp = createNetwork();
        when(vimBroker.getVim(anyString())).thenReturn(new MyVim("test"));
        when(networkRepository.findOne(network_exp.getId())).thenReturn(network_exp);
        networkManagement.delete(createVimInstance(), network_exp);
        when(networkRepository.findOne(anyString())).thenReturn(null);
        Network network_new = networkManagement.query(network_exp.getId());
        Assert.assertNull(network_new);
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
        Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<VirtualNetworkFunctionDescriptor>();
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
        virtualNetworkFunctionDescriptor
                .setMonitoring_parameter(new HashSet<String>() {
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
        virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
        nsd.setVnfd(virtualNetworkFunctionDescriptors);
        return nsd;
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

    private class MyVim extends Vim implements org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement {

        public MyVim(String type, int port) {
            super(type, port);
        }

        public MyVim(String type, String name, int port) {
            super(type, name, port);
        }

        public MyVim(String type, String name) {
            super(type, name);
        }

        public MyVim(String type) {
            super(type);
        }

        @Override
        public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
            return null;
        }

        @Override
        public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {

        }

        @Override
        public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
            return null;
        }

        @Override
        public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException {
            return null;
        }

        @Override
        public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {
            return null;
        }

        @Override
        public NFVImage add(VimInstance vimInstance, NFVImage image, String image_url) throws VimException {
            return null;
        }

        @Override
        public void delete(VimInstance vimInstance, NFVImage image) throws VimException {

        }

        @Override
        public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
            return null;
        }

        @Override
        public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
            return null;
        }

        @Override
        public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {

        }

        @Override
        public Network add(VimInstance vimInstance, Network network) throws VimException {
            return network;
        }

        @Override
        public void delete(VimInstance vimInstance, Network network) throws VimException {

        }

        @Override
        public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
            return updatingNetwork;
        }

        @Override
        public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
            return null;
        }

        @Override
        public Network query(VimInstance vimInstance, String extId) throws VimException {
            return null;
        }

        @Override
        public AsyncResult<VNFCInstance> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFComponent vnfComponent, String userdata, Map<String, String> floatingIps) throws VimException, VimDriverException {
            return null;
        }

        @Override
        public List<Server> queryResources(VimInstance vimInstance) throws VimException {
            return null;
        }

        @Override
        public void update(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public void scale(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public void migrate(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public void operate(VirtualDeploymentUnit vdu, String operation) throws VimException {

        }

        @Override
        public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance) throws VimException {
            return null;

        }

        @Override
        public void createReservation(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public void queryReservation() throws VimException {

        }

        @Override
        public void updateReservation(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public void releaseReservation(VirtualDeploymentUnit vdu) throws VimException {

        }

        @Override
        public Quota getQuota(VimInstance vimInstance) {
            return null;
        }
    }

}
