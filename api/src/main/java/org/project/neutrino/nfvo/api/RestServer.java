package org.project.neutrino.nfvo.api;

import org.project.neutrino.nfvo.api.model.TestClass;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.core.interfaces.MyBean;
import org.project.neutrino.nfvo.core.interfaces.Sender;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@RequestMapping("/")
public class RestServer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MyBean myBean;

    @Autowired
    Sender sender;

    @Autowired
    private GenericRepository<NetworkServiceDescriptor> nsdRepository;

    @Autowired
    ActiveMQProperties properties;

    @RequestMapping("/properties")
    public ActiveMQProperties properties(){
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

    @RequestMapping("/network-service-descriptors")
    public List<NetworkServiceDescriptor> findAll() {

        return nsdRepository.findAll();
    }

    @RequestMapping(value = "/network-service-descriptors", method = RequestMethod.POST)
    public NetworkServiceDescriptor create(@RequestBody NetworkServiceDescriptor nsd) {
        return (NetworkServiceDescriptor) nsdRepository.create(nsd);
    }

    @RequestMapping("/send")
    public void send() {
        sender.simpleSend();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.class)
    String error(Exception exception) {exception.printStackTrace();return exception.getMessage();}

}