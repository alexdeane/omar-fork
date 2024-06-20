/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/SimpleAdminShell.java,v 1.10 2005/05/18 21:19:54 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.admin.AdminResourceBundle;

public class SimpleAdminShell extends AbstractAdminShell {

    HashMap classes = new HashMap();
    PrintStream outStream;

    Class[] execute1ParameterTypes =
	new Class[] {AdminFunctionContext.class,
		     String[].class};
    Class[] execute2ParameterTypes =
	new Class[] {String[].class};
    Class[] execute3ParameterTypes =
	new Class[] {AdminFunctionContext.class,
		     String.class};

    /**
     * Executes commands read from inStream.
     *
     * @param inStream an <code>InputStream</code> of commands
     * @param outStream a <code>PrintStream</code> to which to write output
     * @exception AdminException if an error occurs
     */
    public void run(InputStream inStream, PrintStream outStream) throws AdminException {
	try {
	    if (debug) {
		System.err.println(this.getClass().getName());
	    }

	    this.outStream = outStream;
	    context.setOutStream(outStream);

	    BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
	    run(input, false);
	} catch (Exception e) {
	    throw new AdminException(e);
	}
    }

    /**
     * Executes command or commands in command string.
     *
     * @param command a <code>String</code> containing commands
     * @param outStream a <code>PrintStream</code> to which to write output
     * @exception AdminException if an error occurs
     */
    public void run(String command, PrintStream outStream) throws AdminException {
	try {
	    if (debug) {
		System.err.println(this.getClass());
	    }

	    this.outStream = outStream;
	    context.setOutStream(outStream);

	    // Replace semicolons that are not escaped by a backslash with "\n"
	    command = command.replaceAll("(?!\\\\);", "\n");
	    // Replace semicolons that are escaped by a backslash with ";"
	    command = command.replaceAll("\\\\;", ";");
	    // Add a 'quit' command so the admin shell is sure to exit
	    command = command + "\nquit\n";

	    if (debug) {
		System.err.println(command);
	    }

	    BufferedReader input = new BufferedReader(new StringReader(command));
	    run(input, true);
	} catch (Exception e) {
	    throw new AdminException(e);
	}
    }

    /**
     * Common code for executing commands.
     *
     * @param input a <code>BufferedReader</code> from which to read commands
     * @param echo whether or not to echo commands to outStream
     * @exception Exception if an error occurs
     */
    private void run(BufferedReader input, boolean echo) throws Exception {
	String line;
	while(true) {

	    outStream.print(rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
                                         "prompt"));
	    line = input.readLine();

	    if (line != null) {
		line = line.trim();
	    } else {
		break;
	    }

	    if (echo) {
		outStream.println(line);
	    }

	    //outStream.println(line);

	    if (line.equals("")) {
		continue;
	    }

	    String[] tokens = line.split("\\s+", 2);

	    if (collator.compare(tokens[0], "help") == 0) {
		doHelp(context,
		       tokens.length > 1 ? tokens[1] : null);
		continue;
	    } else if (collator.compare(tokens[0], "quit") == 0) {
		break;
	    }

	    String functionClassName;
	    try {
		functionClassName = functions.getString(tokens[0].toLowerCase());
	    } catch (java.util.MissingResourceException e) {
		Object[] formatArgs = {tokens[0]};
		outStream.println(rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
					       "unrecognizedFunction",
					       formatArgs));
		continue;
	    }

