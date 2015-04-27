package de.fhg.fokus.ngni.osco.tests.api;

import de.fhg.fokus.ngni.osco.interfaces.MyBean;
import de.fhg.fokus.ngni.osco.interfaces.NSDRepository;
import de.fhg.fokus.ngni.osco.interfaces.Sender;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.sound.midi.Receiver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = de.fhg.fokus.ngni.osco.api.RestServer.class)
public class ApplicationTEST {

    @Bean
    MyBean myBean(){
        MyBean myBean = mock(MyBean.class);
        when(myBean.myBean()).thenReturn("This is the mocked content");
        return myBean;
    }

    @Bean
    Sender sender(){
        return mock(Sender.class);
    }

    @Bean
    Receiver receiver(){
        return mock(Receiver.class);
    }

    @Bean
    NSDRepository nsdRepository(){
        return mock(NSDRepository.class);
    }
}
