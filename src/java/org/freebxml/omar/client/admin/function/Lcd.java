/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Lcd.java,v 1.4 2005/05/17 22:47:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.filechooser.FileSystemView;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.admin.AdminShellUtility;

public class Lcd extends AbstractAdminFunction {
    private static final Log log = LogFactory.getLog(Lcd.class.getName());

    public void execute(AdminFunctionContext context,
			String args) throws Exception {
	File newLocalDir;

	if (args == null) {
	    newLocalDir = FileSystemView.getFileSystemView().getDefaultDirectory();
	} else {
			
	    String useArgs = AdminShellUtility.getInstance().normalizeArgs(args);

	    newLocalDir = new File(useArgs);
	    newLocalDir = newLocalDir.getCanonicalFile();
	}

	if (!newLocalDir.isDirectory()) {
	    throw new AdminException(format(rb,
					    "notDirectory",
					    new Object [] {
						newLocalDir
					    }));
	}

	context.setLocalDir(newLocalDir);

	if (context.getDebug()) {
	    context.printMessage(":" + newLocalDir + ":");
	}
    }

    public String getUsage() {
	return format(rb, "usage.lcd");
    }

    public void help(AdminFunctionContext context, String args) throws Exception {
	context.printMessage(
		format(rb,"help.lcd",
		       new Object[] {
			       FileSystemView.getFileSystemView().getDefaultDirectory()
		       }));
    }
}
