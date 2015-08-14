package org.project.openbaton.nfvo.plugin;

import org.project.openbaton.catalogue.nfvo.PluginAnswer;
import org.project.openbaton.catalogue.nfvo.PluginEndpoint;
import org.project.openbaton.catalogue.nfvo.PluginMessage;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.common.utils.AgentBroker;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.JMSException;
import java.lang.reflect.Method;

/**
 * Created by lto on 13/08/15.
 */
public abstract class PluginAgent extends org.project.openbaton.nfvo.common.interfaces.PluginAgent {

	@Autowired
	@Qualifier("pluginEndpointRepository")
	private GenericRepository<PluginEndpoint> pluginEndpointRepository;
    @Autowired
    private AgentBroker agentBroker;

    @Override
    public <T> T invokeMethod(Method method, Class inter, String type, Object... parameters) throws NotFoundException, PluginInvokeException {
        PluginEndpoint endpoint = getEndpoint(inter.getSimpleName(), type);

        log.debug("Destination is: " + endpoint.getEndpoint());

        PluginMessage message = null;

		Class<?>[] pType =  method.getParameterTypes();

		if(pType.length == parameters.length)
		{
			message = createMessageFromParameters(method.getName(), parameters);
		}

        //call a method of the plugin
        agentBroker.getSender(endpoint.getEndpointType()).send(endpoint.getEndpoint(), message);

        PluginAnswer answer = null;
        try {
            answer = (PluginAnswer) agentBroker.getReceiver(endpoint.getEndpointType()).receive(endpoint.getEndpoint());
        } catch (JMSException e) {
            e.printStackTrace();
            throw new PluginInvokeException(e);
        }

        return (T) answer.getAnswer();
	}

    private PluginEndpoint getEndpoint(String classname, String type) throws NotFoundException {
        for (PluginEndpoint endpoint :pluginEndpointRepository.findAll()){
            if (endpoint.getType().equals(type) && endpoint.getInterfaceClass().equals(classname)){
                return endpoint;
            }
        }
        throw new NotFoundException("No plugin endpoint found");
    }

    @Override
	public void register(PluginEndpoint endpoint) {
		log.debug("Registering endpoint: " + endpoint);
        pluginEndpointRepository.create(endpoint);
	}

	
    private PluginMessage createMessageFromParameters(String methodName, Object[] parameters)
	{
		
		String message = null;
		
		for(int i=0; i<parameters.length;i++)
		{
			message += parameters[i].toString();
		}
		
		
		return null;
		
	}
}