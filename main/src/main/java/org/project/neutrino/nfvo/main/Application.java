package org.project.neutrino.nfvo.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.util.FileSystemUtils;

import java.io.File;

/**
 * Created by lto on 16/04/15.
 */


@SpringBootApplication
@EnableJms
@EntityScan(basePackages="org.project.neutrino.nfvo.catalogue.mano")
@ComponentScan(basePackages = "org.project.neutrino.nfvo")
public class Application {



    public static void main(String[] args) {
        // Clean out any ActiveMQ data from a previous run
        FileSystemUtils.deleteRecursively(new File("activemq-data"));
        Logger log = LoggerFactory.getLogger(Application.class);

        log.info("Start Neutrino");
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Bye!");
    }

}