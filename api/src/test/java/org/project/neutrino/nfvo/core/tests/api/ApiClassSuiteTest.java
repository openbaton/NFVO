package org.project.neutrino.nfvo.core.tests.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTEST.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
@WebAppConfiguration
public class ApiClassSuiteTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.ctx).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Ignore
    public void firstExampleTest() {

        // TESTClass
        TestClass tc = new TestClass();
        tc.setName("name");
        tc.setContent("content");



        log.info("Starting test");

        try {
            this.mockMvc.perform(get("/").accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.name").value("MyName")) // Defined inside RestService
                    .andExpect(jsonPath("$.content").value("This is the mocked content"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