	    try {
		doFunction(context,
			   functionClassName,
			   tokens.length > 1 ? tokens[1] : null);
		// This is executed only when there was no exception.from doFunction()
		context.setLastException(null);
	    } catch (Exception e) {
		// Save a copy of the stack trace as a String
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		context.setLastException(sw.toString());

		outStream.println(rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
                                               "functionError"));
		if (!verbose) {
		    if (e.getMessage() != null) {
			// Print only the first line of the exception message
			outStream.println(e.getMessage().split("\n", 2)[0]);
		    }
		} else {
		    e.printStackTrace(outStream);
		}
	    }
	}
    }

    Class getFunctionClass(HashMap classes,
			   String functionClassName) throws Exception {
	Class functionClass = null;
	synchronized (this) {
	    if (classes.containsKey(functionClassName)) {
		functionClass = (Class) classes.get(functionClassName);
	    } else {
		try {
		    functionClass = Class.forName(functionClassName);
		} catch (Exception e) {
		    Object[] formatArgs = {functionClassName};
		    String errmsg = rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
						 "cannotCreate",
						 formatArgs);
		    outStream.println(errmsg);
		}
		classes.put(functionClassName, functionClass);
	    }
	}

	/*
	  Method[] methods = functionClass.getDeclaredMethods();

	  for (int i = 0; i < methods.length; i++) {
	  System.err.println(methods[i]);
	  }
	*/
	return functionClass;
    }

    void doFunction(AdminFunctionContext currContext,
		    String functionClassName,
		    String args) throws Exception {
	Class functionClass = getFunctionClass(classes,
					       functionClassName);

	Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);

	AdminFunction function = (AdminFunction) constructor.newInstance((java.lang.Object[])null);

	/*
	 * The AdminFunction interface has two execute()
	 * functions with different signatures.  An
	 * AdminFunction implementation doesn't have to
	 * implement all of them, so try each in turn to see
	 * if it is implemented by the current function.
	 */
	Method execute1 = null;
	try {
	    execute1 =
		functionClass.getDeclaredMethod("execute",
						execute1ParameterTypes);
	} catch (Exception e1) {
	    Method execute3 = null;
	    try {
		execute3 =
		    functionClass.getDeclaredMethod("execute",
						    execute3ParameterTypes);
	    } catch (Exception e3) {
		//e3.printStackTrace();
		Object[] formatArgs = {functionClassName};
		String errmsg = rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
					     "cannotExecute",
					     formatArgs);
		outStream.println(errmsg);
	    }
	    function.execute(currContext, args);
	    return;
	}
	//System.err.println(context);
	//System.err.println(args);
	function.execute(currContext,
			 args != null ? args.split("\\s+") : null);
	// This 'return' is not necessary but does match use with
	// execute3.
	return;
    }

    void doHelp(AdminFunctionContext context,
		String args) {
	if (args == null) {
	    context.printMessage("help");
	    context.printMessage("quit");

	    Enumeration functionNames = functions.getKeys();

	    while (functionNames.hasMoreElements()) {
		String functionName = (String)functionNames.nextElement();
		try {
		    Class functionClass =
			getFunctionClass(classes,
					 functions.getString(functionName));
		    Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);
		    AdminFunction function = (AdminFunction) constructor.newInstance((java.lang.Object[])null);
		    context.printMessage(function.getUsage());
		} catch (Exception e) {
		    e.printStackTrace();
		    context.printMessage(rb.getString(
						 ADMIN_SHELL_RESOURCES_PREFIX +
                                                 "noUsage",
						 new Object[] {functionName}));
		}
	    }
	} else {
	    String[] tokens = args.split("\\s+", 2);

	    try {
		String functionClassName = functions.getString(tokens[0].toLowerCase());

		try {
		    Class functionClass =
			getFunctionClass(classes,
					 functionClassName);

		    Constructor constructor = functionClass.getConstructor((java.lang.Class[])null);
		    AdminFunction function = (AdminFunction) constructor.newInstance((java.lang.Object[])null);
		    function.help(context,
				  tokens.length > 1 ? tokens[1] : null);
		} catch (Exception e) {
		    e.printStackTrace();
		    context.printMessage(rb.getString(
						 ADMIN_SHELL_RESOURCES_PREFIX +
						 "noHelp",
						 new Object[] {tokens[0]}));
		}
	    } catch (java.util.MissingResourceException e) {
		context.printMessage(rb.getString(
					     ADMIN_SHELL_RESOURCES_PREFIX +
					     "unrecognizedFunction",
					     new Object[] {tokens[0]}));
	    }
	}
    }
}
