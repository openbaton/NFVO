package org.project.neutrino.nfvo.core.test;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Configuration;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.core.NetworkServiceFaultManagement;
import org.project.neutrino.nfvo.core.utils.NSDUtils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.project.neutrino.vnfm.interfaces.manager.VnfmManager;
import org.project.neutrino.vnfm.interfaces.register.VnfmRegister;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.AsyncResult;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = { NetworkServiceDescriptorManagement.class, NetworkServiceFaultManagement.class, NSDUtils.class})
@EnableJms
public class ApplicationTest {

	@Bean
	VnfmRegister vnfmRegister(){
		return mock(VnfmRegister.class);
	}

	@Bean
	VnfmManager vnfmManager(){
		return mock(VnfmManager.class);
	}


	@Bean
	GenericRepository<Configuration> configurationRepository(){
		return mock(GenericRepository.class);
	}
	@Bean
	NSDUtils nsdUtils(){
		return mock(NSDUtils.class);
	}

	@Bean(name = "NSRRepository")
	GenericRepository<NetworkServiceRecord> nsrRepository(){
		return mock(GenericRepository.class);
	}

	@Bean(name = "NSDRepository")
	GenericRepository<NetworkServiceDescriptor> nsdRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "imageRepository")
	GenericRepository<NFVImage> imageRepository() {
		return mock(GenericRepository.class);
	}


	@Bean(name = "vimRepository")
	GenericRepository<VimInstance> vimRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "vnfmEndpointRepository")
	GenericRepository<VnfmManagerEndpoint> vnfmManagerEndpointRepository() {
		return mock(GenericRepository.class);
	}

	@Bean
	VimBroker vimBroker() throws VimException {
		VimBroker mock = mock(VimBroker.class);
		ResourceManagement resourceManagement = mock(ResourceManagement.class);
		when(resourceManagement.allocate(any(VirtualDeploymentUnit.class), any(VirtualNetworkFunctionRecord.class))).thenReturn(new AsyncResult<String>("mocked-id"));
		when(mock.getVim(anyString())).thenReturn(resourceManagement);
		return mock;
	}

	public static void main(String[] argv) {
		ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
		for (String s : context.getBeanDefinitionNames())
			System.out.println(s);
	}
}
