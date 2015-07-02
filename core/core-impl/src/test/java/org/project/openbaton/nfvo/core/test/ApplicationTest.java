package org.project.openbaton.nfvo.core.test;

import org.project.openbaton.nfvo.catalogue.mano.common.VNFDependency;
import org.project.openbaton.nfvo.catalogue.mano.common.VNFRecordDependency;
import org.project.openbaton.nfvo.catalogue.mano.descriptor.*;
import org.project.openbaton.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.nfvo.catalogue.mano.record.VirtualLinkRecord;
import org.project.openbaton.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.core.api.NetworkServiceDescriptorManagement;
import org.project.openbaton.nfvo.core.core.NetworkServiceFaultManagement;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.project.openbaton.vnfm.interfaces.register.VnfmRegister;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.catalogue.nfvo.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.jms.JMSException;
import javax.naming.NamingException;

import static org.mockito.Matchers.any;
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
	VnfmManager vnfmManager() throws JMSException, NamingException, NotFoundException {
		VnfmManager vnfmManager = mock(VnfmManager.class);
		when(vnfmManager.deploy(any(NetworkServiceRecord.class))).thenReturn(new AsyncResult<Void>(null));
		return vnfmManager;
	}

	@Bean
	ClientInterfaces clientInterfaces(){return mock(ClientInterfaces.class);}

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

	@Bean(name = "VNFDependencyRepository")
	GenericRepository<VNFDependency> vnfDependencyRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "VNFDRepository")
	GenericRepository<VirtualNetworkFunctionDescriptor> vnfdRepository() {
		return mock(GenericRepository.class);

	}@Bean(name = "VNFRRepository")
	GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "VNFRDependencyRepository")
	GenericRepository<VNFRecordDependency> vnfRecordRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "networkRepository")
	GenericRepository<Network> networkRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "VNFFGDescriptorRepository")
	GenericRepository<VNFForwardingGraphDescriptor> vnffgDescriptorRepository() {
		return mock(GenericRepository.class);
	}

	@Bean(name = "vnfmEndpointRepository")
	GenericRepository<VnfmManagerEndpoint> vnfmManagerEndpointRepository() {
		return mock(GenericRepository.class);
	}
	@Bean(name = "virtualLinkDescriptorRepository")
	GenericRepository<VirtualLinkDescriptor> virtualLinkDescriptorRepository() {
		return mock(GenericRepository.class);
	}
	@Bean(name = "virtualLinkRecordRepository")
	GenericRepository<VirtualLinkRecord> virtualLinkRecordRepository() {
		return mock(GenericRepository.class);
	}

	@Bean
	ResourceManagement resourceManagement() { return mock(ResourceManagement.class); }

	@Bean
	Vim vim() throws VimException{
		Vim vim = mock(Vim.class);
		when(vim.allocate(any(VirtualDeploymentUnit.class), any(VirtualNetworkFunctionRecord.class))).thenReturn(new AsyncResult<String>("mocked-id"));
		return vim;
	}

	@Bean
	VimBroker vimBroker() throws VimException {
		VimBroker mock = mock(VimBroker.class);
		return mock;
	}

	public static void main(String[] argv) {
		ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
		for (String s : context.getBeanDefinitionNames())
			System.out.println(s);
	}
}
