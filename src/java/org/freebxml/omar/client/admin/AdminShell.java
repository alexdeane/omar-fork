/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminShell.java,v 1.6 2005/04/26 22:55:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;

public interface AdminShell {
    public static final String ADMIN_SHELL_RESOURCE_BASE =
	"org.freebxml.omar.client.admin";

    public static final String ADMIN_SHELL_FUNCTIONS =
	ADMIN_SHELL_RESOURCE_BASE + ".AdminShellFunctions";

    public static final String ADMIN_SHELL_RESOURCES_PREFIX =
            "AdminShellResources.";

    public void run(InputStream inStream, PrintStream outStream) throws AdminException;

    public void run(String command, PrintStream outStream) throws AdminException;

    public void setDebug(boolean debug);

    public void setLocalDir(File baseDir);

    public void setProperties(Properties properties);

    public void setRoot(RegistryPackage rootRP);

    public void setService(JAXRService service);

    public void setSQLSelect(String sqlQuery) throws AdminException;

    public void setVerbose(boolean verbose);

    public void verifySettings() throws AdminException;
}
