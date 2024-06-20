/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AbstractAdminFunction.java,v 1.8 2005/04/26 22:55:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.io.PrintStream;

import java.text.Collator;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.admin.AdminResourceBundle;

public abstract class AbstractAdminFunction implements AdminFunction {
    public static final String ADMIN_FUNCTION_RESOURCES_PREFIX =
        "AdminFunctionResources.";

    protected static final BindingUtility bu = BindingUtility.getInstance();

    protected static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.IDENTICAL);
    }

    protected AdminResourceBundle rb = AdminResourceBundle.getInstance();

    protected AdminFunctionContext context;
    protected RegistryPackage      currentRP;
    protected boolean              debug;
    protected PrintStream          outStream;
    protected Collection           registryObjects;
    protected boolean              verbose;

    public void execute(AdminFunctionContext context,
			String args) throws Exception {
	throw new AdminException(format(rb, "unimplemented"));
    }

    public void execute(AdminFunctionContext context,
			String[] args) throws Exception {
	throw new AdminException(format(rb, "unimplemented"));
    }

    public AdminFunctionContext getContext() {
	return context;
    }

    public String getUsage() {
	return null;
    }

    public void help(AdminFunctionContext context,
		     String args) throws Exception {
	context.printMessage(getUsage());
    }

    public void setContext(AdminFunctionContext context) {
	this.context = context;
    }

    public void setCurrentRegistryPackage(RegistryPackage currentRP) {
	this.currentRP = currentRP;
    }

    public void setDebug(boolean debug) {
	this.debug = debug;
    }

    public void setOutStream(PrintStream outStream) {
	this.outStream = outStream;
    }

    public void setProperties(Properties properties) {
	if (properties.containsKey("debug")) {
	    setDebug(Boolean.
		     valueOf(properties.getProperty("debug")).booleanValue());
	}

	if (properties.containsKey("verbose")) {
	    setVerbose(Boolean.
		       valueOf(properties.getProperty("verbose")).booleanValue());
	}
    }

    public void setRegistryObjects(Collection registryObjects) {
	this.registryObjects = registryObjects;
    }

    public void setVerbose(boolean verbose) {
	this.verbose = verbose;
    }

    public void verifySettings() throws AdminException {
    }

    /**
     * Gets 'resourceName' key from the specified resource bundle and
     * formats the return using given 'formatArgs'.  First attempts
     * retrieval using the ADMIN_FUNCTION_RESOURCES_PREFIX.  If that key is
     * not found, uses ADMIN_SHELL_RESOURCES_PREFIX.  Falling back in this
     * fashion is not the result of an error.
     *
     * @param rb AdminResourceBundle in which key should be found
     * @param resourceName String key for message retrieval
     * @param formatArgs Array of arguments for message
     * @return Formatted String
     */
    public String format(AdminResourceBundle rb,
			 String resourceName,
			 Object[] formatArgs) {
	String ret;

	try {
	    ret = rb.getString(ADMIN_FUNCTION_RESOURCES_PREFIX + resourceName,
				formatArgs);
	} catch (java.util.MissingResourceException e) {
	    ret = rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       resourceName,
			       formatArgs);
	}
	return ret;
    }

    /**
     * Gets 'resourceName' key from the specified resource bundle.  First
     * attempts retrieval using the ADMIN_FUNCTION_RESOURCES_PREFIX.  If
     * that key is not found, uses ADMIN_SHELL_RESOURCES_PREFIX.  Falling
     * back in this fashion is not the result of an error.
     *
     * @param rb AdminResourceBundle in which key should be found
     * @param resourceName String key for message retrieval
     * @return Formatted String
     */
    public String format(AdminResourceBundle rb,
			 String resourceName) {
	String ret;

	try {
	    ret = rb.getString(ADMIN_FUNCTION_RESOURCES_PREFIX + resourceName);
	} catch (java.util.MissingResourceException e) {
	    ret = rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       resourceName);
	}
	return ret;
    }
}
