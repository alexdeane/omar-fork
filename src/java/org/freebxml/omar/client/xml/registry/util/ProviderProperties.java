/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/ProviderProperties.java,v 1.22 2006/02/08 18:38:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.AbstractProperties;
import org.freebxml.omar.common.CommonProperties;



/**
 * Abstraction for Registry Configuration properties.
 *
 * Initial implementation just uses Java Property file,
 * future implementation might acquire configuration info
 * from XML. Thus, all java property methods are called
 * via this abstraction.
 *
 * Registry Property File Search Order
 * 1. If Property "jaxr-ebxml.properties", this value is used as the filename
 * to load the jaxr properties from. Skip to step 3 if this property is
 * set and the properties successfully loaded.
 * 2. If file Property{user.home}/.jaxr-ebxml.properties exists, the properties
 * are loaded from here.
 * 3. System default properties are read from the CLASSPATH. The first
 * file named org/freebxml/omar/client/xml/registry/util/jaxr-ebxml.properties
 * will be used to set default Registry properties. These properties are overriden
 * by the same named property set by either steps 1 or 2.
 *
 * When adding a new property, be sure to set a default value for the
 * property in DEFAULT_PROPERTY_RESOURCE.
 *
 * Property Priority
 * 1. Highest priority: any java system properties including command line
 * properties set with -D
 * 2. Medium priority: any properties set in {user.home}/.jaxr-ebxml.properties
 * 3. Lowest priority: any properties set in the Registry default property file
 */
public class ProviderProperties extends AbstractProperties {
    
    private static Log log = LogFactory.getLog("org.freebxml.omar.client.xml.registry.util.ProviderProperties");    
    private static final String DEFAULT_JAXR_HOME = "jaxr-ebxml";
    private static final String JAXR_HOME_KEY = "jaxr-ebxml.home";
    private static final String PROPERTY_FILE_NAME = "jaxr-ebxml.properties";
    private static final String DEFAULT_PROPERTY_RESOURCE = "org/freebxml/omar/client/xml/registry/util/jaxr-ebxml-defaults.properties";
    private static ProviderProperties instance = null;

    /**
     * Cache ProviderProperties.
     */
    private ProviderProperties() {
        super();
        initProperties();
    }
        
    /**
     * Initializes the properties performing the full loading sequence. 
     */
    protected void initProperties() {
        // Complete omar-common loading sequence at first
        Properties commonProps = CommonProperties.staticLoadProperties(getClass().getClassLoader(), new Properties());

        // Load Provider Properties
        props = loadProperties(commonProps);
        
        // initializes <omar.home>
        initOmarHomeDir(props);
        
        // Substitute variables
        substituteVariables(props);

        // initialize JAXR home
        File jaxrHomeDir = new File(getJaxrHome(props));
        initHomeDir(JAXR_HOME_KEY, jaxrHomeDir);
        props.setProperty(JAXR_HOME_KEY, getJaxrHome(props));

        logProperties("Provider properties", props);
    }

    /*
     * This method is used to load properties. It returns a Properties
     * object that should be cached in memory.  It is called by
     * the ProviderProperties() constructor and by reloadProperties()
     *
     * @param defaultProps a default list of properties
     *
     * @return a Properties object loaded from a predefined resource
     */
    protected Properties loadProperties(final Properties defaultProps) {
        log.trace(JAXRResourceBundle.getInstance().getString("message.LoadingJaxrebxmlProperties"));
        Properties properties = new Properties(defaultProps);
        
        // Start from default properties
        loadDefaultProperties(properties);
        
        // Props from resource at classpath root
        boolean resLoaded = loadResourceProperties(getClass().getClassLoader()
            , properties, PROPERTY_FILE_NAME);
        
        if (!resLoaded) {
            // Load properties from file system
            loadFileProperties(properties, new File(getPropertyFileName(properties)));
        }
        
        //TODO: should it load all or subset
        // we don't really want all the system props, should we weed them out?
        loadSystemProperties(properties);

        return properties;
    }
    
    /** 
      * Check property jaxr-ebxml.property for a property file name.
      * Default property file name is <registry.home>/jaxr-ebxml.properties.
      */
    private String getPropertyFileName(Properties properties) {
        String propertyFileName = getOmarHome(properties) + "/" + PROPERTY_FILE_NAME;
        propertyFileName = System.getProperty(PROPERTY_FILE_NAME, propertyFileName);
        return propertyFileName;
    }

    /**
     * Implement Singleton class, this method is only way to get this object.
     */
    public synchronized static ProviderProperties getInstance() {
        if (instance == null) {
            instance = new ProviderProperties();
        }
        return instance;
    }

    /**
     * Merge properties with existing properties.
     *
     * @param p New properties
     */
    public void mergeProperties(Properties p) {
        //todo: consider refactoring to AbstractProperties
        Set keys = p.keySet();
        Iterator i = keys.iterator();
        Object key;

        while (i.hasNext()) {
            key = i.next();
            String value = (String)p.get(key);
            if (key != null && value != null) { 
                this.props.put(key, value);
            }
        }
    }

    /**
     * Get properties
     *
     * @return Properties May be null
     */
    public Properties getProperties() {
        //todo: consider removing
        return this.props;
    }

    /**
     * Replace pre-defined variables in property values with the variable value from the
     * corresponding property.
     */
    private void substituteVariables(Properties properties) {
        // at coding time this would replace $user.home and $omar.home
        CommonProperties.staticSubstituteVariables(properties);
        //Iterate and replace allowed variables
        substituteVariables(properties, "$jaxr-ebxml.home", getJaxrHome(properties));
    }

    /**
     * Load default common properties, default properties from
     * DEFAULT_PROPERTY_RESOURCE and add them all to 'properties'.
     *
     * @param properties and existing property set to be updated.
     */
    public void loadDefaultProperties(Properties properties) {
        loadResourceProperties(getClass().getClassLoader(), properties
            , ProviderProperties.DEFAULT_PROPERTY_RESOURCE);
    }
    
    protected static String getJaxrHome(Properties properties) {
        String jaxrHome = properties.getProperty(JAXR_HOME_KEY);
        jaxrHome = substituteVariable(jaxrHome, "$user.home", getUserHome());
        jaxrHome = substituteVariable(jaxrHome, "$omar.home", getOmarHome(properties));
        if (jaxrHome == null) {
            throw new RuntimeException("Required property '" + JAXR_HOME_KEY + "' not defined.");
        }
        return jaxrHome;
    }
    

}
