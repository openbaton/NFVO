package org.project.neutrino.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class NetworkManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	private ConfigurationManagement configurationManagement;

	@Autowired
	@Qualifier("configurationRepository")
	private GenericRepository<Configuration> configurationRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(ApplicationTest.class);
		log.info("Starting test");
	}

	@Test
	public void configurationManagementNotNull(){
		Assert.assertNotNull(configurationManagement);
	}

	@Test
	public void nfvImageManagementUpdateTest(){
		Configuration configuration_exp = createConfigutation();
		when(configurationRepository.find(configuration_exp.getId())).thenReturn(configuration_exp);

		Configuration configuration_new = createConfigutation();
		configuration_new.setName("UpdatedName");
		ConfigurationParameter configurationParameter = new ConfigurationParameter();
		configurationParameter.setKey("new_key");
		configurationParameter.setValue("new_value");
		configuration_new.getParameters().add(configurationParameter);
		configuration_exp = configurationManagement.update(configuration_new, configuration_exp.getId());

		assertEqualsConfiguration(configuration_exp, configuration_new);

	}

	private void assertEqualsConfiguration(Configuration configuration_exp, Configuration configuration_new) {
		Assert.assertEquals(configuration_exp.getName(), configuration_new.getName());
		int i = 0;
		for (ConfigurationParameter configurationParameter : configuration_exp.getParameters()){
			Assert.assertEquals(configurationParameter.getKey(),configuration_new.getParameters().get(i).getKey());
			Assert.assertEquals(configurationParameter.getValue(),configuration_new.getParameters().get(i).getValue());
			i++;
		}
	}

	private Configuration createConfigutation() {
		Configuration configuration = new Configuration();
		configuration.setName("configuration_name");
		configuration.setParameters(new ArrayList<ConfigurationParameter>(){{
			ConfigurationParameter configurationParameter = new ConfigurationParameter();
			configurationParameter.setKey("key");
			configurationParameter.setValue("value");
			add(configurationParameter);
		}});
		return configuration;
	}

	private void assertEqualsNetwork(Network network_exp, Network network_new) {
		Assert.assertEquals(network_exp.getName(), network_new.getName());
		Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
		Assert.assertEquals(network_exp.getExternal(), network_new.getExternal());
		Assert.assertEquals(network_exp.getNetworkType(), network_new.getNetworkType());
		Assert.assertEquals(network_exp.getPhysicalNetworkName(), network_new.getPhysicalNetworkName());
		Assert.assertEquals(network_exp.getSegmentationId(), network_new.getSegmentationId());
		Assert.assertEquals(network_exp.getShared(), network_new.getShared());
		Assert.assertEquals(network_exp.getSubnets().size(), network_new.getSubnets().size());
	}

	private Network createNetwork() {
		Network network = new Network();
		network.setName("network_name");
		network.setExtId("ext_id");
		network.setExternal(false);
		network.setNetworkType("network_type");
		network.setPhysicalNetworkName("physical_network_name");
		network.setSegmentationId(0);
		network.setShared(false);
		network.setSubnets(new ArrayList<Subnet>(){{
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
	public void configurationManagementAddTest(){
		Configuration configuration_exp = createConfigutation();
		when(configurationRepository.create(any(Configuration.class))).thenReturn(configuration_exp);
		Configuration configuration_new = configurationManagement.add(configuration_exp);

		assertEqualsConfiguration(configuration_exp, configuration_new);
	}

	@Test
	public void configurationManagementQueryTest(){
		when(configurationRepository.findAll()).thenReturn(new ArrayList<Configuration>());

		Assert.assertEquals(0, configurationManagement.query().size());

		Configuration configutation_exp = createConfigutation();
		when(configurationRepository.find(configutation_exp.getId())).thenReturn(configutation_exp);
		Configuration configuration_new = configurationManagement.query(configutation_exp.getId());
		assertEqualsConfiguration(configutation_exp, configuration_new);
	}

	@Test
	public void configurationManagementDeleteTest(){
		Configuration configuration_exp = createConfigutation();
		when(configurationRepository.find(configuration_exp.getId())).thenReturn(configuration_exp);
		configurationManagement.delete(configuration_exp.getId());
		when(configurationRepository.find(configuration_exp.getId())).thenReturn(null);
		Configuration configuration_new = configurationManagement.query(configuration_exp.getId());
		Assert.assertNull(configuration_new);
	}

	@AfterClass
	public static void shutdown() {
		// TODO Teardown to avoid exceptions during test shutdown
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
		List<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new ArrayList<VirtualNetworkFunctionDescriptor>();
		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
		virtualNetworkFunctionDescriptor
				.setMonitoring_parameter(new ArrayList<String>() {
					{
						add("monitor1");
						add("monitor2");
						add("monitor3");
					}
				});
		virtualNetworkFunctionDescriptor.setDeployment_flavour(new ArrayList<VNFDeploymentFlavour>() {{
			VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
			vdf.setExtId("ext_id");
			vdf.setFlavour_key("flavor_name");
			add(vdf);
		}});
		virtualNetworkFunctionDescriptor
				.setVdu(new ArrayList<VirtualDeploymentUnit>() {
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
		vimInstance.setNetworks(new ArrayList<Network>() {{
			Network network = new Network();
			network.setExtId("ext_id");
			network.setName("network_name");
			add(network);
		}});
		vimInstance.setFlavours(new ArrayList<DeploymentFlavour>() {{
			DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
			deploymentFlavour.setExtId("ext_id_1");
			deploymentFlavour.setFlavour_key("flavor_name");
			add(deploymentFlavour);

			deploymentFlavour = new DeploymentFlavour();
			deploymentFlavour.setExtId("ext_id_2");
			deploymentFlavour.setFlavour_key("m1.tiny");
			add(deploymentFlavour);
		}});
		vimInstance.setImages(new ArrayList<NFVImage>() {{
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
