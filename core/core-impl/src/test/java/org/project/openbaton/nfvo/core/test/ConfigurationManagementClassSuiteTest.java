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
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.nfvo.core.api.ConfigurationManagement;
import org.project.openbaton.nfvo.repositories.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
public class ConfigurationManagementClassSuiteTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

    @InjectMocks
    private ConfigurationManagement configurationManagement;

    @Mock
    private ConfigurationRepository configurationRepository;

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
    public void configurationManagementNotNull() {
        Assert.assertNotNull(configurationManagement);
    }

    @Test
    public void nfvImageManagementUpdateTest() {
        Configuration configuration_exp = createConfigutation();
        when(configurationRepository.findOne(configuration_exp.getId())).thenReturn(configuration_exp);

        Configuration configuration_new = createConfigutation();
        configuration_new.setName("UpdatedName");
        ConfigurationParameter configurationParameter = new ConfigurationParameter();
        configurationParameter.setConfKey("new_key");
        configurationParameter.setValue("new_value");
        configuration_new.getConfigurationParameters().add(configurationParameter);
        configuration_exp = configurationManagement.update(configuration_new, configuration_exp.getId());

        assertEqualsConfiguration(configuration_exp, configuration_new);

    }

    private void assertEqualsConfiguration(Configuration configuration_exp, Configuration configuration_new) {
        Assert.assertEquals(configuration_exp.getName(), configuration_new.getName());
        int i = 0;
        for (ConfigurationParameter configurationParameter : configuration_exp.getConfigurationParameters()) {
            ConfigurationParameter[] parameters = new ConfigurationParameter[10];
            Assert.assertEquals(configurationParameter.getConfKey(), configuration_new.getConfigurationParameters().toArray(parameters)[i].getConfKey());
            Assert.assertEquals(configurationParameter.getValue(), configuration_new.getConfigurationParameters().toArray(parameters)[i].getValue());
            i++;
        }
    }

    private Configuration createConfigutation() {
        Configuration configuration = new Configuration();
        configuration.setName("configuration_name");
        configuration.setConfigurationParameters(new HashSet<ConfigurationParameter>() {{
            ConfigurationParameter configurationParameter = new ConfigurationParameter();
            configurationParameter.setConfKey("key");
            configurationParameter.setValue("value");
            add(configurationParameter);
        }});
        return configuration;
    }

    private void assertEqualsNetwork(Network network_exp, Network network_new) {
        Assert.assertEquals(network_exp.getName(), network_new.getName());
        Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
        Assert.assertEquals(network_exp.isExternal(), network_new.isExternal());
        Assert.assertEquals(network_exp.isShared(), network_new.isShared());
        Assert.assertEquals(network_exp.getSubnets().size(), network_new.getSubnets().size());
    }

    @Test
    public void configurationManagementAddTest() {
        Configuration configuration_exp = createConfigutation();
        when(configurationRepository.save(any(Configuration.class))).thenReturn(configuration_exp);
        Configuration configuration_new = configurationManagement.add(configuration_exp);

        assertEqualsConfiguration(configuration_exp, configuration_new);
    }

    @Test
    public void configurationManagementQueryTest() {
        when(configurationRepository.findAll()).thenReturn(new ArrayList<Configuration>());

        Assert.assertEquals(false, configurationManagement.query().iterator().hasNext());

        Configuration configutation_exp = createConfigutation();
        when(configurationRepository.findOne(configutation_exp.getId())).thenReturn(configutation_exp);
        Configuration configuration_new = configurationManagement.query(configutation_exp.getId());
        assertEqualsConfiguration(configutation_exp, configuration_new);
    }

    @Test
    public void configurationManagementDeleteTest() {
        Configuration configuration_exp = createConfigutation();
        when(configurationRepository.findOne(configuration_exp.getId())).thenReturn(configuration_exp);
        configurationManagement.delete(configuration_exp.getId());
        when(configurationRepository.findOne(configuration_exp.getId())).thenReturn(null);
        Configuration configuration_new = configurationManagement.query(configuration_exp.getId());
        Assert.assertNull(configuration_new);
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
