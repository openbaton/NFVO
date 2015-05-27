package org.project.neutrino.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.neutrino.nfvo.core.interfaces.exception.NotFoundException;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
	ConfigurationManagement configurationManagement;

	@Autowired
	NetworkServiceDescriptorManagement nsdManagement;

	@Autowired
	NFVImageManagement NFVImageManagement;

	@Autowired
	NetworkServiceRecordManagement nsrManagement;

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
	public void nsrManagementCreateTest() throws NotFoundException {
		NetworkServiceDescriptor networkServiceDescriptor = createNetworkServiceDescriptor();
		NetworkServiceRecord networkServiceRecord = null;
		/**
		 * TODO to remove when there will be some vnfm registered and figure out how to do in tests
		 */
		exception.expect(NotFoundException.class);
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
		}

		Assert.assertNotNull(networkServiceRecord);
		Assert.assertEquals(networkServiceDescriptor.getName(), networkServiceRecord.getName());
		Assert.assertEquals(networkServiceDescriptor.getMonitoring_parameter(), networkServiceRecord.getMonitoring_parameter());
		Assert.assertEquals(networkServiceDescriptor.getVendor(), networkServiceRecord.getVendor());
		Assert.assertEquals(networkServiceDescriptor.getLifecycle_event(),networkServiceRecord.getLifecycle_event());
		Assert.assertEquals(networkServiceDescriptor.getVersion(),networkServiceRecord.getVersion());
		int i=0;
		for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
			VirtualNetworkFunctionRecord vnfr = networkServiceRecord.getVnfr().get(i++);
			Assert.assertEquals(vnfd.getMonitoring_parameter(),vnfr.getMonitoring_parameter());
			Assert.assertEquals(vnfd.getLifecycle_event(),vnfr.getLifecycle_event());
			Assert.assertEquals(vnfd.getVersion(),vnfr.getVersion());
			Assert.assertEquals(vnfd.getVendor(),vnfr.getVendor());
		}
	}

	private NetworkServiceDescriptor createNetworkServiceDescriptor() {
		NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
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
		final VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
		vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
		vdu.setComputation_requirement("high_requirements");
		VimInstance vimInstance = new VimInstance();
		vimInstance.setName("testVIM");
		vimInstance.setType("test");
		vdu.setVimInstance(vimInstance);
		virtualNetworkFunctionDescriptor
				.setVdu(new ArrayList<VirtualDeploymentUnit>() {
					{
						add(vdu);
					}
				});
		virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
		nsd.setVnfd(virtualNetworkFunctionDescriptors);
		return nsd;
	}

	@AfterClass
	public static void shutdown() {
		// TODO Teardown to avoid exceptions during test shutdown
	}

}
