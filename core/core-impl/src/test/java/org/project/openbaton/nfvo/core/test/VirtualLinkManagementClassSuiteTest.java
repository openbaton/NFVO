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
import org.project.openbaton.catalogue.mano.common.*;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.LinkStatus;
import org.project.openbaton.catalogue.mano.record.VNFForwardingGraphRecord;
import org.project.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.Network;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.core.api.VirtualLinkManagement;
import org.project.openbaton.nfvo.repositories.VirtualLinkDescriptorRepository;
import org.project.openbaton.nfvo.repositories.VirtualLinkRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
public class VirtualLinkManagementClassSuiteTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

    @InjectMocks
    private VirtualLinkManagement virtualLinkManagement;

    @Mock
    private VirtualLinkDescriptorRepository virtualLinkDescriptorRepository;

    @Mock
    private VirtualLinkRecordRepository virtualLinkRecordRepository;

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
    public void virtualLinkManagementNotNull() {
        Assert.assertNotNull(virtualLinkManagement);
    }

    @Test
    public void virtualLinkManagementUpdateDescriptorTest() {
        VirtualLinkDescriptor virtualLinkDescriptor_exp = createVirtualLinkDescriptor();
        when(virtualLinkDescriptorRepository.findOne(virtualLinkDescriptor_exp.getId())).thenReturn(virtualLinkDescriptor_exp);
        when(virtualLinkDescriptorRepository.save(virtualLinkDescriptor_exp)).thenReturn(virtualLinkDescriptor_exp);

        VirtualLinkDescriptor virtualLinkDescriptor_new = createVirtualLinkDescriptor();
        virtualLinkDescriptor_new.setRoot_requirement("root_requirement_updated");
        when(virtualLinkDescriptorRepository.save(virtualLinkDescriptor_new)).thenReturn(virtualLinkDescriptor_new);
        virtualLinkDescriptor_exp = virtualLinkManagement.update(virtualLinkDescriptor_new, virtualLinkDescriptor_exp.getId());

        assertEquals(virtualLinkDescriptor_exp, virtualLinkDescriptor_new);
    }

    @Test
    public void virtualLinkManagementUpdateRecordTest() {
        VirtualLinkRecord virtualLinkRecord_exp = createVirtualLinkRecord();
        when(virtualLinkRecordRepository.findOne(virtualLinkRecord_exp.getId())).thenReturn(virtualLinkRecord_exp);
        when(virtualLinkRecordRepository.save(virtualLinkRecord_exp)).thenReturn(virtualLinkRecord_exp);

        VirtualLinkRecord virtualLinkRecord_new = createVirtualLinkRecord();
        virtualLinkRecord_new.setRoot_requirement("root_requirement_updated");
        when(virtualLinkRecordRepository.save(virtualLinkRecord_new)).thenReturn(virtualLinkRecord_new);
        virtualLinkRecord_exp = virtualLinkManagement.update(virtualLinkRecord_new, virtualLinkRecord_exp.getId());

        assertEquals(virtualLinkRecord_exp, virtualLinkRecord_new);
    }

    private void assertEquals(VirtualLinkRecord virtualLinkRecord_exp, VirtualLinkRecord virtualLinkRecord_new) {
        Assert.assertEquals(virtualLinkRecord_exp.getVendor(), virtualLinkRecord_new.getVendor());
        Assert.assertEquals(virtualLinkRecord_exp.getConnectivity_type(), virtualLinkRecord_new.getConnectivity_type());
        Assert.assertEquals(virtualLinkRecord_exp.getConnection(), virtualLinkRecord_new.getConnection());
        Assert.assertEquals(virtualLinkRecord_exp.getLeaf_requirement(), virtualLinkRecord_new.getLeaf_requirement());
        Assert.assertEquals(virtualLinkRecord_exp.getRoot_requirement(), virtualLinkRecord_new.getRoot_requirement());
        Assert.assertEquals(virtualLinkRecord_exp.getVersion(), virtualLinkRecord_new.getVersion());
        Assert.assertEquals(virtualLinkRecord_exp.getVim_id(), virtualLinkRecord_new.getVim_id());
        Assert.assertEquals(virtualLinkRecord_exp.getAllocated_capacity(), virtualLinkRecord_new.getAllocated_capacity());
    }

    private void assertEquals(VirtualLinkDescriptor virtualLinkDescriptor_exp, VirtualLinkDescriptor virtualLinkDescriptor_new) {
        Assert.assertEquals(virtualLinkDescriptor_exp.getDescriptor_version(), virtualLinkDescriptor_new.getDescriptor_version());
        Assert.assertEquals(virtualLinkDescriptor_exp.getVendor(), virtualLinkDescriptor_new.getVendor());
        Assert.assertEquals(virtualLinkDescriptor_exp.getConnectivity_type(), virtualLinkDescriptor_new.getConnectivity_type());
        Assert.assertEquals(virtualLinkDescriptor_exp.getConnection(), virtualLinkDescriptor_new.getConnection());
        Assert.assertEquals(virtualLinkDescriptor_exp.getNumber_of_endpoints(), virtualLinkDescriptor_new.getNumber_of_endpoints());
        Assert.assertEquals(virtualLinkDescriptor_exp.getLeaf_requirement(), virtualLinkDescriptor_new.getLeaf_requirement());
        Assert.assertEquals(virtualLinkDescriptor_exp.getRoot_requirement(), virtualLinkDescriptor_new.getRoot_requirement());
    }

    private VirtualLinkDescriptor createVirtualLinkDescriptor() {
        VirtualLinkDescriptor virtualLinkDescriptor = new VirtualLinkDescriptor();
        virtualLinkDescriptor.setConnection(new HashSet<String>() {{
            add("connection1");
        }});
        virtualLinkDescriptor.setDescriptor_version("desc_version");
        virtualLinkDescriptor.setRoot_requirement("root_req");
        virtualLinkDescriptor.setNumber_of_endpoints(3);
        virtualLinkDescriptor.setVendor("vendor");
        virtualLinkDescriptor.setVld_security(new Security());
        virtualLinkDescriptor.setConnectivity_type("type");
        virtualLinkDescriptor.setLeaf_requirement("leaf_req");
        virtualLinkDescriptor.setQos(new HashSet<String>() {{
            add("qos");
        }});
        virtualLinkDescriptor.setTest_access(new HashSet<String>() {{
            add("test_access");
        }});
        return virtualLinkDescriptor;
    }

    private VirtualLinkRecord createVirtualLinkRecord() {
        VirtualLinkRecord virtualLinkRecord = new VirtualLinkRecord();
        virtualLinkRecord.setConnection(new HashSet<String>() {{
            add("connection1");
        }});
        virtualLinkRecord.setRoot_requirement("root_req");
        virtualLinkRecord.setVendor("vendor");
        virtualLinkRecord.setConnectivity_type("type");
        virtualLinkRecord.setLeaf_requirement("leaf_req");
        virtualLinkRecord.setQos(new HashSet<String>() {{
            add("qos");
        }});
        virtualLinkRecord.setVersion("version");
        virtualLinkRecord.setVim_id("vim_id");
        virtualLinkRecord.setVnffgr_reference(new HashSet<VNFForwardingGraphRecord>());
        virtualLinkRecord.setStatus(LinkStatus.NORMALOPERATION);
        virtualLinkRecord.setAudit_log(new HashSet<String>() {{
            add("audit_log_1");
        }});
        virtualLinkRecord.setAllocated_capacity(new HashSet<String>() {{
            add("allocated_cap");
        }});
        virtualLinkRecord.setTest_access(new HashSet<String>(){{add("test_access");}});
        virtualLinkRecord.setParent_ns("parent_id");
        virtualLinkRecord.setNumber_of_endpoints(3);
        virtualLinkRecord.setNotification(new HashSet<String>() {{
            add("notification");
        }});
        virtualLinkRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>() {{
            LifecycleEvent lifecycleEvent = new LifecycleEvent();
            lifecycleEvent.setEvent(Event.INSTANTIATE);
            lifecycleEvent.setLifecycle_events(new ArrayList<String>() {{
                add("command");
            }});
            add(lifecycleEvent);
        }});
        return virtualLinkRecord;
    }

    @Test
    public void virtualLinkManagementAddDescriptorTest() {
        VirtualLinkDescriptor virtualLinkDescriptor_exp = createVirtualLinkDescriptor();
        when(virtualLinkDescriptorRepository.save(any(VirtualLinkDescriptor.class))).thenReturn(virtualLinkDescriptor_exp);
        VirtualLinkDescriptor virtualLinkDescriptor_new = virtualLinkManagement.add(virtualLinkDescriptor_exp);

        assertEquals(virtualLinkDescriptor_exp, virtualLinkDescriptor_new);
    }

    @Test
    public void virtualLinkManagementRecordAddTest() {
        VirtualLinkRecord virtualLinkRecord_exp = createVirtualLinkRecord();
        when(virtualLinkRecordRepository.save(any(VirtualLinkRecord.class))).thenReturn(virtualLinkRecord_exp);
        VirtualLinkRecord virtualLinkRecord_new = virtualLinkManagement.add(virtualLinkRecord_exp);

        assertEquals(virtualLinkRecord_exp, virtualLinkRecord_new);
    }

    @Test
    public void virtualLinkManagementQueryTest() {
        when(virtualLinkDescriptorRepository.findAll()).thenReturn(new ArrayList<VirtualLinkDescriptor>());
        when(virtualLinkRecordRepository.findAll()).thenReturn(new ArrayList<VirtualLinkRecord>());

        Assert.assertEquals(false, virtualLinkManagement.queryDescriptors().iterator().hasNext());
        Assert.assertEquals(false, virtualLinkManagement.queryRecords().iterator().hasNext());

        VirtualLinkDescriptor virtualLinkDescriptor_exp = createVirtualLinkDescriptor();
        when(virtualLinkDescriptorRepository.findOne(virtualLinkDescriptor_exp.getId())).thenReturn(virtualLinkDescriptor_exp);
        VirtualLinkDescriptor virtualLinkDescriptor_new = virtualLinkManagement.queryDescriptor(virtualLinkDescriptor_exp.getId());
        assertEquals(virtualLinkDescriptor_exp, virtualLinkDescriptor_new);

        VirtualLinkRecord virtualLinkRecord_exp = createVirtualLinkRecord();
        when(virtualLinkRecordRepository.findOne(virtualLinkRecord_exp.getId())).thenReturn(virtualLinkRecord_exp);
        VirtualLinkRecord virtualLinkRecord_new = virtualLinkManagement.queryRecord(virtualLinkRecord_exp.getId());
        assertEquals(virtualLinkRecord_exp, virtualLinkRecord_new);
    }

    @Test
    public void virtualLinkManagementDeleteDescriptorTest() {
        VirtualLinkDescriptor virtualLinkDescriptor_exp = createVirtualLinkDescriptor();
        when(virtualLinkDescriptorRepository.findOne(virtualLinkDescriptor_exp.getId())).thenReturn(virtualLinkDescriptor_exp);
        virtualLinkManagement.delete(virtualLinkDescriptor_exp.getId());
        when(virtualLinkDescriptorRepository.findOne(virtualLinkDescriptor_exp.getId())).thenReturn(null);
        VirtualLinkDescriptor virtualLinkDescriptor_new = virtualLinkManagement.queryDescriptor(virtualLinkDescriptor_exp.getId());
        Assert.assertNull(virtualLinkDescriptor_new);
    }

    @Test
    public void virtualLinkManagementDeleteRecordTest() {
        VirtualLinkRecord virtualLinkRecord_exp = createVirtualLinkRecord();
        when(virtualLinkRecordRepository.findOne(virtualLinkRecord_exp.getId())).thenReturn(virtualLinkRecord_exp);
        when(virtualLinkDescriptorRepository.findOne(virtualLinkRecord_exp.getId())).thenThrow(NoResultException.class);
        virtualLinkManagement.delete(virtualLinkRecord_exp.getId());
        when(virtualLinkRecordRepository.findOne(virtualLinkRecord_exp.getId())).thenReturn(null);
        VirtualLinkRecord virtualLinkRecord_new = virtualLinkManagement.queryRecord(virtualLinkRecord_exp.getId());
        Assert.assertNull(virtualLinkRecord_new);
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

}
