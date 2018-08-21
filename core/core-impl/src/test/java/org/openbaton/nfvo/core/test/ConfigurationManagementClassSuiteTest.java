/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.api.ConfigurationManagement;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 20/04/15. */
public class ConfigurationManagementClassSuiteTest {

  private static final String projectId = "project-id";

  @Rule public ExpectedException exception = ExpectedException.none();
  private final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  @InjectMocks private ConfigurationManagement configurationManagement;

  @Mock private ConfigurationRepository configurationRepository;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    log.info("Starting test");
  }

  @Test
  public void configurationManagementNotNull() {
    Assert.assertNotNull(configurationManagement);
  }

  @Test
  public void nfvImageManagementUpdateTest() throws NotFoundException {
    Configuration configutation = createConfigutation();
    Configuration configuration2 = createConfigutation();
    configuration2.setName("UpdatedName");
    ConfigurationParameter configurationParameter = new ConfigurationParameter();
    configurationParameter.setConfKey("new_key");
    configurationParameter.setValue("new_value");
    configuration2.getConfigurationParameters().add(configurationParameter);
    when(configurationRepository.save(any(Configuration.class))).thenReturn(configuration2);
    when(configurationRepository.findFirstByIdAndProjectId(anyString(), eq(projectId)))
        .thenReturn(configuration2);
    configutation =
        configurationManagement.update(configuration2, configutation.getId(), projectId);
    assertEqualsConfiguration(configutation, configuration2);
  }

  private void assertEqualsConfiguration(
      Configuration configuration_exp, Configuration configuration_new) {
    Assert.assertEquals(configuration_exp.getName(), configuration_new.getName());
    int i = 0;
    for (ConfigurationParameter configurationParameter :
        configuration_exp.getConfigurationParameters()) {
      ConfigurationParameter[] parameters = new ConfigurationParameter[10];
      Assert.assertEquals(
          configurationParameter.getConfKey(),
          configuration_new.getConfigurationParameters().toArray(parameters)[i].getConfKey());
      Assert.assertEquals(
          configurationParameter.getValue(),
          configuration_new.getConfigurationParameters().toArray(parameters)[i].getValue());
      i++;
    }
  }

  private Configuration createConfigutation() {
    Configuration configuration = new Configuration();
    configuration.setProjectId(projectId);
    configuration.setName("configuration_name");
    configuration.setConfigurationParameters(
        new HashSet<ConfigurationParameter>() {
          {
            ConfigurationParameter configurationParameter = new ConfigurationParameter();
            configurationParameter.setConfKey("key");
            configurationParameter.setValue("value");
            add(configurationParameter);
          }
        });
    return configuration;
  }

  private void assertEqualsNetwork(Network network_exp, Network network_new) {
    Assert.assertEquals(network_exp.getName(), network_new.getName());
    Assert.assertEquals(network_exp.getExtId(), network_new.getExtId());
    Assert.assertEquals(network_exp.getExternal(), network_new.getExternal());
    Assert.assertEquals(network_exp.getExtShared(), network_new.getExtShared());
    Assert.assertEquals(network_exp.getSubnets().size(), network_new.getSubnets().size());
  }

  @Test
  public void configurationManagementAddTest() {
    Configuration configuration_exp = createConfigutation();
    when(configurationRepository.save(any(Configuration.class))).thenReturn(configuration_exp);
    Configuration configuration_new = configurationManagement.add(configuration_exp);

    assertEqualsConfiguration(configuration_exp, configuration_new);
  }

  @Test
  public void configurationManagementQueryTest() {
    when(configurationRepository.findAll()).thenReturn(new ArrayList<>());

    Assert.assertEquals(false, configurationManagement.query().iterator().hasNext());

    Configuration configutation_exp = createConfigutation();
    when(configurationRepository.findOne(configutation_exp.getId())).thenReturn(configutation_exp);
    when(configurationRepository.findFirstByIdAndProjectId(configutation_exp.getId(), projectId))
        .thenReturn(configutation_exp);
    Configuration configuration_new =
        configurationManagement.query(configutation_exp.getId(), projectId);
    assertEqualsConfiguration(configutation_exp, configuration_new);
  }

  @Test
  public void configurationManagementDeleteTest() {
    Configuration configuration_exp = createConfigutation();
    when(configurationRepository.findFirstById(anyString())).thenReturn(configuration_exp);
    configurationManagement.delete(configuration_exp.getId());
    when(configurationRepository.findFirstById(anyString())).thenReturn(null);
    Configuration configuration_new =
        configurationManagement.query(configuration_exp.getId(), projectId);
    Assert.assertNull(configuration_new);
  }
}
