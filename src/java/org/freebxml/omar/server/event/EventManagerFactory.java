/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/event/EventManagerFactory.java,v 1.8 2006/08/24 20:42:15 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.util.ServerResourceBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * Factory to create an EventManager.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class EventManagerFactory {
    private static EventManagerFactory instance; //singleton instance
    public static final String EVENT_MANAGER_CLASS_PROPERTY = "omar.server.event.EventManagerFactory.eventManagerClass";
    private EventManager eventManager = null;
    private static final Log log = LogFactory.getLog(EventManagerFactory.class);

    /** Creates a new instance of EventManager */
    protected EventManagerFactory() {
    }

    public synchronized static EventManagerFactory getInstance() {
        if (instance == null) {
            instance = new EventManagerFactory();
        }

        return instance;
    }

    public EventManager getEventManager() {
        if (eventManager == null) {
            synchronized (this) {
                if (eventManager == null) {
                    try {
                        String pluginClass = RegistryProperties.getInstance()
                                                               .getProperty(EVENT_MANAGER_CLASS_PROPERTY);

                        eventManager = (EventManager) createPluginInstance(pluginClass);
                    } catch (Exception e) {
                        e.printStackTrace();

                        String errmsg = ServerResourceBundle.getInstance().getString("message.EventManagerCannotInstantiateEventManagerPlugin",
                                                                                      new Object[]{EVENT_MANAGER_CLASS_PROPERTY});
                        log.error(errmsg, e);
                    }
                }
            }
        }

        return eventManager;
    }

    /**
    * Creates the instance of the pluginClass
    */
    private Object createPluginInstance(String pluginClass)
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
            //log.warn(ServerResourceBundle.getInstance().getString("message.NoAccessibleConstructorInvokingGetInstanceInstead"));

            Method factory = theClass.getDeclaredMethod("getInstance", (java.lang.Class[])null);
            plugin = factory.invoke(null, new Object[0]);
        }

        return plugin;
    }
}
