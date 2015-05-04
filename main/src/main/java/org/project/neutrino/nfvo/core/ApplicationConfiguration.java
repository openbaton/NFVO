package org.project.neutrino.nfvo.core;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lto on 04/05/15.
 */
@Configuration
public class ApplicationConfiguration {


    @Bean
    public BrokerService brokerService() {
        BrokerService broker = new BrokerService();

        // configure the broker
        try {
            broker.addConnector("tcp://localhost:61616");
//            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }
}
