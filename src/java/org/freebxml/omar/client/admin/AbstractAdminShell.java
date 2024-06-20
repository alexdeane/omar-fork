/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AbstractAdminShell.java,v 1.8 2005/04/26 22:55:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.admin.AdminResourceBundle;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import java.text.Collator;

import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public abstract class AbstractAdminShell implements AdminShell {
    private static Log log = LogFactory.getLog(AbstractAdminShell.class.getName());

    static final BindingUtility bu = BindingUtility.getInstance();
    static final Collator collator;
    static AdminFunctionContext context;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        context = AdminFunctionContext.getInstance();

        String editor = System.getProperty("EDITOR");

        if ((editor == null) || editor.equals("")) {
            String platform = System.getProperty("os.name");

            if (platform.startsWith("Windows")) {
                editor = "notepad.exe";
            } else {
                // assume some flavor of Unix with vi editor
                editor = "/bin/vi";
            }
        }

        context.setEditor(editor);
    }

    AdminResourceBundle rb = AdminResourceBundle.getInstance();
    ResourceBundle functions = ResourceBundle.getBundle(ADMIN_SHELL_FUNCTIONS);

    RegistryPackage currentRP;
    boolean debug;
    File localDir;
    Properties properties;
    RegistryObject[] registryObjects;
    JAXRService service;
    boolean verbose;

    public void run(InputStream inStream, PrintStream outStream)
        throws AdminException {
        throw new AdminException(rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
					      "unimplemented"));
    }

    public void setLocalDir(File localDir) {
	this.localDir = localDir;
	context.setLocalDir(localDir);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        context.setDebug(debug);
    }

    public void setProperties(Properties properties) {
        if (properties.containsKey("debug")) {
            setDebug(Boolean.valueOf(properties.getProperty("debug"))
                            .booleanValue());
        }

        if (properties.containsKey("verbose")) {
            setVerbose(Boolean.valueOf(properties.getProperty("verbose"))
                              .booleanValue());
        }

        context.setProperties(properties);
    }

    public void setRoot(RegistryPackage rootRP) {
        this.currentRP = rootRP;
        context.setCurrentRP(rootRP);
    }

    public void setService(JAXRService service) {
        if (service == null) {
            throw new NullPointerException();
        }

        this.service = service;
        context.setService(service);
    }

    public void setSQLSelect(String sqlQuery) throws AdminException {
        if (sqlQuery == null) {
            throw new NullPointerException();
        }

        //assert context.getService() != null;
        try {
            Collection coll = context.getService().doSQLQuery(sqlQuery);
            context.setRegistryObjects(coll);
        } catch (Exception e) {
            throw new AdminException(e);
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        context.setVerbose(verbose);
    }

    public void verifySettings() throws AdminException {
    }
}
