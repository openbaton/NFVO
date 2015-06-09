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
import org.project.neutrino.nfvo.common.exceptions.BadFormatException;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
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

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class NetworkServiceRecordManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	private NetworkServiceRecordManagement nsrManagement;


	@Autowired
	@Qualifier("vimRepository")
	GenericRepository<VimInstance> vimRepository;

	@Autowired
	@Qualifier("NSDRepository")
	GenericRepository<NetworkServiceDescriptor> nsdRepository;

	@Autowired
	@Qualifier("NSRRepository")
	GenericRepository<NetworkServiceRecord> nsrRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(ApplicationTest.class);
		log.info("Starting test");
	}

	@Test
	public void nsrManagementNotNull(){
		Assert.assertNotNull(nsrManagement);
	}

	@Test
	public void nsrManagementQueryTest(){
		when(nsrRepository.findAll()).thenReturn(new ArrayList<NetworkServiceRecord>());
		List<NetworkServiceRecord> nsds = nsrManagement.query();
		Assert.assertEquals(nsds.size(), 0);
		final NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
		when(nsrRepository.findAll()).thenReturn(new ArrayList<NetworkServiceRecord>() {{
			add(nsd_exp);
		}});
		nsds = nsrManagement.query();
		Assert.assertEquals(nsds.size(), 1);

		when(nsrRepository.find(nsd_exp.getId())).thenReturn(nsd_exp);
		assertEqualsNSR(nsd_exp);
	}

	@Test
	public void nsrManagementDeleteTest(){
		NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
		when(nsrRepository.find(nsd_exp.getId())).thenReturn(nsd_exp);
		nsrManagement.delete(nsd_exp.getId());
	}

	@Test
	public void nsrManagementOnboardTest1() throws NotFoundException, InterruptedException, ExecutionException, NamingException, VimException, JMSException, BadFormatException {
		when(nsdRepository.findAll()).thenReturn(new ArrayList<NetworkServiceDescriptor>());
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>());
		NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();

		exception.expect(NotFoundException.class);
		nsrManagement.onboard(nsd_exp);
	}

	@Test
	public void nsrManagementOnboardTest2() throws NotFoundException, InterruptedException, ExecutionException, NamingException, VimException, JMSException, BadFormatException {
		final NetworkServiceDescriptor nsd_exp = createNetworkServiceDescriptor();
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsrManagement.onboard(nsd_exp);
	}

	@Test
	public void nsrManagementUpdateTest() throws NotFoundException {
		final NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
		when(nsrRepository.find(nsd_exp.getId())).thenReturn(nsd_exp);
		NetworkServiceRecord new_nsr = createNetworkServiceRecord();
		new_nsr.setName("UpdatedName");
		nsrManagement.update(new_nsr,nsd_exp.getId());
		new_nsr.setId(nsd_exp.getId());
		assertEqualsNSR(new_nsr);
	}


	@AfterClass
	public static void shutdown() {
		// TODO Teardown to avoid exceptions during test shutdown
	}

	private void assertEqualsNSR(NetworkServiceRecord nsr_exp) throws NoResultException {
		NetworkServiceRecord nsd = nsrManagement.query(nsr_exp.getId());
		Assert.assertEquals(nsr_exp.getId(), nsd.getId());
		Assert.assertEquals(nsr_exp.getName(), nsd.getName());
		Assert.assertEquals(nsr_exp.getVendor(), nsd.getVendor());
		Assert.assertEquals(nsr_exp.getVersion(), nsd.getVersion());
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

	private NetworkServiceRecord createNetworkServiceRecord() {
		final NetworkServiceRecord nsr = new NetworkServiceRecord();
		nsr.setVendor("FOKUS");
		ArrayList<VirtualNetworkFunctionRecord> virtualNetworkFunctionRecords = new ArrayList<VirtualNetworkFunctionRecord>();
		VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
		virtualNetworkFunctionRecord
				.setMonitoring_parameter(new ArrayList<String>() {
					{
						add("monitor1");
						add("monitor2");
						add("monitor3");
					}
				});
		VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
		vdf.setExtId("ext_id");
		vdf.setFlavour_key("flavor_name");
		virtualNetworkFunctionRecord.setDeployment_flavour_key(vdf);
		virtualNetworkFunctionRecord
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
		virtualNetworkFunctionRecords.add(virtualNetworkFunctionRecord);
		nsr.setVnfr(virtualNetworkFunctionRecords);
		return nsr;
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
