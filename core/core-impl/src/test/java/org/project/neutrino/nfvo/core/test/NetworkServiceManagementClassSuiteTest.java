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
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.neutrino.nfvo.core.interfaces.VimManagement;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class NetworkServiceManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	private NetworkServiceDescriptorManagement nsdManagement;

	@Autowired
	private NFVImageManagement NFVImageManagement;

	@Autowired
	private NetworkServiceRecordManagement nsrManagement;

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
	public void imageManagementNotNull(){
		Assert.assertNotNull(NFVImageManagement);
	}
	@Test
	public void nsrManagementNotNull(){
		Assert.assertNotNull(nsrManagement);
	}

	@Test
	public void nsdManagementEnableTest() throws NotFoundException {
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
	public void nsdManagementDisableTest() throws NotFoundException {
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
	public void nsdManagementOnboardTest() throws NotFoundException {
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
		when(nsdRepository.find(anyString())).thenReturn(null);
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>());
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
	public void nsdManagementUpdateTest() throws NotFoundException {
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

	@Test
	@Ignore
	public void nsrManagementCreateTest()  {
		NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();
		NetworkServiceRecord networkServiceRecord = null;
		/**
		 * TODO to remove when there will be some vnfm_reg registered and figure out how to do in tests
		 */
		try {
			networkServiceRecord = nsrManagement.onboard(networkServiceDescriptor);
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (VimException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (NotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(networkServiceRecord);
		Assert.assertEquals(networkServiceDescriptor.getName(), networkServiceRecord.getName());
		Assert.assertEquals(networkServiceDescriptor.getMonitoring_parameter(), networkServiceRecord.getMonitoring_parameter());
		Assert.assertEquals(networkServiceDescriptor.getVendor(), networkServiceRecord.getVendor());
		Assert.assertEquals(networkServiceDescriptor.getLifecycle_event(), networkServiceRecord.getLifecycle_event());
		Assert.assertEquals(networkServiceDescriptor.getVersion(), networkServiceRecord.getVersion());
		int i=0;
		for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
			VirtualNetworkFunctionRecord vnfr = networkServiceRecord.getVnfr().get(i++);
			Assert.assertEquals(vnfd.getMonitoring_parameter(),vnfr.getMonitoring_parameter());
			Assert.assertEquals(vnfd.getLifecycle_event(),vnfr.getLifecycle_event());
			Assert.assertEquals(vnfd.getVersion(),vnfr.getVersion());
			Assert.assertEquals(vnfd.getVendor(),vnfr.getVendor());
		}
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
