package org.project.neutrino.nfvo.dummy;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.common.vnfm.AbstractVnfmJMS;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 27/05/15.
 */
@Configuration
@EnableJms
public class DummyJMSVNFManager extends AbstractVnfmJMS {

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    private static final String SELECTOR = "dummy-endpoint";

    @Override
    public void instantiate(VirtualNetworkFunctionRecord vnfr) {
        log.info("Instantiation of VirtualNetworkFunctionRecord " + vnfr.getName());
        log.trace("Instantiation of VirtualNetworkFunctionRecord " + vnfr);
        try {
            Thread.sleep((long) (Math.random() * 10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.INSTATIATE_FINISH);
        try {
            UtilsJMS.sendToQueue(coreMessage, "vnfm-core-actions");
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void query() {

    }

    @Override
    public void scale() {

    }

    @Override
    public void checkInstantiationFeasibility() {

    }

    @Override
    public void heal() {

    }

    @Override
    public void updateSoftware() {

    }

    @Override
    public void modify() {

    }

    @Override
    public void upgradeSoftware() {

    }

    @Override
    public void terminate() {

    }

    @JmsListener(destination = "core-vnfm-actions", selector = "type = \'" + SELECTOR + "\'", containerFactory = "myJmsContainerFactory")
    public void onMessage(CoreMessage message){
        this.onAction(message);
    }

    @Bean
    JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    public static void main(String[] args) {
        System.out.println("type=\"" + SELECTOR + "\"");
        ConfigurableApplicationContext context = SpringApplication.run(DummyJMSVNFManager.class, args);
    }
}
