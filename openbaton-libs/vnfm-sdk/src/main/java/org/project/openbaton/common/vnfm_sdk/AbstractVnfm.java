package org.project.openbaton.common.vnfm_sdk;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement {
    protected String type;
    protected String endpoint;
    protected Properties properties;
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected VnfmManagerEndpoint vnfmManagerEndpoint;
    protected static final String nfvoQueue = "vnfm-core-actions";
    protected VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    @PreDestroy
    private void shutdown(){
        this.unregister();
    }

    @PostConstruct
    private void init(){
        setup();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public abstract CoreMessage instantiate();

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
    public abstract CoreMessage modify();

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract CoreMessage terminate();

    public abstract CoreMessage handleError();

    protected void loadProperties() {
        Resource resource = new ClassPathResource("conf.properties");
        properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        this.endpoint = (String) properties.get("endpoint");
        this.type = (String) properties.get("type");
    }

    protected void onAction(CoreMessage message) {
        log.trace("VNFM: Received Message: " + message.getAction());
        this.virtualNetworkFunctionRecord = message.getPayload();
        CoreMessage coreMessage = null;
        switch (message.getAction()){
            case ALLOCATE_RESOURCES:
                break;
            case SCALE:
                this.scale();
                break;
            case SCALING:
                break;
            case ERROR:
                coreMessage = handleError();
                break;
            case MODIFY:
                coreMessage = this.modify();
                break;
            case RELEASE_RESOURCES:
                coreMessage = this.terminate();
                break;
            case GRANT_OPERATION:
            case INSTANTIATE:
                coreMessage = this.instantiate();
            case SCALE_UP_FINISHED:
                break;
            case SCALE_DOWN_FINISHED:
                break;
            case RELEASE_RESOURCES_FINISH:
                break;
            case INSTANTIATE_FINISH:
                break;
            case CONFIGURE:
                coreMessage = configure(message.getPayload());
        }


        if (coreMessage != null){
            log.debug("send to NFVO");
            sendToNfvo(coreMessage);
        }
    }

    protected abstract CoreMessage configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    protected abstract void sendToNfvo(CoreMessage coreMessage);

    /**
     * This method unregister the VNFM in the NFVO
     */
    protected abstract void unregister();
    /**
     * This method register the VNFM to the NFVO sending the right endpoint
     */
    protected abstract void register();

    /**
     * This method setups the VNFM and then register it to the NFVO. We recommend to not change this method or at least
     * to override calling super()
     */
    protected void setup(){
        loadProperties();
        vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpointType(EndpointType.JMS);
        register();
    }

    protected void sendToEmsAndUpdate(VirtualNetworkFunctionRecord vnfr, Event event, String command, String emsEndpoint) throws VnfmSdkException, JMSException {
        executeActionOnEMS(emsEndpoint, command);
        try {
            updateVnfr(vnfr, event, command);
            log.debug("Updated VNFR");
        }catch (NullPointerException e){
            throw new VnfmSdkException(e);
        }
    }
}
