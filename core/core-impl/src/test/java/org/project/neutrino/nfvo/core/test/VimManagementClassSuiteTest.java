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
import org.project.neutrino.nfvo.catalogue.nfvo.Location;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.core.interfaces.VimManagement;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.DeploymentFlavorManagement;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.NetworkManagement;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class VimManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	VimBroker<ImageManagement> imageManagementVimBroker;
	@Autowired
	VimBroker<DeploymentFlavorManagement> flavorManagementVimBroker;
	@Autowired
	VimBroker<NetworkManagement> networkManagementVimBroker;
	@Autowired
	GenericRepository<VimInstance> vimRepository;

	@Autowired
	private VimManagement vimManagement;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(ApplicationTest.class);
		log.info("Starting test");
	}

	@Test
	public void nfvImageManagementNotNull(){
		Assert.assertNotNull(vimManagement);
	}

	@Test
	public void nfvImageManagementUpdateTest(){
	}

	@Test
	public void nfvImageManagementRefreshTest() throws VimException {
		initMocks();
		VimInstance vimInstance = createVimInstance();
		vimManagement.refresh(vimInstance);

		Assert.assertEquals(0, vimInstance.getFlavours().size());
		Assert.assertEquals(0, vimInstance.getImages().size());
		Assert.assertEquals(0,vimInstance.getNetworks().size());
	}

	@Test
	public void vimManagementUpdateTest() throws VimException {
		initMocks();
		VimInstance vimInstance_exp = createVimInstance();
		when(vimRepository.find(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
		VimInstance vimInstance_new = createVimInstance();
		vimInstance_new.setName("UpdatedName");
		vimInstance_new.setTenant("UpdatedTenant");
		vimInstance_new.setUsername("UpdatedUsername");

		vimInstance_exp = vimManagement.update(vimInstance_new, vimInstance_exp.getId());

		Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
		Assert.assertEquals(vimInstance_exp.getTenant(), vimInstance_new.getTenant());
		Assert.assertEquals(vimInstance_exp.getType(), vimInstance_new.getType());
		Assert.assertEquals(vimInstance_exp.getKeyPair(), vimInstance_new.getKeyPair());
		Assert.assertEquals(vimInstance_exp.getUsername(), vimInstance_new.getUsername());
		Assert.assertEquals(vimInstance_exp.getAuthUrl(), vimInstance_new.getAuthUrl());
		Assert.assertEquals(vimInstance_exp.getPassword(), vimInstance_new.getPassword());
		Assert.assertEquals(vimInstance_exp.getLocation().getName(), vimInstance_new.getLocation().getName());
		Assert.assertEquals(vimInstance_exp.getLocation().getLatitude(), vimInstance_new.getLocation().getLatitude());
		Assert.assertEquals(vimInstance_exp.getLocation().getLongitude(), vimInstance_new.getLocation().getLongitude());
		Assert.assertEquals(vimInstance_exp.getFlavours().size(), 0);
		Assert.assertEquals(vimInstance_exp.getImages().size(), 0);
		Assert.assertEquals(vimInstance_exp.getNetworks().size(), 0);
	}

	@Test
	public void nfvImageManagementAddTest() throws VimException {
		initMocks();
		VimInstance vimInstance_exp = createVimInstance();
		when(vimRepository.create(any(VimInstance.class))).thenReturn(vimInstance_exp);
		VimInstance vimInstance_new = vimManagement.add(vimInstance_exp);

		Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
		Assert.assertEquals(vimInstance_exp.getTenant(), vimInstance_new.getTenant());
		Assert.assertEquals(vimInstance_exp.getType(), vimInstance_new.getType());
		Assert.assertEquals(vimInstance_exp.getKeyPair(), vimInstance_new.getKeyPair());
		Assert.assertEquals(vimInstance_exp.getUsername(), vimInstance_new.getUsername());
		Assert.assertEquals(vimInstance_exp.getAuthUrl(), vimInstance_new.getAuthUrl());
		Assert.assertEquals(vimInstance_exp.getPassword(), vimInstance_new.getPassword());
		Assert.assertEquals(vimInstance_exp.getLocation().getName(), vimInstance_new.getLocation().getName());
		Assert.assertEquals(vimInstance_exp.getLocation().getLatitude(), vimInstance_new.getLocation().getLatitude());
		Assert.assertEquals(vimInstance_exp.getLocation().getLongitude(), vimInstance_new.getLocation().getLongitude());
		Assert.assertEquals(vimInstance_exp.getFlavours().size(), vimInstance_new.getFlavours().size());
		Assert.assertEquals(vimInstance_exp.getImages().size(), vimInstance_new.getImages().size());
		Assert.assertEquals(vimInstance_exp.getNetworks().size(), vimInstance_new.getNetworks().size());
	}

	private void initMocks() throws VimException {
		ImageManagement imageManagement = mock(ImageManagement.class);
		when(imageManagement.queryImages(any(VimInstance.class))).thenReturn(new ArrayList<NFVImage>());
		when(imageManagementVimBroker.getVim(anyString())).thenReturn(imageManagement);

		NetworkManagement networkManagement = mock(NetworkManagement.class);
		when(networkManagement.queryNetwork(any(VimInstance.class))).thenReturn(new ArrayList<Network>());
		when(networkManagementVimBroker.getVim(anyString())).thenReturn(networkManagement);

		DeploymentFlavorManagement deploymentFlavorManagement = mock(DeploymentFlavorManagement.class);
		when(deploymentFlavorManagement.queryDeploymentFlavors(any(VimInstance.class))).thenReturn(new ArrayList<DeploymentFlavour>());
		when(flavorManagementVimBroker.getVim(anyString())).thenReturn(deploymentFlavorManagement);
	}

	@Test
	public void nfvImageManagementQueryTest(){
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>());

		Assert.assertEquals(0, vimManagement.query().size());

		VimInstance vimInstance_exp = createVimInstance();
		when(vimRepository.find(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
		VimInstance vimInstance_new = vimManagement.query(vimInstance_exp.getId());
		Assert.assertEquals(vimInstance_exp.getId(), vimInstance_new.getId());
		Assert.assertEquals(vimInstance_exp.getName(), vimInstance_new.getName());
		Assert.assertEquals(vimInstance_exp.getFlavours().size(), vimInstance_new.getFlavours().size());
		Assert.assertEquals(vimInstance_exp.getImages().size(), vimInstance_new.getImages().size());
		Assert.assertEquals(vimInstance_exp.getNetworks().size(), vimInstance_new.getNetworks().size());
	}

	@Test
	public void nfvImageManagementDeleteTest(){
		VimInstance vimInstance_exp = createVimInstance();
		when(vimRepository.find(vimInstance_exp.getId())).thenReturn(vimInstance_exp);
		vimManagement.delete(vimInstance_exp.getId());
		when(vimRepository.find(vimInstance_exp.getId())).thenReturn(null);
		VimInstance vimInstance_new = vimManagement.query(vimInstance_exp.getId());
		Assert.assertNull(vimInstance_new);
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
		Location location = new Location();
		location.setName("LocationName");
		location.setLatitude("Latitude");
		location.setLongitude("Longitude");
		vimInstance.setLocation(location);
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
