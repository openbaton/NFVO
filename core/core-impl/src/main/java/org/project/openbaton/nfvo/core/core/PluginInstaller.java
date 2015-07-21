package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.nfvo.Configuration;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.exceptions.PluginInstallException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Created by lto on 21/07/15.
 */
@Component
public class PluginInstaller implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VimBroker vimBroker;

    @Autowired
    @Qualifier("configurationRepository")
    private GenericRepository<Configuration> configurationRepository;


    public void installPlugin(String path) throws PluginInstallException {
        File jar = new File(path);
        if (!jar.exists())
            throw new PluginInstallException(path + " does not exist");

        ClassLoader parent = ClassUtils.getDefaultClassLoader();
        path = jar.getAbsolutePath();
        try {
            log.trace("path is: " + path);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file://" + path)}, parent);

            URL url;
            url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/test/TestClient.class");
            String type = "test";
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
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            JarFile file = connection.getJarFile();
            switch (type){
                case "test":
                    Class c = classLoader.loadClass("org.project.openbaton.clients.interfaces.client.test.TestClient");
                    ClientInterfaces instance = (ClientInterfaces) c.newInstance();
                    log.debug("instance: " + instance);
                    vimBroker.addClient(instance, type);
                    break;
                case "openstack":
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
        } catch (IOException e) {
            throw new PluginInstallException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {

        List<Configuration> configurations = configurationRepository.findAll();

        String installFolderPath = null;
        for (Configuration c : configurations){
            if (c.getName().equals("system"))
                for (ConfigurationParameter cp : c.getConfigurationParameters()){
                    if (cp.getConfKey().equals("plugin-installation-dir")){
                        installFolderPath = cp.getValue();
                        break;
                    }
                }
        }

        File folder = new File(installFolderPath);

        File[] files = folder.listFiles();

        if (files.length > 0){
            for (File f : files){
                String path = f.getAbsolutePath();
                if (path.endsWith(".jar")){
                    this.installPlugin(path);
                }
            }
        }

    }
}
