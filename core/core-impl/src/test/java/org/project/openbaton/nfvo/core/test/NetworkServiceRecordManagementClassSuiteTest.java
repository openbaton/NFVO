package org.project.openbaton.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.nfvo.catalogue.mano.common.*;
import org.project.openbaton.nfvo.catalogue.mano.descriptor.*;
import org.project.openbaton.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.catalogue.nfvo.NFVImage;
import org.project.openbaton.nfvo.catalogue.nfvo.Network;
import org.project.openbaton.nfvo.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

	@Autowired
	private NSDUtils nsdUtils;

	@Autowired
	public VimBroker vimBroker;

	@Before
	public void init() throws VimException{
		MockitoAnnotations.initMocks(ApplicationTest.class);
		ResourceManagement resourceManagement = mock(ResourceManagement.class);
		when(resourceManagement.allocate(any(VirtualDeploymentUnit.class), any(VirtualNetworkFunctionRecord.class))).thenReturn(new AsyncResult<String>("mocked_id"));
		Vim vim = mock(Vim.class);
		when(vimBroker.getVim(anyString())).thenReturn(vim);
		when(vim.allocate(any(VirtualDeploymentUnit.class), any(VirtualNetworkFunctionRecord.class))).thenReturn(new AsyncResult<String>("mocked_id"));
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
	public void nsrManagementDeleteTest() throws VimException, InterruptedException, ExecutionException, NamingException, NotFoundException, JMSException {
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
	public void nsrManagementOnboardTest3() throws NotFoundException, InterruptedException, ExecutionException, NamingException, VimException, JMSException, BadFormatException {
		/**
		 * Initial settings
		 */
		NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();

		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = networkServiceDescriptor.getVnfd().iterator().next();
		LifecycleEvent event = new LifecycleEvent();
		event.setEvent(Event.INSTALL);
		event.setLifecycle_events(new HashSet<String>());
		event.getLifecycle_events().add("command_1");
		virtualNetworkFunctionDescriptor.getLifecycle_event().add(event);

		when(nsdRepository.find(anyString())).thenReturn(networkServiceDescriptor);
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsdUtils.fetchDependencies(networkServiceDescriptor);
		nsdUtils.fetchVimInstances(networkServiceDescriptor);

		/**
		 * Real Method
		 */

		nsrManagement.onboard(networkServiceDescriptor.getId());
	}

	@Test
	public void nsrManagementOnboardTest4() throws NotFoundException, InterruptedException, ExecutionException, NamingException, VimException, JMSException, BadFormatException {
		/**
		 * Initial settings
		 */
		NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();

		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = networkServiceDescriptor.getVnfd().iterator().next();
		LifecycleEvent event = new LifecycleEvent();
		event.setEvent(Event.ALLOCATE);
		event.setLifecycle_events(new HashSet<String>());
		event.getLifecycle_events().add("command_1");
		virtualNetworkFunctionDescriptor.getLifecycle_event().add(event);

		when(nsdRepository.find(anyString())).thenReturn(networkServiceDescriptor);
		when(vimRepository.findAll()).thenReturn(new ArrayList<VimInstance>() {{
			add(createVimInstance());
		}});

		nsdUtils.fetchDependencies(networkServiceDescriptor);
		nsdUtils.fetchVimInstances(networkServiceDescriptor);

		/**
		 * Real Method
		 */

		nsrManagement.onboard(networkServiceDescriptor.getId());
	}

	@Test
	public void nsrManagementUpdateTest() throws NotFoundException {
		final NetworkServiceRecord nsd_exp = createNetworkServiceRecord();
		when(nsrRepository.find(nsd_exp.getId())).thenReturn(nsd_exp);
		NetworkServiceRecord new_nsr = createNetworkServiceRecord();
		new_nsr.setName("UpdatedName");
		nsrManagement.update(new_nsr, nsd_exp.getId());
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
		nsd.setMonitoring_parameter(new HashSet<String>());
		nsd.getMonitoring_parameter().add("monitor1");
		nsd.getMonitoring_parameter().add("monitor2");
		nsd.getMonitoring_parameter().add("monitor3");
		nsd.setLifecycle_event(new HashSet<LifecycleEvent>());
		nsd.setPnfd(new HashSet<PhysicalNetworkFunctionDescriptor>());
		nsd.setVnffgd(new HashSet<VNFForwardingGraphDescriptor>());
		nsd.setVld(new HashSet<VirtualLinkDescriptor>());
		nsd.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
		nsd.setVnf_dependency(new HashSet<VNFDependency>());
		Set<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new HashSet<VirtualNetworkFunctionDescriptor>();
		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor1 = createVirtualNetworkFunctionDescriptor();
		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor2 = createVirtualNetworkFunctionDescriptor();
		virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor1);
		virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor2);

		nsd.setVnfd(virtualNetworkFunctionDescriptors);


		VNFDependency vnfDependency = new VNFDependency();
		vnfDependency.setSource(virtualNetworkFunctionDescriptor1);
		vnfDependency.setTarget(virtualNetworkFunctionDescriptor2);
		nsd.getVnf_dependency().add(vnfDependency);

		return nsd;
	}

	private VirtualNetworkFunctionDescriptor createVirtualNetworkFunctionDescriptor() {
		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
		virtualNetworkFunctionDescriptor.setName("" + ((int) (Math.random() * 10000)));
		virtualNetworkFunctionDescriptor.setMonitoring_parameter(new HashSet<String>());
		virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor1");
		virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor2");
		virtualNetworkFunctionDescriptor.getMonitoring_parameter().add("monitor3");
		virtualNetworkFunctionDescriptor.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
		virtualNetworkFunctionDescriptor.setConnection_point(new HashSet<ConnectionPoint>());
		virtualNetworkFunctionDescriptor.setVirtual_link(new HashSet<InternalVirtualLink>());
		virtualNetworkFunctionDescriptor.setLifecycle_event(new HashSet<LifecycleEvent>());

		virtualNetworkFunctionDescriptor.setDeployment_flavour(new HashSet<VNFDeploymentFlavour>() {{
			VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
			vdf.setExtId("ext_id");
			vdf.setFlavour_key("flavor_name");
			add(vdf);
		}});
		virtualNetworkFunctionDescriptor.setVdu(new HashSet<VirtualDeploymentUnit>() {
			{
				VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
				vdu.setVm_image(new HashSet<String>() {{
					add("mocked_image");
				}});
				vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
				vdu.setComputation_requirement("high_requirements");
				vdu.setVnfc(new HashSet<VNFComponent>());
				vdu.setLifecycle_event(new HashSet<LifecycleEvent>());
				vdu.setMonitoring_parameter(new HashSet<String>() {
					{
						add("monitor1");
						add("monitor2");
						add("monitor3");
					}
				});
				VimInstance vimInstance = new VimInstance();
				vimInstance.setName("vim_instance");
				vimInstance.setType("test");
				vdu.setVimInstance(vimInstance);
				add(vdu);
			}
		});
		return virtualNetworkFunctionDescriptor;
	}

	private NetworkServiceRecord createNetworkServiceRecord() {
		final NetworkServiceRecord nsr = new NetworkServiceRecord();
		nsr.setVendor("FOKUS");
		nsr.setMonitoring_parameter(new HashSet<String>());
		nsr.getMonitoring_parameter().add("monitor1");
		nsr.getMonitoring_parameter().add("monitor2");
		nsr.getMonitoring_parameter().add("monitor3");
		HashSet<VirtualNetworkFunctionRecord> virtualNetworkFunctionRecords = new HashSet<VirtualNetworkFunctionRecord>();
		VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
		virtualNetworkFunctionRecord
				.setMonitoring_parameter(new HashSet<String>() {
					{
						add("monitor1");
						add("monitor2");
						add("monitor3");
					}
				});
		VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
		vdf.setExtId("ext_id");
		vdf.setFlavour_key("flavor_name");
		virtualNetworkFunctionRecord.setDeployment_flavour_key(vdf.getFlavour_key());
		virtualNetworkFunctionRecord
				.setVdu(new HashSet<VirtualDeploymentUnit>() {
					{
						VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
						vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
						vdu.setVm_image(new HashSet<String>() {{
							add("mocked_image");
						}});
						vdu.setComputation_requirement("high_requirements");
						vdu.setVnfc(new HashSet<VNFComponent>());
						vdu.setLifecycle_event(new HashSet<LifecycleEvent>());
						vdu.setMonitoring_parameter(new HashSet<String>() {
							{
								add("monitor1");
								add("monitor2");
								add("monitor3");
							}
						});
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
