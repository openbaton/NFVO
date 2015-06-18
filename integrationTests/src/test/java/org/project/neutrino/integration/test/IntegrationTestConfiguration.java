/* Tiziano Cecamore - 2015*/
package org.project.neutrino.integration.test;

import javax.sql.DataSource;

import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@SpringBootApplication
@EntityScan(basePackageClasses = {
		org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor.class,
		org.project.neutrino.nfvo.catalogue.nfvo.VimInstance.class,
		org.project.neutrino.nfvo.catalogue.mano.common.AbstractVirtualLink.class,
		org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord.class })
@EnableJpaRepositories(basePackageClasses = {GenericRepository.class})
public class IntegrationTestConfiguration {
	@Bean
	public DataSource dataSource() {
		// instantiate, configure and return embedded DataSource
		return new EmbeddedDatabaseBuilder().build();
	}
	
}
