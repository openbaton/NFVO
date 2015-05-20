package org.project.neutrino.nfvo.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by lto on 16/04/15.
 */
@Component
@Order
public class CLIBean implements CommandLineRunner{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ConfigurableApplicationContext configurableApplicationContext;

    @Override
    public void run(String... strings) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String input;
        System.out.print(">> ");
        boolean exit = false;
        while (!exit && (input = br.readLine()) != null) {
            System.out.print(">> ");

            switch (input.toLowerCase()) {
                case "exit":
                    configurableApplicationContext.close();
                    exit = true;
                    break;
                case "list beans":
                    for (String s: configurableApplicationContext.getBeanDefinitionNames())
                        log.info(s);
                default:


            }
        }
    }
}
