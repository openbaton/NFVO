package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.nfvo.Configuration;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.nfvo.common.exceptions.PluginInstallException;
import org.project.openbaton.nfvo.core.interfaces.ConfigurationManagement;
import org.project.openbaton.nfvo.vim_interfaces.monitoring.MonitoringBroker;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by lto on 21/07/15.
 */
@Service
@Scope
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class PluginInstaller implements CommandLineRunner {

    private Map<String, Process> processes;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private VimBroker vimBroker;
    @Autowired
    private MonitoringBroker monitoringBroker;
    @Autowired
    private ConfigurationManagement configurationManagement;

    @PostConstruct
    private void init() {
        this.processes = new HashMap<>();
    }

    public void installVimPlugin(String path, List<String> classes) throws PluginInstallException {
        /**
         * Usual checks
         */
        checkJar(path);
        /**
         * Checking version
         */
        String pluginName = "plugin-vim-drivers";

        try {
            checkScreen();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        }

        /**
         * Run it into the screen session
         */
        try {
            //screen -S openbaton -p 0 -X screen -t  openstack-plugin exec java -jar /opt/openbaton/openstack-plugin/build/libs/openstack-driver-0.5-SNAPSHOT.jar
            String command = "screen -S openbaton -p 0 -X screen -t " + pluginName + " java -jar " + path;
            log.debug("Running command: " + command);

            Process plugin = Runtime.getRuntime().exec(command);
//            Runtime.getRuntime().exec("screen -p 0");

//           Runtime.getRuntime().exec("screen -d openbaton");
//            Runtime.getRuntime().exec("screen -r openbaton -p 0");
            this.processes.put(pluginName, plugin);
            BufferedReader stdError = new BufferedReader(new InputStreamReader(plugin.getErrorStream()));
            String s;
            while ((s = stdError.readLine()) != null) {
                log.error(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        }

    }

    private void checkScreen() throws IOException, InterruptedException {
        Process screen = Runtime.getRuntime().exec("screen -ls");
        screen.waitFor();
        int exitValue = screen.exitValue();
        log.debug("screen -ls exit value: " + exitValue);
        if (exitValue != 0){
            Runtime.getRuntime().exec("screen -d -m -S openbaton");
        }
    }

    private void checkJar(String path) throws PluginInstallException {
        if (!path.endsWith(".jar")) {
            throw new PluginInstallException("The file must be a valid jar plugin");
        }
        File file = new File(path);
        if (!file.isFile()) {
            throw new PluginInstallException("The file must be a valid jar plugin");
        }
    }

    public void installMonitoringPlugin(String path, List<String> classes) throws PluginInstallException {
        /**
         * Usual checks
         */
        checkJar(path);
        /**
         * Checking version
         */
        String pluginName = "plugin-monitoring";

        try {
            checkScreen();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        }
        /**
         * Run it into the screen session
         */
        try {
            Process plugin = Runtime.getRuntime().exec("screen -S openbaton -p 0 -X screen -t " + pluginName + " java -jar " + path);
//            Runtime.getRuntime().exec("screen -p 0");
//         Runtime.getRuntime().exec("screen -d openbaton");
//            Runtime.getRuntime().exec("screen -r -p 0");
            this.processes.put(pluginName, plugin);
        } catch (IOException e) {
            e.printStackTrace();
            throw new PluginInstallException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {

        List<String> classes = new ArrayList<>();
        String installFolderPath = null;
        Configuration system = configurationManagement.queryByName("system");


        for (ConfigurationParameter cp : system.getConfigurationParameters()) {
            if (cp.getConfKey().equals("vim-plugin-installation-dir")) {
                installFolderPath = cp.getValue();
            }
            if (cp.getConfKey().equals("vim-classes")) {
                classes = Arrays.asList(cp.getValue().split(";"));
            }
        }
        File[] files;
        File folder = new File(installFolderPath);
        if (folder != null || !folder.exists()) {
            files = folder.listFiles();

            if (files.length > 0) {
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(".jar")) {
                        this.installVimPlugin(path, classes);
                    }
                }
            }
        } else
            log.error("Folder " + installFolderPath + " not found");

        for (ConfigurationParameter cp : system.getConfigurationParameters()) {
            if (cp.getConfKey().equals("monitoring-plugin-installation-dir")) {
                installFolderPath = cp.getValue();
            }
            if (cp.getConfKey().equals("monitoring-classes")) {
                classes = Arrays.asList(cp.getValue().split(";"));
            }
        }

        folder = new File(installFolderPath);
        if (folder != null || !folder.exists()) {
            files = folder.listFiles();

            if (files.length > 0) {
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(".jar")) {
                        this.installMonitoringPlugin(path, classes);
                    }
                }
            }
        } else
            log.error("Folder " + installFolderPath + " not found");
    }
}