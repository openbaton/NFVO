package org.project.neutrino.nfvo.common.vnfm;

import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.vnfm.interfaces.VNFLifecycleManagement;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;
import java.util.Properties;

import static org.project.neutrino.nfvo.catalogue.nfvo.Action.INSTATIATE;

/**
 * Created by lto on 28/05/15.
 */

@Component
@Order
public abstract class AbstractVnfmJMS implements CommandLineRunner, VNFLifecycleManagement {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected String type;
    protected String endpoint;

    @Override
    public abstract void instantiate(VirtualNetworkFunctionRecord vnfr);

    @Override
    public abstract void query();

    @Override
    public abstract void scale();

    @Override
    public abstract void checkInstantiationFeasibility();

    @Override
    public abstract void heal();

    @Override
    public abstract void updateSoftware();

    @Override
    public abstract void modify();

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract void terminate();

    protected void onAction(CoreMessage message) {
        log.trace("Inside ONMESSAGE");
        switch (message.getAction()){
            case INSTATIATE:
                this.instantiate((VirtualNetworkFunctionRecord) message.getObject());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        //TODO initialize and register

        loadProperties();

        VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpoinType("jms");
        log.debug("Registering to queue: vnfm-register");
        UtilsJMS.sendToQueue(vnfmManagerEndpoint,"vnfm-register");
    }

    private void loadProperties() {
            Resource resource = new ClassPathResource("conf.properties");
            Properties properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        this.endpoint = (String) properties.get("endpoint");
        this.type = (String) properties.get("type");
    }
}
