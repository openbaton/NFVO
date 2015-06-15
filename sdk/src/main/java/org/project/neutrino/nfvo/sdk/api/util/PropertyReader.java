package org.project.neutrino.nfvo.sdk.api.util;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;

/**
* OpenBaton SDK Property Reader. Provides URL information from the properties file
*/
public class PropertyReader {

    private Properties applicationProperties;

    /**
     * Creates a property reader that deserializes the property file from a jar
     *
     * @param propertiesPath
     * 				the jar (class)path to the properties file
     */
    public PropertyReader(final String propertiesPath) {

        applicationProperties = new Properties();
        InputStream inputStream = null;

        try {
            // load the jars properties file
            inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(propertiesPath);
            applicationProperties.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the api url
     *
     * @return the api url property
     */
    public String getApiUrl() {
        return applicationProperties.getProperty("apiUrl");
    }

    /**
     * Gets the configuration path
     *
     * @return the configuration path property
     */
    public String getRestConfigurationPath() {
        return getApiUrl() + applicationProperties.getProperty("restConfigurationPath");
    }

    /**
     * Gets the image path
     *
     * @return the image path property
     */
    public String getRestImagePath() {
        return getApiUrl() + applicationProperties.getProperty("restImagePath");
    }

    /**
     * Gets the networkservicedescriptor path
     *
     * @return the networkservicedescriptor path property
     */
    public String getRestNetworkServiceDescriptorPath() {
        return getApiUrl() + applicationProperties.getProperty("restNetworkServiceDescriptorPath");
    }

    /**
     * Gets the networkservicerecord path
     *
     * @return the networkservicerecord path property
     */
    public String getRestNetworkServiceRecordPath() {
        return getApiUrl() + applicationProperties.getProperty("restNetworkServiceRecordPath");
    }

    /**
     * Gets the viminstance path
     *
     * @return the viminstance path property
     */
    public String getRestVimInstancePath() {
        return getApiUrl() + applicationProperties.getProperty("restVimInstancePath");
    }

    /**
     * Gets the virtuallink path
     *
     * @return the virtuallink path property
     */
    public String getRestVirtualLinkPath() {
        return getApiUrl() + applicationProperties.getProperty("restVirtualLinkPath");
    }

}