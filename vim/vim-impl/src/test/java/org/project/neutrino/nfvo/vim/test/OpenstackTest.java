package org.project.neutrino.nfvo.vim.test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Created by mpa on 07.05.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class OpenstackTest {

    @Test
    public void test1(){
        // TODO Tests...
    }
}
