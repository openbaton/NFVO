package org.project.neutrino.nfvo.common.vnfm;

import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.EndpointType;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.vnfm.interfaces.VNFLifecycleManagement;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

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
    public abstract void modify(VirtualNetworkFunctionRecord vnfr);

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract void terminate();

    protected void onAction(CoreMessage message) {
        log.trace("VNFM: Received Message: " + message.getAction());
        switch (message.getAction()){
            case INSTANTIATE_FINISH:
                break;
            case ALLOCATE_RESOURCES:
                break;
            case ERROR:
                break;
            case MODIFY:
                this.modify((VirtualNetworkFunctionRecord) message.getPayload());
                break;
            case RELEASE_RESOURCES:
                break;
            case INSTANTIATE:
                this.instantiate((VirtualNetworkFunctionRecord) message.getPayload());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        //TODO initialize and register

        loadProperties();

        VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpointType(EndpointType.JMS);
        log.debug("Registering to queue: vnfm-register");
        UtilsJMS.sendToRegister(vnfmManagerEndpoint);


//        Thread.sleep(10000);
//        CoreMessage coreMessage = new CoreMessage();
//        coreMessage.setAction(Action.INSTANTIATE_FINISH);
//        coreMessage.setObject(null);
//        UtilsJMS.sendToQueue(coreMessage, "vnfm-core-actions");
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
