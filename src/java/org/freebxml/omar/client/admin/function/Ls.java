/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Ls.java,v 1.12 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.common.Utility;

public class Ls extends AbstractAdminFunction {
	public void execute(AdminFunctionContext context,
						String args) throws Exception {
		String queryStr;

		if (context.getCurrentRP() == null) {

			queryStr =
				"SELECT ro.* from RegistryObject ro WHERE " +
				"ro.id NOT IN (SELECT targetObject FROM Association) OR " +
				"ro.id IN (SELECT DISTINCT targetObject FROM Association " +
				"WHERE associationType != '" +
				bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
				"')";
		} else {
			queryStr =
				"SELECT DISTINCT ro.* " +
				"FROM RegistryObject ro, RegistryPackage p, " +
				"Association ass WHERE ((p.id = '" +
				context.getCurrentRP().getKey().getId() +
				"') AND (ass.associationType='"
				+ bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
				"' AND ass.sourceObject = p.id AND ass.targetObject = ro.id)) ";
		}

		javax.xml.registry.Query query =
			context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
													  queryStr);
		// make JAXR request
		javax.xml.registry.BulkResponse resp =
			context.getService().getDQM().executeQuery(query);

		Collection registryObjects = resp.getCollection();
		Iterator iter = registryObjects.iterator();

		// Number of objects listed by command.
		int objectCount = 0;
		if (args != null) {
			// If there are any args, match every object in RegistryPackage against
			// each arg (with the arg evaluated as a pattern)
			String[] tokens = args.split("\\s+");
			while (iter.hasNext()) {
				RegistryObject ro = (RegistryObject) iter.next();
				String roName = ro.getName().getValue();

				for(int index = 0; index < tokens.length; index++) {
					String pattern = tokens[index];

					// Match against pattern as either UR or globbing pattern.
					// Currently can't match against null names.
					if ((Utility.getInstance().isValidURN(pattern) &&
						 ro.getKey().getId().equals(pattern)) ||
						(!Utility.getInstance().isValidURN(pattern) &&
						 roName != null &&
						 SelectorUtils.match(pattern, roName))) {
						context.printMessage(ro.getKey().getId() +
											 "  " +
											 ro.getName());

						objectCount++;
						continue;
					}
				}
			}
		} else {
			// If no args, list every RegistryObject in RegistryPackage.
			while (iter.hasNext()) {
				RegistryObject ro = (RegistryObject) iter.next();
				context.printMessage(ro.getKey().getId() +
									 "  " +
									 ro.getName());
				objectCount++;
			}
		}

		context.printMessage(format(rb,"objectsFound",
					    new Object[] {
						    new Integer(objectCount)
					    }));
	}

	public String getUsage() {
		return format(rb, "usage.ls");
	}
}
