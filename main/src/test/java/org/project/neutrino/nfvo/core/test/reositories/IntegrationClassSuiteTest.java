package org.project.neutrino.nfvo.core.test.reositories;


import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.project.neutrino.nfvo.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {Application.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class IntegrationClassSuiteTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ConfigurableApplicationContext context;

    @BeforeClass
    public static void init(){
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    @Ignore
    public void method1(){
        log.info("Here the context");

        for (String s : context.getBeanDefinitionNames()){
            log.info(s);
        }

    }

    @After
    public void shutdown(){
        context.close();
    }

}
