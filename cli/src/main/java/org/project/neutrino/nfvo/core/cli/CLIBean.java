package org.project.neutrino.nfvo.core.cli;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lto on 16/04/15.
 */
@Component
@Order
public class CLIBean implements CommandLineRunner{

    private static Logger log = LoggerFactory.getLogger("CLIterface");

    @Autowired
    ConfigurableApplicationContext context;

    private final static Map<String, String> commandList = new HashMap<String, String>(){{
        put("help", "Print the usage");
        put("exit", "Exit the application");
        put("list_beans", "List all available beans in the running framework");
    }};

    public static void usage() {
        log.info("Usage: java -jar build/libs/neutrino-<$version>.jar");
        log.info("Available commands are");
        for (Map.Entry<String, String> entry : commandList.entrySet()) {
            log.info("\t" + entry.getKey() + ":\t" + entry.getValue());
        }
    }

    @Override
    public void run(String... args) throws Exception {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String username = System.getProperty("user.name");
//        String input;
//        System.out.print(">> ");
//        boolean exit = false;
//        while (!exit && (input = br.readLine()) != null) {
//            System.out.print(">> ");
//            switch (input.toLowerCase()) {
//                case "exit":
//                    context.close();
//                    exit = true;
//                    break;
//                case "list beans":
//                    for (String s: context.getBeanDefinitionNames())
//                        log.info(s);
//                default:
//
//
//            }
//        }

        try {
            Character mask = null;
            String trigger = null;
            boolean color = true;

            ConsoleReader reader = new ConsoleReader();

            reader.setPrompt("\u001B[135m"+ username + "@[\u001B[32mopen-baton\u001B[0m]~> ");

            if ((args == null) || (args.length == 0)) {
                usage();
           }

            reader.addCompleter(new StringsCompleter(commandList.keySet()));
            String line;
            PrintWriter out = new PrintWriter(reader.getOutput());

            while ((line = reader.readLine()) != null) {
                out.flush();
                line = line.trim();
                // If we input the special word then we will mask
                // the next line.
//                if ((trigger != null) && (line.compareTo(trigger) == 0)) {
//                    line = reader.readLine("password> ", mask);
//                }
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    context.close();
                    break;
                }
                if (line.equalsIgnoreCase("cls")) {
                    reader.clearScreen();
                }
                if (line.equalsIgnoreCase("help")) {
                    usage();
                }
                if (line.equalsIgnoreCase("list_beans")) {
                    for (String s : context.getBeanDefinitionNames())
                        log.info(s);
                }
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
