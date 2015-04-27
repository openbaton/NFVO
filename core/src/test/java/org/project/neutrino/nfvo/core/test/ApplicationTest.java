package org.project.neutrino.nfvo.core.test;

import org.project.neutrino.nfvo.core.repositories.NSDRepositoryImpl;
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
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@EntityScan(basePackages = "de.fhg.fokus.ngni.nfvo.repository.mano")
@ComponentScan(basePackages = "de.fhg.fokus.ngni.osco.repositories")
@EnableJpaRepositories(basePackageClasses = NSDRepositoryImpl.class)
public class ApplicationTest {

    @Bean
    public DataSource dataSource() {
        // instantiate, configure and return embedded DataSource
        return new EmbeddedDatabaseBuilder().build();
    }

    public static void main(String[] argv){

        ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
        for (String s : context.getBeanDefinitionNames())
            System.out.println(s);

    }
}
