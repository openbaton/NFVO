package org.project.openbaton.nfvo.core.tests.api;


import static org.mockito.Mockito.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


import javax.sound.midi.Receiver;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.project.neutrino.nfvo")

public class ApplicationTEST {

    private static Logger log = LoggerFactory.getLogger(ApplicationTEST.class);


    @Bean
    Receiver receiver(){
        return mock(Receiver.class);
    }

    public static void main(String[] argv){
//        ConfigurableApplicationContext context =
                SpringApplication.run(ApplicationTEST.class);


    }
}
