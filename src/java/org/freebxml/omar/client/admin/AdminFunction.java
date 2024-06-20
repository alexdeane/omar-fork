/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminFunction.java,v 1.4 2004/10/01 16:30:50 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.io.PrintStream;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;


public interface AdminFunction {
    public void execute(AdminFunctionContext context, String[] args)
        throws Exception;

    public void execute(AdminFunctionContext context, String args)
        throws Exception;

    public AdminFunctionContext getContext();

    public String getUsage();

    public void help(AdminFunctionContext context, String args)
        throws Exception;

    public void setContext(AdminFunctionContext context);

    public void setCurrentRegistryPackage(RegistryPackage presentRP);

    public void setDebug(boolean debug);

    public void setOutStream(PrintStream outStream);

    public void setProperties(Properties properties);

    public void setRegistryObjects(Collection registryObjects);

    public void setVerbose(boolean verbose);

    public void verifySettings() throws AdminException;
}
