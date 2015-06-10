package org.project.neutrino.nfvo.core.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * OpenBaton Commands implementation using the spring-shell library.
 */
@Component
public class BaseCommand implements CommandMarker {
	
	private static Logger log = LoggerFactory.getLogger("CLInterface");
	
	@Autowired
	private ConfigurableApplicationContext context;
	
	private boolean simpleCommandExecuted = false;
	
	/**
	 * Test function. Simply shows all bean names available
	 */
	@CliCommand(value = "show beans", help = "Show all bean names available")
	public String showBeanDefinitionNames() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			stringBuilder.append(beanDefinitionName + ";");
		}
		return stringBuilder.toString();
	}

}
