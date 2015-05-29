package org.project.neutrino.nfvo.dummy;

import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.vnfm.AbstractVnfmJMS;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 27/05/15.
 */
@Configuration
@ComponentScan
public class DummyJMSVNFManager extends AbstractVnfmJMS {

    @Override
    public void instantiate(VirtualNetworkFunctionRecord vnfr) {
        log.info("Instantiation of VirtualNetworkFunctionRecord " + vnfr.getName());
        log.trace("Instantiation of VirtualNetworkFunctionRecord " + vnfr);
        try {
            Thread.sleep((long) (Math.random() * 10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        VnfmManagerEndpoint endpoint = new VnfmManagerEndpoint();
        endpoint.setType("dummy");
        endpoint.setEndpoint("dummy-endpoint");
        endpoint.setEndpoinType("jms");

        try {
            UtilsJMS.sendToNFVO(endpoint);
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



    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DummyJMSVNFManager.class, args);
    }
}
