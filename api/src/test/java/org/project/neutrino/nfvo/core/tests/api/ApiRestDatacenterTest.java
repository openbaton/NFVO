package org.project.neutrino.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestDatacenter;
import org.project.neutrino.nfvo.catalogue.nfvo.Datacenter;
import org.project.neutrino.nfvo.core.interfaces.DatacenterManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dbo
 *
 */

public class ApiRestDatacenterTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestDatacenter restDatacenter;

	@Mock
	DatacenterManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testdatacenterFindAll() {

		log.info("" + mock.query());
		List<Datacenter> list = mock.query();
		when(mock.query()).thenReturn(list);
		assertEquals(list, restDatacenter.findAll());
	}

	@Test
	public void testdatacenterCreate() {
		Datacenter datacenter = new Datacenter();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.add(datacenter)).thenReturn(datacenter);
		log.info("" + restDatacenter.create(datacenter));
		Datacenter datacenter2 = restDatacenter.create(datacenter);
		assertEquals(datacenter, datacenter2);
	}

	@Test
	public void testdatacenterFindBy() {
		Datacenter datacenter = new Datacenter();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.query(datacenter.getId())).thenReturn(datacenter);
		assertEquals(datacenter, restDatacenter.findById(datacenter.getId()));
	}

	@Test
	public void testdatacenterUpdate() {
		Datacenter datacenter = new Datacenter();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.update(datacenter, datacenter.getId())).thenReturn(datacenter);
		assertEquals(datacenter, restDatacenter.update(datacenter, datacenter.getId()));
	}

	@Test
	public void testdatacenterDelete() {
		mock.delete("123");
		restDatacenter.delete("123");
	}
}
