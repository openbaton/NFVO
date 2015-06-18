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

    private Properties sdkProperties;

    /**
     * Creates a property reader that deserializes the property file from a jar
     *
     * @param propertiesPath
     * 				the jar (class)path to the properties file
     */
    public PropertyReader(final String sdkPropertiesPath) {

        sdkProperties = new Properties();
        InputStream inputStream = null;

        try {
            // load the jars properties file
            inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(sdkPropertiesPath);
            sdkProperties.load(inputStream);

            // Print the sorted properties when called
//            ArrayList<String> list = new ArrayList(sdkProperties.stringPropertyNames());
//            Collections.sort(list);
//            for (String str : list) {
//                System.out.print(str);
//                System.out.println(sdkProperties.getProperty(str));
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

}