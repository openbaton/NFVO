package org.project.neutrino.nfvo.repositories.tests;

import org.project.openbaton.nfvo.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.catalogue.mano.common.AbstractVirtualLink;
import org.project.openbaton.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

/**
 * Created by lto on 30/04/15.
 */
@SpringBootApplication
@EntityScan(basePackageClasses = {VimInstance.class, NetworkServiceDescriptor.class, AbstractVirtualLink.class, NetworkServiceRecord.class})
@ComponentScan(basePackageClasses = {org.project.neutrino.nfvo.repositories.NSDRepository.class, org.project.neutrino.nfvo.abstract_repositories.DatabaseRepository.class})
@EnableJpaRepositories(basePackageClasses = {org.project.neutrino.nfvo.repositories.NSDRepository.class, org.project.neutrino.nfvo.abstract_repositories.DatabaseRepository.class})
public class ApplicationTest {

    @Bean
    public DataSource dataSource() {
        // instantiate, configure and return embedded DataSource
        return new EmbeddedDatabaseBuilder().build();
    }

    /**
     * Testing if the context contains all the needed beans
     * @param argv
     */
    public static void main(String[] argv){

        ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
        for (String s : context.getBeanDefinitionNames())
            System.out.println(s);
    }
}
