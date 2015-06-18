package org.project.neutrino.nfvo.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.shell.Bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Bridge for either executing the openbaton shell standalone or in an existing
 * spring boot environment that either leads to calls of the main() or the run()
 * method.
 */
@Component
public class SpringShellBridge implements CommandLineRunner {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * When running in spring boot application this implements the CommandLineRunner 
	 * and is executed after all the spring-shell components were loaded.
	 * 
	 * @param args
	 *            parameters for starting the shell and bootstrap
	 */
    @Override
    public void run(String... args) throws Exception {
		Bootstrap bootstrap = new Bootstrap();
        List<String> argumentList = new ArrayList<String>(Arrays.asList(args));
        bootstrap.main(argumentList.toArray(new String[0]));
    }
    
	/**
	 * Base start as a single module
	 * 
	 * @param args
	 *            parameters for starting the shell and bootstrap
	 */
    public static void main(String[] args) {
        SpringShellBridge springShellBridge = new SpringShellBridge();
        try {
            springShellBridge.run(args);
        } catch (Exception e) {
            springShellBridge.log.error(e.getMessage(), e);
        }
    }
}
