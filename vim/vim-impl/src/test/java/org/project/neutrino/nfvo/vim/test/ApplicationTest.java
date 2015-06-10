package org.project.neutrino.nfvo.vim.test;

import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static org.mockito.Mockito.mock;

/**
 * Created by lto on 30/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {org.project.neutrino.nfvo.vim.client.openstack.OpenstackClient.class, org.project.neutrino.nfvo.vim.client.amazon.AmazonClient.class, org.project.neutrino.nfvo.vim.client.test.TestClient.class, org.project.neutrino.nfvo.vim.TestVIM.class})
public class ApplicationTest {

    @Bean
    ClientInterfaces openstackClient(){
        ClientInterfaces clientInterfaces = mock(ClientInterfaces.class);

        return clientInterfaces;
    }

    /**
     * Testing if the context contains all the needed api
     * @param argv
     */
    public static void main(String[] argv){

        ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
        for (String s : context.getBeanDefinitionNames())
            System.out.println(s);
    }
}
