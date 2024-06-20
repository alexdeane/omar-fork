/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Add.java,v 1.8 2005/05/17 22:47:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.admin.AdminShell;

public class Add extends AbstractAdminFunction {
	static Class[] execute3ParameterTypes =
		new Class[] {AdminFunctionContext.class,
					 String.class};

	private static ResourceBundle addFunctions =
		ResourceBundle.getBundle(AdminShell.ADMIN_SHELL_RESOURCE_BASE + ".AddFunctions");

    public void execute(AdminFunctionContext context,
						String args) throws Exception {
		if (args == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String[] tokens = args.split("\\s+", 2);

		if (tokens == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String addFunctionClassName;
		try {
			addFunctionClassName = addFunctions.getString(tokens[0].toLowerCase());
		} catch (java.util.MissingResourceException e) {
			context.printMessage(format(rb,
						    "unrecognizedObjectType",
						    new Object[] {tokens[0]}));
			return;
		}

		doAddFunction(context,
					  addFunctionClassName,
					  tokens.length > 1 ? tokens[1] : null);
	}
    
	void doAddFunction(AdminFunctionContext currContext,
					   String functionClassName,
					   String args) throws Exception {
		Class functionClass =
					functionClass = Class.forName(functionClassName);

		Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);

		AdminFunction function = (AdminFunction) constructor.newInstance((java.lang.Object[])null);

		Method execute3 = null;
		try {
			execute3 =
				functionClass.getDeclaredMethod("execute",
												execute3ParameterTypes);
		} catch (Exception e3) {
			//e3.printStackTrace();
			context.printMessage(format(rb, "cannotExecute",
						    new Object[] {
							    functionClassName
						    }));
		}
		function.execute(currContext, args);
	}

    public String getUsage() {
        return format(rb, "usage.add");
    }

	public void help(AdminFunctionContext context,
					 String args) throws Exception {
		if (args == null) {
			Enumeration functionNames = addFunctions.getKeys();

			while (functionNames.hasMoreElements()) {
				String functionName = (String)functionNames.nextElement();
				String functionClassName = addFunctions.getString(functionName);
				try {
					Class functionClass =
						functionClass = Class.forName(functionClassName);
					Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);
					AdminFunction function =
						(AdminFunction) constructor.newInstance((java.lang.Object[])null);
					context.printMessage(function.getUsage());
				} catch (Exception e) {
					e.printStackTrace();
					context.printMessage(
						format(rb, "noUsage",
						       new Object[] {
							       functionName}));
				}
			}
		} else {
			String[] tokens = args.split("\\s+", 2);

			try {
				String addFunctionClassName =
					addFunctions.getString(tokens[0].toLowerCase());

				try {
					Class functionClass =
						Class.forName(addFunctionClassName);

					Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);
					AdminFunction function =
						(AdminFunction) constructor.newInstance((java.lang.Object[])null);

					function.help(context,
								  tokens.length > 1 ? tokens[1] : null);
				} catch (Exception e) {
					e.printStackTrace();
					context.printMessage(
						format(rb, "noHelp",
						       new Object[] {
							       tokens[0]}));
				}
			} catch (java.util.MissingResourceException e) {
				context.printMessage(
					format(rb, "unrecognizedFunction",
					       new Object[] {tokens[0]}));
			}
		}
	}
}
