package de.fhg.fokus.ngni.osco.api;

import de.fhg.fokus.ngni.nfvo.repository.mano.descriptor.NetworkServiceDescriptor;
import de.fhg.fokus.ngni.osco.api.model.TestClass;
import de.fhg.fokus.ngni.osco.interfaces.MyBean;
import de.fhg.fokus.ngni.osco.interfaces.NSDRepository;
import de.fhg.fokus.ngni.osco.interfaces.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private NSDRepository nsdRepository;

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
        return nsdRepository.create(nsd);
    }

    @RequestMapping("/send")
    public void send() {
        sender.simpleSend();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.class)
    String error(Exception exception) {exception.printStackTrace();return exception.getMessage();}

}