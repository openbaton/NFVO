/* Tiziano Cecamore - 2015*/

package org.project.neutrino.integration.test;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.util.Properties;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestImage;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.project.neutrino.nfvo.main.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.glance.v1_0.features.ImageApi;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.jclouds.openstack.nova.v2_0.NovaApi;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

@ContextConfiguration(classes = { Application.class,
		IntegrationTestConfiguration.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class TestImage {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ConfigurableApplicationContext context;

	// prova test rest image
	@InjectMocks
	RestImage restImage;

	@Mock
	NFVImageManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	// prova test rest image
	@Test
	public void testImageCreate() {
		log.info("################################# TEST IMAGE #############################################");
		NFVImage image = new NFVImage();
		image.setId("123");
		image.setMinCPU("1");
		image.setMinRam(1000);
		image.setName("Image_test");

		when(mock.add(image)).thenReturn(image);
		log.info("" + restImage.create(image));

		when(mock.query(image.getId())).thenReturn(image);
		assertEquals(image, restImage.findById(image.getId()));
	}

	@Test
	public void testImageCreateClient() {

		GlanceApi glanceApi;
		NovaApi novaApi;

		Set<String> zones;
		String defaultZone = null;

		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());

		String provider = FactoryCloudService.getProviderOpenstack();
		String identity = FactoryCloudService.getIdentityOpenstack();
		String credential = FactoryCloudService.getCredentialOpenstack();

		Properties overrides = new Properties();
		overrides.put(KeystoneProperties.CREDENTIAL_TYPE,
				CredentialTypes.PASSWORD_CREDENTIALS);
		overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
		overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");

		glanceApi = ContextBuilder.newBuilder(provider)
				.endpoint(FactoryCloudService.getEndpointOpenstack())
				.credentials("admin" + ":" + identity, credential)
				.modules(modules).overrides(overrides)
				.buildApi(GlanceApi.class);

		novaApi = ContextBuilder.newBuilder("openstack-nova")
				.endpoint(FactoryCloudService.getEndpointOpenstack())
				.credentials("admin" + ":" + identity, credential)
				.modules(modules).overrides(overrides).buildApi(NovaApi.class);

		zones = novaApi.getConfiguredRegions();
		if (null == defaultZone) {
			defaultZone = zones.iterator().next();
		}

		@SuppressWarnings("unused")
		boolean find = false;

		ImageApi img = glanceApi.getImageApi(defaultZone);
		for (org.jclouds.openstack.glance.v1_0.domain.Image i : img.list()
				.concat()) {
			if (i.getId().equalsIgnoreCase("123"))
				find = true;
		}

		// Assert.assertTrue(find);

	}

}
