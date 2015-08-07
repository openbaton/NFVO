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
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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


    public void installVimDriverPlugin(String path, List<String> classes) throws PluginInstallException {

        try {
            ClassLoader classLoader = getClassLoader(path);

            for (String clazz: classes){
                log.debug("Loading class: " +clazz);
                Class c = null;
                try {
                    c = classLoader.loadClass(clazz);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                Field f = null;
				try {
					f = c.getField("interfaceVersion");
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
                if(((String) f.get (c)).equals(ClientInterfaces.interfaceVersion.toString())){
                log.debug("Correct interface Version");	
                ClientInterfaces instance = (ClientInterfaces) c.newInstance();
                log.debug("instance: " + instance);
                log.debug("of type: " + instance);
                vimBroker.addClient(instance, instance.getType());
                }else
               	throw new PluginInstallException("The interface Version are different");
            }
        } catch (MalformedURLException e) {
            throw new PluginInstallException(e);
        } catch (InstantiationException e) {
            throw new PluginInstallException(e);
        } catch (IllegalAccessException e) {
            throw new PluginInstallException(e);
        } catch (SecurityException e) {
        	throw new PluginInstallException(e);
		}
    }

    public ClassLoader getClassLoader(String path) throws PluginInstallException, MalformedURLException {
        File jar = new File(path);
        if (!jar.exists())
            throw new PluginInstallException(path + " does not exist");
        ClassLoader parent = ClassUtils.getDefaultClassLoader();
        path = jar.getAbsolutePath();
        log.trace("path is: " + path);
        return new URLClassLoader(new URL[]{new URL("file://" + path)}, parent);
    }

    public void installMonitoringPlugin(String path, List<String> classes) throws PluginInstallException {

        try {
            ClassLoader classLoader = getClassLoader(path);

            for (String clazz : classes) {
                log.debug("Loading class: " + clazz);
                Class c = classLoader.loadClass(clazz);
                ResourcePerformanceManagement agent = (ResourcePerformanceManagement) c.newInstance();
                log.debug("instance: " + agent);
                log.debug("of type: " + agent.getType());
                monitoringBroker.addAgent(agent, agent.getType());
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
        List<String> classes = new ArrayList<>();
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
            }
            if (cp.getConfKey().equals("vim-classes")){
                classes = Arrays.asList(cp.getValue().split(";"));
            }
        }

        File folder = new File(installFolderPath);

        File[] files = folder.listFiles();

        if (files.length > 0) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    this.installVimDriverPlugin(path, classes);
                }
            }
        }

        for (ConfigurationParameter cp : system.getConfigurationParameters()) {
            if (cp.getConfKey().equals("monitoring-plugin-installation-dir")) {
                installFolderPath = cp.getValue();
            }
            if (cp.getConfKey().equals("monitoring-classes")){
                classes = Arrays.asList(cp.getValue().split(";"));
            }
        }

        folder = new File(installFolderPath);

        files = folder.listFiles();

        if (files.length > 0) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    this.installMonitoringPlugin(path, classes);
                }
            }
        }

    }
}
