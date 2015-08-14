package org.project.openbaton.nfvo.plugin;

import org.project.openbaton.catalogue.nfvo.PluginAnswer;
import org.project.openbaton.catalogue.nfvo.PluginEndpoint;
import org.project.openbaton.catalogue.nfvo.PluginMessage;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.common.interfaces.Receiver;
import org.project.openbaton.nfvo.common.interfaces.Sender;
import org.project.openbaton.nfvo.common.utils.AgentBroker;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import java.lang.reflect.Method;

/**
 * Created by lto on 13/08/15.
 */
@Service
@Scope
public class PluginAgent extends org.project.openbaton.nfvo.common.interfaces.PluginAgent {

	@Autowired
	@Qualifier("pluginEndpointRepository")
	private GenericRepository<PluginEndpoint> pluginEndpointRepository;
    @Autowired
    private AgentBroker agentBroker;

    @Override
    public <T> T invokeMethod(Method method, Class inter, Object... parameters) throws NotFoundException, PluginInvokeException {
        PluginEndpoint endpoint = getEndpoint(inter.getSimpleName());
		PluginMessage message = null;
        String destination;
        if (inter.getSimpleName().equals("ClientInterfaces"))
            destination = "vim-driver-plugin";
        else if (inter.getSimpleName().equals("ResourcePerformanceManagement"))
            destination = "monitor-plugin";
        else
            throw new RuntimeException("No plugin interface found"); //TODO chose a good exception!

		Class<?>[] pType =  method.getParameterTypes();

		if(pType.length == parameters.length)
		{
			message = createMessageFromParameters(method.getName(), parameters);
		}

        //call a method of the plugin
        agentBroker.getSender(endpoint.getEndpointType()).send(destination, message);

        PluginAnswer answer = null;
        try {
            answer = (PluginAnswer) agentBroker.getReceiver(endpoint.getEndpointType()).receive(destination);
        } catch (JMSException e) {
            e.printStackTrace();
            throw new PluginInvokeException(e);
        }

        return (T) answer.getAnswer();
	}

    private PluginEndpoint getEndpoint(String type) throws NotFoundException {
        for (PluginEndpoint endpoint :pluginEndpointRepository.findAll()){
            if (endpoint.getType().equals(type)){
                return endpoint;
            }
        }
        throw new NotFoundException("No plugin endpoint found");
    }

    @Override
	public void register(PluginEndpoint endpoint) {
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
