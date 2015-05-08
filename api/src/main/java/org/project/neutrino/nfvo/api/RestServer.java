package org.project.neutrino.nfvo.api;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;

import org.project.neutrino.nfvo.api.model.TestClass;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.core.interfaces.MyBean;
import org.project.neutrino.nfvo.core.interfaces.Sender;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RestServer {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ConfigurableApplicationContext context;

	@Autowired
	MyBean myBean;

	@Autowired
	Sender sender;

	@PostConstruct
	public void init() {
		// To be used to initialize stuff
		// for (String s : context.getBeanDefinitionNames())
		// log.debug(s);
	}

	@Autowired
	@Qualifier("NSDRepository")
	private GenericRepository<NetworkServiceDescriptor> nsdRepository;

	@Autowired
	ActiveMQProperties properties;

	@RequestMapping("/properties")
	public ActiveMQProperties properties() {
		return properties;
	}

	@RequestMapping("/")
	public TestClass home() {

		log.debug("######### " + myBean);
		TestClass tc = new TestClass();
		tc.setContent(myBean.myBean());
		tc.setName("MyName");
		return tc;
	}

	@RequestMapping("/close")
	public void close() {
		myBean.close();
	}

	@RequestMapping(value = "/network-service-descriptors", method = RequestMethod.GET)
	public List<NetworkServiceDescriptor> findAll() {

		return nsdRepository.findAll();
	}

	@RequestMapping(value = "/network-service-descriptors", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public NetworkServiceDescriptor create(
			@RequestBody NetworkServiceDescriptor nsd) {
		return (NetworkServiceDescriptor) nsdRepository.create(nsd);
	}

	@RequestMapping(value = "/network-service-descriptors", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public NetworkServiceDescriptor update(
			@RequestBody NetworkServiceDescriptor nsd) {
		return (NetworkServiceDescriptor) nsdRepository.merge(nsd);
	}

	@RequestMapping(value = "/network-service-descriptors/{id}", method = RequestMethod.DELETE)
	public void remove(@RequestBody String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = nsdRepository.find(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
		}
		nsdRepository.remove(nsd);
	}

	@RequestMapping("/send")
	public void send() {
		sender.simpleSend();
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(Exception.class)
	String error(Exception exception) {
		exception.printStackTrace();
		return exception.getMessage();
	}

}