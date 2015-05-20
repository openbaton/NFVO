package org.project.neutrino.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestConfiguration;
import org.project.neutrino.nfvo.catalogue.nfvo.Configuration;
import org.project.neutrino.nfvo.catalogue.nfvo.ConfigurationParameter;
import org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dbo
 *
 */

public class ApiRestConfigurationTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestConfiguration restConfiguration;

	@Mock
	ConfigurationManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testconfigurationFindAll() {

		log.info("" + mock.query());
		List<Configuration> list = mock.query();
		when(mock.query()).thenReturn(list);
		assertEquals(list, restConfiguration.findAll());
	}

	@Test
	public void testconfigurationCreate() {
		Configuration configuration = new Configuration();
		configuration.setId("123");
		ConfigurationParameter parameters = new ConfigurationParameter();
		parameters.setKey("test_key");
		parameters.setValue("test_value");
		configuration.getParameters().add(parameters);
		configuration.setName("configuration_test");
		when(mock.add(configuration)).thenReturn(configuration);
		log.info("" + restConfiguration.create(configuration));
		Configuration configuration2 = restConfiguration.create(configuration);
		assertEquals(configuration, configuration2);
	}

	@Test
	public void testconfigurationFindBy() {
		Configuration configuration = new Configuration();
		configuration.setId("123");
		ConfigurationParameter parameters = new ConfigurationParameter();
		parameters.setKey("test_key");
		parameters.setValue("test_value");
		configuration.getParameters().add(parameters);
		configuration.setName("configuration_test");
		when(mock.query(configuration.getId())).thenReturn(configuration);
		assertEquals(configuration, restConfiguration.findById(configuration.getId()));
	}

	@Test
	public void testconfigurationUpdate() {
		Configuration configuration = new Configuration();
		configuration.setId("123");
		ConfigurationParameter parameters = new ConfigurationParameter();
		parameters.setKey("test_key");
		parameters.setValue("test_value");
		configuration.getParameters().add(parameters);
		configuration.setName("configuration_test");
		when(mock.update(configuration, configuration.getId())).thenReturn(configuration);
		assertEquals(configuration, restConfiguration.update(configuration, configuration.getId()));
	}

	@Test
	public void testconfigurationDelete() {
		mock.delete("123");
		restConfiguration.delete("123");
	}
}
