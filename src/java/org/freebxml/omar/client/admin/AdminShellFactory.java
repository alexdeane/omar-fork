/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminShellFactory.java,v 1.8 2006/08/24 20:41:54 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.admin.AdminResourceBundle;

public class AdminShellFactory {
    private static AdminShellFactory instance; //singleton instance
    public static final String ADMIN_SHELL_CLASS_PROPERTY = "omar.client.admin.AdminShellFactory.adminShellClass";

    protected AdminResourceBundle rb = AdminResourceBundle.getInstance();
    private AdminShell adminShell = null;
    private static final Log log = LogFactory.getLog(AdminShellFactory.class);

    /** Creates a new instance of AdminShellFactory */
    protected AdminShellFactory() {
    }

    public synchronized static AdminShellFactory getInstance() {
        if (instance == null) {
            instance = new AdminShellFactory();
        }

        return instance;
    }

    public AdminShell getAdminShell() {
        synchronized (this) {
            if (adminShell == null) {
                try {
                    String pluginClass = ProviderProperties.getInstance()
                                                           .getProperty(ADMIN_SHELL_CLASS_PROPERTY);

                    adminShell = (AdminShell) createPluginInstance(pluginClass);
                } catch (Exception e) {
                    Object[] cannotCreateArgs = { ADMIN_SHELL_CLASS_PROPERTY };
                    String errmsg = rb.getString(
			    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			    "cannotCreate",
			    cannotCreateArgs);
                    log.error(errmsg, e);
                }
            }
        }

        return adminShell;
    }

    /**
    * Creates the instance of the pluginClass
    */
    private Object createPluginInstance(String pluginClass)
        throws Exception {
        Object plugin = null;

        log.debug("adminShellClass = " + pluginClass);

        Class theClass = Class.forName(pluginClass);

        //try to invoke constructor using Reflection,
        //if this fails then try invoking getInstance()
        try {
            Constructor constructor = theClass.getConstructor((java.lang.Class[])null);
            plugin = constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            log.warn(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                  "noAccessibleConstructor"));

            Method factory = theClass.getDeclaredMethod("getInstance", (java.lang.Class[])null);
            plugin = factory.invoke(null, new Object[0]);
        }

        return plugin;
    }
}
