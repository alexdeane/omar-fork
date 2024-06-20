/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Echo.java,v 1.3 2005/05/17 22:47:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;

public class Echo extends AbstractAdminFunction {
	public void execute(AdminFunctionContext context,
						String args) throws Exception {
		context.printMessage(args);
	}

	public String getUsage() {
		return format(rb, "usage.echo");
	}
}
