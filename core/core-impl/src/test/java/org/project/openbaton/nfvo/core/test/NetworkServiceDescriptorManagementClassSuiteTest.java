package org.project.openbaton.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.common.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.common.catalogue.mano.common.HighAvailability;
import org.project.openbaton.common.catalogue.mano.common.VNFDependency;
import org.project.openbaton.common.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.common.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.common.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.common.catalogue.nfvo.NFVImage;
import org.project.openbaton.common.catalogue.nfvo.Network;
import org.project.openbaton.common.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class NetworkServiceDescriptorManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	private NetworkServiceDescriptorManagement nsdManagement;

	@Autowired
	@Qualifier("vimRepository")
	GenericRepository<VimInstance> vimRepository;


	@Autowired
	@Qualifier("NSDRepository")
	GenericRepository<NetworkServiceDescriptor> nsdRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(ApplicationTest.class);
		log.info("Starting test");
	}

	@Test
	public void nsdManagementNotNull(){
		Assert.assertNotNull(nsdManagement);
	}

	@Test
	public void nsdManagementEnableTest() throws NotFoundException, BadFormatException {
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsdManagement.onboard(nsd_exp);
		when(nsdRepository.find(anyString())).thenReturn(nsd_exp);
		Assert.assertTrue(nsdManagement.enable(nsd_exp.getId()));
		Assert.assertTrue(nsd_exp.isEnabled());
		nsdManagement.delete(nsd_exp.getId());
	}

	@Test
	public void nsdManagementDisableTest() throws NotFoundException, BadFormatException {
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		nsd_exp.setEnabled(true);
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsdManagement.onboard(nsd_exp);
		when(nsdRepository.find(anyString())).thenReturn(nsd_exp);
		Assert.assertFalse(nsdManagement.disable(nsd_exp.getId()));
		Assert.assertFalse(nsd_exp.isEnabled());
		nsdManagement.delete(nsd_exp.getId());
	}

	@Test
	public void nsdManagementQueryTest(){
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
		List<NetworkServiceDescriptor> nsds = nsdManagement.query();
		Assert.assertEquals(nsds.size(), 0);
		final NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>() {{
			add(nsd_exp);
		}});
		nsds = nsdManagement.query();
		Assert.assertEquals(nsds.size(), 1);
		nsdManagement.delete(nsd_exp.getId());
	};

	@Test
	public void nsdManagementOnboardTest() throws NotFoundException, BadFormatException {
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
		when(nsdRepository.find(anyString())).thenReturn(null);
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>());
		exception.expect(NotFoundException.class);
		nsdManagement.onboard(nsd_exp);

		exception.expect(NullPointerException.class);
		assertEqualsNSD(nsd_exp);

		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		exception = ExpectedException.none();
		nsdManagement.onboard(nsd_exp);
		assertEqualsNSD(nsd_exp);
		nsdManagement.delete(nsd_exp.getId());
	}


	@Test
	public void nsdManagementUpdateTest() throws NotFoundException, BadFormatException {
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();

		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsdManagement.onboard(nsd_exp);
		when(nsdRepository.find(nsd_exp.getId())).thenReturn(nsd_exp);

		NetworkServiceDescriptor new_nsd = createNetworkServiceDescriptor();
		new_nsd.setName("UpdatedName");
		nsdManagement.update(new_nsd, nsd_exp.getId());

		new_nsd.setId(nsd_exp.getId());

		assertEqualsNSD(new_nsd);

		nsdManagement.delete(nsd_exp.getId());
	}

	@AfterClass
	public static void shutdown() {
		// TODO Teardown to avoid exceptions during test shutdown
	}

	private void assertEqualsNSD(NetworkServiceDescriptor nsd_exp) throws NoResultException {
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
		virtualNetworkFunctionDescriptor.setName("" + ((int) (Math.random()*1000)));
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
