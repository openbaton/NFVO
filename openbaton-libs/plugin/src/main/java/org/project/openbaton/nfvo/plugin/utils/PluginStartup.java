package org.project.openbaton.nfvo.plugin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lto on 10/09/15.
 */
public class PluginStartup {

    private static Logger log = LoggerFactory.getLogger(PluginStartup.class);

    private static Map<String, Process> processes = new HashMap<>();

    private static void installPlugin(String path, boolean waitForPlugin, String registryip, String port) throws IOException {
        String pluginName = path.substring(path.lastIndexOf("/") + 1, path.length());

//        StringTokenizer st = new StringTokenizer(pluginName, "-");
        String name = pluginName.substring(0,pluginName.indexOf("-"));
        log.trace("Running: java -jar " + path + " " + name + " localhost "+ port);
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", path, name, registryip, port);
        File file = new File("plugin-" + name + ".log");
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
        log.trace("ProcessBuilder is: " + processBuilder);
        Process p = processBuilder.start();
        if (waitForPlugin)
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        processes.put(path,p);
    }

    public static void startPluginRecursive(String folderPath, boolean waitForPlugin, String registryip, String port) throws IOException {

        File folder = new File(folderPath);

        if (folder.isDirectory()){
            for (File jar : folder.listFiles()) {
                if (jar.getAbsolutePath().endsWith(".jar"))
                    installPlugin(jar.getAbsolutePath(), waitForPlugin, registryip, port);
                else
                    if (jar.isDirectory())
                        startPluginRecursive(jar.getAbsolutePath(), waitForPlugin,registryip, port);
                    else
                        log.error(jar.getAbsolutePath() + " is not a jar file");
            }
        }else log.error(folderPath + " must be a folder");

    }

    public static void destroy(){
        for (Process p : processes.values()){
            p.destroy();
        }
    }
}
