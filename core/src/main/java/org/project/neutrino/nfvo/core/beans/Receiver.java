package org.project.neutrino.nfvo.core.beans;

/**
 * Created by lto on 17/04/15.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;

@Component
@EnableJms
public class Receiver {

    private Logger log = LoggerFactory.getLogger(MyBean.class);

    /**
     * Get a copy of the application context
     */
    @Autowired
    ConfigurableApplicationContext context;

    /**
     * When you receive a message, print it out, then shut down the application.
     * Finally, clean up any ActiveMQ server stuff.
     */
    @JmsListener(destination = "mailbox-destination", containerFactory = "myJmsContainerFactory")
    public void receiveMessage(String message) {
        log.debug("Received <" + message + ">");
//        context.close();
        FileSystemUtils.deleteRecursively(new File("activemq-data"));
    }
}
