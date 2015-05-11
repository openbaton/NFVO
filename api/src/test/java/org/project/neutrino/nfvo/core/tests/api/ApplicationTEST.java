package org.project.neutrino.nfvo.core.tests.api;

import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sound.midi.Receiver;

import static org.mockito.Mockito.mock;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.project.neutrino.nfvo")
@EnableJpaRepositories(basePackageClasses = {GenericRepository.class})
public class ApplicationTEST {

    private static Logger log = LoggerFactory.getLogger(ApplicationTEST.class);

    @Bean
    GenericRepository genericRepository(){ return mock(GenericRepository.class);}

    @Bean
    Receiver receiver(){
        return mock(Receiver.class);
    }

    public static void main(String[] argv){

        ConfigurableApplicationContext context = SpringApplication.run(ApplicationTEST.class);


    }
}
