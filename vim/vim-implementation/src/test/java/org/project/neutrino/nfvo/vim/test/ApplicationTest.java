package org.project.neutrino.nfvo.vim.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by lto on 30/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {org.project.neutrino.nfvo.vim.client.openstack.PolicyManagement.class})
public class ApplicationTest {

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
