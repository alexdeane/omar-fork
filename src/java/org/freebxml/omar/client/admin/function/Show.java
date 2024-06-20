/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Show.java,v 1.8 2005/04/26 22:55:45 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;

import java.io.PrintStream;

import java.text.Collator;

import java.util.Locale;


public class Show extends AbstractAdminFunction {
    private static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        if (context.getOutStream() == null) {
            throw new NullPointerException();
        }

        if (args == null) {
            showDebug(context);
            showEditor(context);
            showLastException(context);
            showLocalDir(context);
            showLocale(context);
            showVerbose(context);
        } else if (collator.compare(args, "debug") == 0) {
            showDebug(context);
        } else if (collator.compare(args, "editor") == 0) {
            showEditor(context);
        } else if (collator.compare(args, "exception") == 0) {
            showLastException(context);
        } else if (collator.compare(args, "localdir") == 0) {
            showLocalDir(context);
        } else if (collator.compare(args, "locale") == 0) {
            showLocale(context);
        } else if (collator.compare(args, "verbose") == 0) {
            showVerbose(context);
        } else {
            context.printMessage(format(rb,"invalidArgument",
					new Object[] { args }));
        }
    }

    private void showDebug(AdminFunctionContext context) {
        context.printMessage(format(rb,"debugStatus",
				    new Object[] {
					    new Boolean(context.getDebug())
				    }));
    }

    private void showEditor(AdminFunctionContext context) {
        context.printMessage(format(rb,"editorStatus",
				    new Object[] { context.getEditor() }));
    }

    private void showLastException(AdminFunctionContext context) {
        String lastException = context.getLastException();

        if (lastException != null) {
            context.printMessage(format(rb,"lastException"));
            context.printMessage(lastException);
        } else {
            context.printMessage(format(rb,"noLastException"));
        }
    }

    private void showLocalDir(AdminFunctionContext context) {
        context.printMessage(format(rb,"localDir",
				    new Object[] {
					    context.getLocalDir()
				    }));
    }

    private void showLocale(AdminFunctionContext context) {
        context.printMessage(format(rb,"localeStatus",
				    new Object[] {
					    Locale.getDefault().getDisplayName(),
					    Locale.getDefault()
				    }));
    }

    private void showVerbose(AdminFunctionContext context) {
        context.printMessage(format(rb,"verboseStatus",
				    new Object[] {
					    new Boolean(context.getVerbose())
				    }));
    }

    public String getUsage() {
        return "show [debug | editor | exception | localdir | locale | verbose]?";
    }
}
