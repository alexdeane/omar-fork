/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/common/AbstractFactory.java,v 1.5 2006/02/08 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.util.ServerResourceBundle;

/**
 * Creates an instance of a type specified by a registry property if
 * the instance does not yet exist.  This is a common operation,
 * although the property name, the variable that refers to the object
 * instance, and the type of the object instance vary each time.
 *
 * @author  Tony Graham
 */
public abstract class AbstractFactory {
    private static final Log log = LogFactory.getLog(AbstractFactory.class.getName());

    /**
     * Get an instance of the class specified by the value of the
     * registry property specified by <code>propertyName</code> only
     * if the current value of the instance is <code>null</code>.
     *
     * @param propertyName name of registry property specifying class name
     * @param object variable to hold the instance
     * @return an instance of the specified class
     */
    protected synchronized Object getInstance(String propertyName,
				 Object object) {
        if (object == null) {
            try {
                String pluginClass = RegistryProperties.getInstance()
                                                       .getProperty(propertyName);

                object = createPluginInstance(pluginClass);
            } catch (Exception e) {
                String errmsg =ServerResourceBundle.getInstance().getString("message.AbstractFactoryCannotInstantiatePlugin",
                                                                             new Object[]{propertyName});
                log.error(errmsg, e);
            }
        }

        return object;
    }

    /**
     * Creates the instance of the pluginClass
     *
     * @param pluginClass class name of instance to create
     * @return an instance of the specified class
     * @exception Exception if an error occurs
     */
    protected Object createPluginInstance(String pluginClass)
        throws Exception {
        Object plugin = null;

        if (log.isDebugEnabled()) {
            log.debug("pluginClass = " + pluginClass);
        }

        Class theClass = Class.forName(pluginClass);

        //try to invoke constructor using Reflection, 
        //if this fails then try invoking getInstance()
        try {
            Constructor constructor = theClass.getConstructor((java.lang.Class[])null);
            plugin = constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            log.warn(ServerResourceBundle.getInstance().getString("message.NoAccessibleConstructorInvokingGetInstanceInstead"));

            Method factory = theClass.getDeclaredMethod("getInstance", (java.lang.Class[])null);
            plugin = factory.invoke(null, new Object[0]);
        }

        return plugin;
    }
}
