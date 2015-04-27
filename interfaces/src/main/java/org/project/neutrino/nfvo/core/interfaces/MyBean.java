package org.project.neutrino.nfvo.core.interfaces;

import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;


/**
 * Created by lto on 17/04/15.
 */
public interface MyBean {
    String myBean();

    String send();

//    @Bean // Strictly speaking this bean is not necessary as boot creates a default
    JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory);

    void close();
}
