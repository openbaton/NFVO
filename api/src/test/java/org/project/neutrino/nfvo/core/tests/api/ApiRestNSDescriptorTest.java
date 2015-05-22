package org.project.neutrino.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestNetworkService;
import org.project.neutrino.nfvo.api.exceptions.NSDNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRestNSDescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestNetworkService restNetworkService;

	@Mock
	NetworkServiceDescriptorManagement nsdManagement;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNSDFindAll() {

		log.info("" + nsdManagement.query());
		List<NetworkServiceDescriptor> list = nsdManagement.query();
		when(nsdManagement.query()).thenReturn(list);
		assertEquals(list, restNetworkService.findAll());
	}

	@Test
	public void testNSDCreate() {
		NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setId("123");
		networkServiceDescriptor.setVendor("Fokus");
		when(nsdManagement.onboard(networkServiceDescriptor)).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor networkServiceDescriptor2 = null;
		log.info("" + restNetworkService.create(networkServiceDescriptor));
		networkServiceDescriptor2 = restNetworkService
				.create(networkServiceDescriptor);

		assertEquals(networkServiceDescriptor, networkServiceDescriptor2);

	}

	@Test
	public void testNSDFindBy() {
		NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setId("123");
		networkServiceDescriptor.setVendor("Fokus");
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor,
				restNetworkService.findById(networkServiceDescriptor.getId()));
	}

	@Test
	public void testNSDUpdate() {
		NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setId("123");
		networkServiceDescriptor.setVendor("Fokus");
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, restNetworkService.update(
				networkServiceDescriptor, networkServiceDescriptor.getId()));
	}

	@Test
	public void testNSDDelete() {
		nsdManagement.delete(anyString());
		restNetworkService.delete(anyString());
	}
}
