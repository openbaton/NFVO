package org.project.neutrino.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

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

	@InjectMocks
	ConfigurationManagement configurationManagement;

	@InjectMocks
	NetworkServiceDescriptorManagement nsdManagement;

	@InjectMocks
	NFVImageManagement NFVImageManagement;

	@InjectMocks
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
