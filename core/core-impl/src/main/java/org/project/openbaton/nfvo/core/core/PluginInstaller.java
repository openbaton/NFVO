package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.nfvo.Configuration;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.monitoring.interfaces.ResourcePerformanceManagement;
import org.project.openbaton.nfvo.exceptions.PluginInstallException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vim_interfaces.monitoring.MonitoringBroker;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Created by lto on 21/07/15.
 */
@Service
@Scope
public class PluginInstaller implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VimBroker vimBroker;

    @Autowired
    private MonitoringBroker monitoringBroker;

    @Autowired
    @Qualifier("configurationRepository")
    private GenericRepository<Configuration> configurationRepository;


    public void installVimDriverPlugin(String path) throws PluginInstallException {

        File jar = new File(path);
        if (!jar.exists())
            throw new PluginInstallException(path + " does not exist");

        ClassLoader parent = ClassUtils.getDefaultClassLoader();
        path = jar.getAbsolutePath();
        try {
            log.trace("path is: " + path);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file://" + path)}, parent);

            URL url = null;
            String type = null;
            if (url == null) {
                url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/test/TestClient.class");
                type = "test";
            }
            if (url == null){
                url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/openstack/OpenstackClient.class");
                type = "openstack";
            }
            if (url == null){
                url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/amazon/AmazonClient.class");
                type = "amazon";
            }
            if (url == null)
                throw new PluginInstallException("No ClientInterfaces known were found");

            log.trace("URL: " + url.toString());
            log.trace("type is: " + type);
            switch (type){
                case "test":
                    Class c = classLoader.loadClass("org.project.openbaton.clients.interfaces.client.test.TestClient");
                    ClientInterfaces instance = (ClientInterfaces) c.newInstance();
                    log.debug("instance: " + instance);
                    vimBroker.addClient(instance, type);
                    break;
                case "openstack":
                    c = classLoader.loadClass("org.project.openbaton.clients.interfaces.client.openstack.OpenstackClient");
                    instance = (ClientInterfaces) c.newInstance();
                    log.debug("instance: " + instance);
                    vimBroker.addClient(instance, type);
                    break;
                case "amazon":
                    break;
                default:
                    throw new PluginInstallException("No type found");
            }
        } catch (MalformedURLException e) {
            throw new PluginInstallException(e);
        } catch (ClassNotFoundException e) {
            throw new PluginInstallException(e);
        } catch (InstantiationException e) {
            throw new PluginInstallException(e);
        } catch (IllegalAccessException e) {
            throw new PluginInstallException(e);
        }
    }

    public void installMonitoringPlugin(String path) throws PluginInstallException {
        File jar = new File(path);
        if (!jar.exists())
            throw new PluginInstallException(path + " does not exist");

        ClassLoader parent = ClassUtils.getDefaultClassLoader();
        path = jar.getAbsolutePath();
        try {
            log.trace("path is: " + path);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file://" + path)}, parent);

            URL url = null;
            String type = null;
            if (url == null){
                url = classLoader.getResource("org/project/openbaton/monitoring/agent/SmartDummyMonitoringAgent.class");
                type = "dummy";
            }
            if (url == null)
                throw new PluginInstallException("No ClientInterfaces known were found");

            log.trace("URL: " + url.toString());
            log.trace("type is: " + type);
            switch (type){
                case "dummy":
                    Class c = classLoader.loadClass("org.project.openbaton.monitoring.agent.SmartDummyMonitoringAgent");
                    ResourcePerformanceManagement agent = (ResourcePerformanceManagement) c.newInstance();
                    log.debug("instance: " + agent);
                    monitoringBroker.addAgent(agent, type);
                    break;
                default:
                    throw new PluginInstallException("No type found");
            }
        } catch (MalformedURLException e) {
            throw new PluginInstallException(e);
        } catch (ClassNotFoundException e) {
            throw new PluginInstallException(e);
        } catch (InstantiationException e) {
            throw new PluginInstallException(e);
        } catch (IllegalAccessException e) {
            throw new PluginInstallException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {

        List<Configuration> configurations = configurationRepository.findAll();

        String installFolderPath = null;
        Configuration system = null;
        for (Configuration c : configurations) {
            if (c.getName().equals("system")) {
                system = c;
                break;
            }
        }

        for (ConfigurationParameter cp : system.getConfigurationParameters()) {
            if (cp.getConfKey().equals("vim-plugin-installation-dir")) {
                installFolderPath = cp.getValue();
                break;
            }
        }

        File folder = new File(installFolderPath);

        File[] files = folder.listFiles();

        if (files.length > 0) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    this.installVimDriverPlugin(path);
                }
            }
        }

        for (ConfigurationParameter cp : system.getConfigurationParameters()) {
            if (cp.getConfKey().equals("monitoring-plugin-installation-dir")) {
                installFolderPath = cp.getValue();
                break;
            }
        }

        folder = new File(installFolderPath);

        files = folder.listFiles();

        if (files.length > 0) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    this.installMonitoringPlugin(path);
                }
            }
        }

    }
}
