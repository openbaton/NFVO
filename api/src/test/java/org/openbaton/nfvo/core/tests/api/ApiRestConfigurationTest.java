/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.nfvo.api.admin.RestConfiguration;
import org.openbaton.nfvo.core.interfaces.ConfigurationManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRestConfigurationTest {

  @InjectMocks private RestConfiguration restConfiguration;
  @Mock private ConfigurationManagement mock;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void configurationFindAll() {

    log.info("" + mock.query());
    Iterable<Configuration> list = mock.query();
    when(mock.queryByProject(anyString())).thenReturn(list);
    assertEquals(list, restConfiguration.findAll("project-id"));
  }

  @Test
  public void configurationCreate() {
    Configuration configuration = new Configuration();
    configuration.setId("123");
    ConfigurationParameter parameters = new ConfigurationParameter();
    parameters.setConfKey("test_key");
    parameters.setValue("test_value");
    configuration.setConfigurationParameters(new HashSet<ConfigurationParameter>());
    configuration.getConfigurationParameters().add(parameters);
    configuration.setName("configuration_test");
    when(mock.add(configuration)).thenReturn(configuration);

    log.info("" + restConfiguration.create(configuration, "project-id"));
    Configuration configuration2 = restConfiguration.create(configuration, "project-id");
    assertEquals(configuration, configuration2);
  }

  @Test
  public void configurationFindBy() {
    Configuration configuration = new Configuration();
    configuration.setId("123");
    ConfigurationParameter parameters = new ConfigurationParameter();
    parameters.setConfKey("test_key");
    parameters.setValue("test_value");
    configuration.setConfigurationParameters(new HashSet<ConfigurationParameter>());
    configuration.getConfigurationParameters().add(parameters);
    configuration.setName("configuration_test");
    when(mock.query(anyString(), anyString())).thenReturn(configuration);
    assertEquals(configuration, restConfiguration.findById(configuration.getId(), "project-id"));
  }

  @Test
  public void configurationUpdate() {
    Configuration configuration = new Configuration();
    configuration.setId("123");
    ConfigurationParameter parameters = new ConfigurationParameter();
    parameters.setConfKey("test_key");
    parameters.setValue("test_value");
    configuration.setConfigurationParameters(new HashSet<ConfigurationParameter>());
    configuration.getConfigurationParameters().add(parameters);
    configuration.setName("configuration_test");
    when(mock.update(any(configuration.getClass()), anyString(), anyString()))
        .thenReturn(configuration);
    assertEquals(
        configuration,
        restConfiguration.update(configuration, configuration.getId(), "project-id"));
  }

  @Test
  public void configurationDelete() {
    mock.delete("123");
    restConfiguration.delete("123", "project-id");
  }
}
