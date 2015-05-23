package org.project.neutrino.nfvo.core.test;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Configuration;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.core.NetworkServiceFaultManagement;
import org.project.neutrino.nfvo.core.utils.NSDUtils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static org.mockito.Mockito.mock;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = { NetworkServiceDescriptorManagement.class, NetworkServiceFaultManagement.class, NSDUtils.class})
public class ApplicationTest {

	@Bean
	GenericRepository<Configuration> configurationRepository(){
		return mock(GenericRepository.class);
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

	@Bean
	VimBroker vimBroker(){
		return mock(VimBroker.class);
	}

	public static void main(String[] argv) {
		ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
		for (String s : context.getBeanDefinitionNames())
			System.out.println(s);
	}
}
