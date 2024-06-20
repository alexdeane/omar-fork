/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/KeystoreMover.java,v 1.2 2006/06/20 23:13:03 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.xml.registry.ClientRequestContext;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CommonResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;


public class KeystoreMover extends AbstractAdminFunction {
    private HashMap attachments = new HashMap();
    private String request;
    
    public void execute(AdminFunctionContext context, String args)
    throws Exception {
        org.freebxml.omar.common.security.KeystoreMover ksm = new org.freebxml.omar.common.security.KeystoreMover();
        
        String sourceKeystoreType = "PKCS12";
        String sourceKeystorePath = null;
        String sourceKeystorePassword = null;
        String sourceAlias = null;
        String sourceKeyPassword = null;
        
        String destinationKeystoreType = "JKS";
        String destinationKeystorePath = null;
        String destinationKeystorePassword = null;
        String destinationAlias = null;
        String destinationKeyPassword = null;
        
	if (args != null) {
	    String[] tokens = args.split("\\s+");

	    int tIndex = 0;

	    for (tIndex = 0;
		 ((tIndex < tokens.length) && tokens[tIndex].startsWith("-"));
		 tIndex++) {
		String option = tokens[tIndex];

		if (option.equalsIgnoreCase("-sourceKeystoreType")) {
		    sourceKeystoreType = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeystorePath")) {
		    sourceKeystorePath = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeystorePassword")) {
		    sourceKeystorePassword = tokens[++tIndex];
		} else if (option.startsWith("-sourceAlias")) {
		    sourceAlias = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeyPassword")) {
		    sourceKeyPassword = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystoreType")) {
		    destinationKeystoreType = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystorePath")) {
		    destinationKeystorePath = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystorePassword")) {
		    destinationKeystorePassword = tokens[++tIndex];
		} else if (option.startsWith("-destinationAlias")) {
		    destinationAlias = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeyPassword")) {
		    destinationKeyPassword = tokens[++tIndex];
		} else {
		    context.printMessage(format(rb,"invalidArgument",
						new Object[] { option }));
		    return;
		}
	    }
        
	    if (sourceKeystorePath == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePath"));
		context.printMessage(getUsage());
	    }
	    if (sourceKeystorePassword == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePassword"));
		context.printMessage(getUsage());
	    }
	    if (destinationKeystorePath == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePath"));
		context.printMessage(getUsage());
	    }
	    if (destinationKeystorePassword == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePassword"));
		context.printMessage(getUsage());
	    }
        
	    ksm.move(sourceKeystoreType, sourceKeystorePath, sourceKeystorePassword, sourceAlias, sourceKeyPassword, 
		     destinationKeystoreType, destinationKeystorePath, destinationKeystorePassword, destinationAlias, destinationKeyPassword);
        } else {
            context.printMessage(format(rb,"argumentRequired"));
            
            return;
        }
    }
    
    public String getUsage() {
        return format(rb, "usage.keystoreMover");
    }

    public void help(AdminFunctionContext context,
		     String args) throws Exception {
	context.printMessage(getUsage());
	context.printMessage();
	context.printMessage(format(rb, "help.keystoreMover"));
    }
}
