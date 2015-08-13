package org.project.openbaton.nfvo.plugin;

import org.project.openbaton.catalogue.nfvo.PluginMessage;
import org.project.openbaton.nfvo.common.interfaces.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by lto on 13/08/15.
 */
@Service
@Scope
public class PluginAgent implements org.project.openbaton.nfvo.common.interfaces.PluginAgent {




	@Autowired
	private Sender sender;


    @Override
    public <T> T invokeMethod(Method method, Class inter, Object... parameters)
	{
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
        sender.send(destination,message);

        addManagerEndpoint



        return null;
	}

    @JmsListener(destination = "vnfm-register", containerFactory = "queueJmsContainerFactory")
    public void addManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        if (endpoint.getEndpointType() == null){
            endpoint.setEndpointType(EndpointType.JMS);
        }
        log.debug("Received: " + endpoint);
        this.register(endpoint);
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
