package org.project.openbaton.nfvo.core.tests.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.common.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.api.RestVimInstances;
import org.project.openbaton.nfvo.core.interfaces.VimManagement;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;



public class ApiRestVimInstancesTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestVimInstances restVimInstances;

	@Mock
	private VimManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testdatacenterFindAll() {

		log.info("" + mock.query());
		List<VimInstance> list = mock.query();
		when(mock.query()).thenReturn(list);
		assertEquals(list, restVimInstances.findAll());
	}

	@Test
	public void testdatacenterCreate() throws VimException {
		VimInstance datacenter = new VimInstance();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.add(datacenter)).thenReturn(datacenter);
		log.info("" + restVimInstances.create(datacenter));
		VimInstance datacenter2 = restVimInstances.create(datacenter);
		assertEquals(datacenter, datacenter2);
		
	}

	@Test
	public void testdatacenterFindBy() {
		VimInstance datacenter = new VimInstance();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.query(datacenter.getId())).thenReturn(datacenter);
		assertEquals(datacenter, restVimInstances.findById(datacenter.getId()));
	}

	@Test
	public void testdatacenterUpdate() throws VimException {
		VimInstance datacenter = new VimInstance();
		datacenter.setId("123");
		datacenter.setName("DC-1");
		datacenter.setType("OpenStack");
		datacenter.setName("datacenter_test");
		when(mock.update(datacenter, datacenter.getId())).thenReturn(datacenter);
		assertEquals(datacenter, restVimInstances.update(datacenter, datacenter.getId()));
	}

	@Test
	public void testdatacenterDelete() {
		mock.delete("123");
		restVimInstances.delete("123");
	}
}
