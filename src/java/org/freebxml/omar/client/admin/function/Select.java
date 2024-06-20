/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Select.java,v 1.8 2005/05/17 22:47:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;

public class Select extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context,
			String args) throws Exception {
	if (args != null) {
	    Collection coll = context.getService().doSQLQuery("select " + args);
	    context.setRegistryObjects(coll);
	}

	Collection registryObjects = context.getRegistryObjects();

	if (registryObjects != null) {
	    Iterator iter = registryObjects.iterator();

	    while (iter.hasNext()) {
		RegistryObject ro = (RegistryObject) iter.next();
		context.printMessage(ro.getKey().getId() +
				     "  " +
				     ro.getName());
	    }

	    context.printMessage(format(rb,"objectsFound",
					new Object[] {
						new Integer(
							registryObjects.size())
					}));
	} else {
	    context.printMessage(format(rb,"noSelection"));
	}
    }

    public String getUsage() {
	return format(rb, "usage.select");
    }
}
