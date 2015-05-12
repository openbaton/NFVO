package org.project.neutrino.nfvo.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by lto on 12/05/15.
 */
@Component
@Order(value = 1)
class SystemStartup implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing NEUTRINO");
        ClassPathResource classPathResource = new ClassPathResource("neutrino.properties");
        Properties properties = new Properties();
        properties.load(classPathResource.getInputStream());

        log.debug("Config Values are: " + properties.values());
    }
}
