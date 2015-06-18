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
     * Gets the configuration Url path
     *
     * @return the configuration Url path property
     */
    public String getRestConfigurationUrl() {
        return getApiUrl() + applicationProperties.getProperty("restConfigurationPath");
    }

    /**
     * Gets the image Url path
     *
     * @return the image Url path property
     */
    public String getRestImageUrl() {
        return getApiUrl() + applicationProperties.getProperty("restImagePath");
    }

    /**
     * Gets the networkservicedescriptor Url path
     *
     * @return the networkservicedescriptor Url path property
     */
    public String getRestNetworkServiceDescriptorUrl() {
        return getApiUrl() + applicationProperties.getProperty("restNetworkServiceDescriptorPath");
    }

    /**
     * Gets the networkservicerecord Url path
     *
     * @return the networkservicerecord Url path property
     */
    public String getRestNetworkServiceRecordUrl() {
        return getApiUrl() + applicationProperties.getProperty("restNetworkServiceRecordPath");
    }

    /**
     * Gets the viminstance Url path
     *
     * @return the viminstance Url path property
     */
    public String getRestVimInstanceUrl() {
        return getApiUrl() + applicationProperties.getProperty("restVimInstancePath");
    }

    /**
     * Gets the virtuallink Url path
     *
     * @return the virtuallink Url path property
     */
    public String getRestVirtualLinkUrl() {
        return getApiUrl() + applicationProperties.getProperty("restVirtualLinkPath");
    }

}