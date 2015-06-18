/* Tiziano Cecamore - 2015*/

package org.project.neutrino.integration.test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;

public class Authentication implements Closeable {
	final GlanceApi glanceApi;

	public static void main(String[] args) throws IOException {
		Authentication authentication = new Authentication(args);

		try {
			authentication.authenticateOnCall();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			authentication.close();
		}
	}

	public Authentication(String[] args) {

		Iterable<Module> modules = ImmutableSet
				.<Module> of(new SLF4JLoggingModule());

		String identity = FactoryCloudService.getIdentityOpenstack();
		String credential = FactoryCloudService.getCredentialOpenstack();

		Properties overrides = new Properties();
		overrides.put(KeystoneProperties.CREDENTIAL_TYPE,
				CredentialTypes.PASSWORD_CREDENTIALS);
		overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
		overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");

		glanceApi = ContextBuilder.newBuilder("openstack-glance")
				.endpoint(FactoryCloudService.getEndpointOpenstack())
				.credentials("admin" + ":" + identity, credential)
				.modules(modules).overrides(overrides)
				.buildApi(GlanceApi.class);

	}

	/**
	 * Calling getConfiguredRegions() causes jclouds to authenticate. If
	 * authentication doesn't work, the call to getConfiguredRegions() will
	 * result in an org.jclouds.rest.AuthorizationException
	 */
	private void authenticateOnCall() {
		System.out.format("Authenticate On Call%n");

		glanceApi.getConfiguredRegions();

		System.out.format("  Authenticated%n");
	}

	/**
	 * Always close your service when you're done with it.
	 */
	public void close() throws IOException {
		Closeables.close(glanceApi, true);
	}
}
