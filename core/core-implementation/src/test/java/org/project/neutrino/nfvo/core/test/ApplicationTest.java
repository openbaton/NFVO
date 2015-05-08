package org.project.neutrino.nfvo.core.test;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@EntityScan(basePackageClasses = {
		org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor.class,
		org.project.neutrino.nfvo.catalogue.mano.common.AbstractVirtualLink.class,
		org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord.class })
@ComponentScan(basePackageClasses = { org.project.neutrino.nfvo.core.beans.MyBean.class })
public class ApplicationTest {

	@Bean
	public DataSource dataSource() {
		// instantiate, configure and return embedded DataSource
		return new EmbeddedDatabaseBuilder().build();
	}

	public static void main(String[] argv) {

		ConfigurableApplicationContext context = SpringApplication
				.run(ApplicationTest.class);
		for (String s : context.getBeanDefinitionNames())
			System.out.println(s);

	}
}
