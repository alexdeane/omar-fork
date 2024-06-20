/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Set.java,v 1.8 2005/06/04 02:16:28 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.io.PrintStream;

import java.text.Collator;
import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;

public class Set extends AbstractAdminFunction {
    private static final Collator collator;
    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

	public void execute(AdminFunctionContext context,
						String args) throws Exception {
		if (args == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String[] argsArray = args.split("\\s+", 2);

		if (collator.compare(argsArray[0], "debug") == 0) {
			if ((collator.compare(argsArray[1], "true") == 0) ||
				(collator.compare(argsArray[1], "on") == 0) ||
				(collator.compare(argsArray[1], "yes") == 0)) {
				context.setDebug(true);
				return;
			} else if ((collator.compare(argsArray[1], "false") == 0) ||
				(collator.compare(argsArray[1], "off") == 0) ||
				(collator.compare(argsArray[1], "no") == 0)) {
				context.setDebug(false);
				return;
			}
		} else if (collator.compare(argsArray[0], "editor") == 0) {
			context.setEditor(argsArray[1]);
			return;
		} else if (collator.compare(argsArray[0], "verbose") == 0) {
			if ((collator.compare(argsArray[1], "true") == 0) ||
				(collator.compare(argsArray[1], "on") == 0) ||
				(collator.compare(argsArray[1], "yes") == 0)) {
				context.setVerbose(true);
				return;
			} else if ((collator.compare(argsArray[1], "false") == 0) ||
				(collator.compare(argsArray[1], "off") == 0) ||
				(collator.compare(argsArray[1], "no") == 0)) {
				context.setVerbose(false);
				return;
			}
		}

		// If got to here, there was a problem.
		context.printMessage(format(rb,"invalidArgument",
					    new Object[] {args}));
	}

	public String getUsage() {
		return format(rb,"usage.set");
	}
}
