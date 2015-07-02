package org.project.openbaton.nfvo.core.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * OpenBaton Commands implementation using the spring-shell library.
 */
@Component
public class Base implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String CURRENT_PATH = Paths.get(".").toAbsolutePath().normalize().toString();
    private final String SEPARATOR = "\t";
	
	@Autowired
	private ConfigurableApplicationContext context;
	
	/**
	 * Test function. Simply shows all bean names available
	 */
	@CliCommand(value = "show beans", help = "Show all bean names available")
	public String showBeanDefinitionNames() {
		StringBuilder stringBuilder = new StringBuilder();
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        Arrays.sort(beanDefinitionNames);
		for (String beanDefinitionName : beanDefinitionNames) {
			stringBuilder.append(beanDefinitionName + SEPARATOR);
		}
		return stringBuilder.toString();
	}
}
