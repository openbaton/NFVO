package org.project.neutrino.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestNetworkService;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dbo
 *
 */
public class ApiRestNSDescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestNetworkService restNetworkService;

	@Mock
	NetworkServiceDescriptorManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNSDFindAll() {

		log.info("" + mock.query());
		List<NetworkServiceDescriptor> list = mock.query();
		when(mock.query()).thenReturn(list);
		assertEquals(list, restNetworkService.findAll());
	}

	@Test
	public void testNSDCreate() {
		NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setId("123");
		networkServiceDescriptor.setVendor("Fokus");
		when(mock.onboard(networkServiceDescriptor)).thenReturn(
				networkServiceDescriptor);
		log.info("" + restNetworkService.create(networkServiceDescriptor));
		NetworkServiceDescriptor networkServiceDescriptor2 = restNetworkService
				.create(networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, networkServiceDescriptor2);
	}

	@Test
	public void testNSDFindBy() {
		NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setId("123");
		networkServiceDescriptor.setVendor("Fokus");
		when(mock.query(networkServiceDescriptor.getId())).thenReturn(
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
				mock.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, restNetworkService.update(
				networkServiceDescriptor, networkServiceDescriptor.getId()));
	}

	@Test
	public void testNSDDelete() {
		mock.delete("123");
		restNetworkService.delete("123");
	}
}
