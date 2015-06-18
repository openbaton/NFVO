package org.project.neutrino.nfvo.core.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.Path;
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
	/**
	 * Test function. Simply shows all bean names available
	 */
	@CliCommand(value = "list directory files", help = "Lists all files in the given directory. If no directory is given, lists the files from the current dir.")
	public String showDirectoryFiles(
            @CliOption(key = { "path" }, mandatory = false, unspecifiedDefaultValue="build/resources/main", specifiedDefaultValue="build/resources/main", help = "The image id to find.") final String path) {
		StringBuilder stringBuilder = new StringBuilder();

        // get the current path and its files
//        String currentPath = ;
//		File folder = new File(currentPath);
        File folder = new File(path);
		File[] folderFiles = folder.listFiles();
        Arrays.sort(folderFiles);

        // print files to console
		for (File file : folderFiles) {
			if (file.isFile()) {
                stringBuilder.append(file.getName() + SEPARATOR);
			}
		}
		return stringBuilder.toString();
	}


}
