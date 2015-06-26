package org.project.neutrino.nfvo.sdk.api.util;

import java.lang.String;
import java.util.Collections;
import java.util.ArrayList;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;

/**
* OpenBaton SDK Property Reader. Provides URL information from the properties file
*/
public class PropertyReader {

    private Properties mainProperties, sdkProperties;

    /**
     * Creates a property reader that deserializes the property file from a jar
     *
     * @param propertiesPath
     * 				the jar (class)path to the properties file
     */
    public PropertyReader(final String mainPropertiesPath, final String sdkPropertiesPath) {
        mainProperties = readProperties(mainPropertiesPath);
        sdkProperties = readProperties(sdkPropertiesPath);
    }

    private Properties readProperties(final String propertiesPath) {
        Properties properties = null;
        InputStream inputStream = null;
        try {
            // load the jar's properties files
            inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(propertiesPath);
            // if there is an inputstream, execute the following
            properties = new Properties();
            properties.load(inputStream);

            // Print the sorted properties when called
//            ArrayList<String> propertyList = new ArrayList(properties.stringPropertyNames());
//            Collections.sort(propertyList);
//            for (String property : propertyList) {
//                System.out.print(propertyList);
//                System.out.println(properties.getProperty(property));
//            }

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
        return properties;
    }

    /**
     * Gets the api url
     *
     * @return the api url property
     */
    public String getApiUrl() {
        return sdkProperties.getProperty("apiUrl");
    }

    /**
     * Gets the configuration Url path
     *
     * @return the configuration Url path property
     */
    public String getRestConfigurationUrl() {
        return getApiUrl() + sdkProperties.getProperty("restConfigurationPath");
    }

    /**
     * Gets the image Url path
     *
     * @return the image Url path property
     */
    public String getRestImageUrl() {
        return getApiUrl() + sdkProperties.getProperty("restImagePath");
    }

    /**
     * Gets the networkservicedescriptor Url path
     *
     * @return the networkservicedescriptor Url path property
     */
    public String getRestNetworkServiceDescriptorUrl() {
        return getApiUrl() + sdkProperties.getProperty("restNetworkServiceDescriptorPath");
    }

    /**
     * Gets the networkservicerecord Url path
     *
     * @return the networkservicerecord Url path property
     */
    public String getRestNetworkServiceRecordUrl() {
        return getApiUrl() + sdkProperties.getProperty("restNetworkServiceRecordPath");
    }

    /**
     * Gets the viminstance Url path
     *
     * @return the viminstance Url path property
     */
    public String getRestVimInstanceUrl() {
        return getApiUrl() + sdkProperties.getProperty("restVimInstancePath");
    }

    /**
     * Gets the virtuallink Url path
     *
     * @return the virtuallink Url path property
     */
    public String getRestVirtualLinkUrl() {
        return getApiUrl() + sdkProperties.getProperty("restVirtualLinkPath");
    }

    /**
     * Gets the vnffg Url path
     *
     * @return the vnffg Url path property
     */
    public String getRestVNFFGUrl() {
        return getApiUrl() + sdkProperties.getProperty("restVNFFGPath");
    }

}