/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Cd.java,v 1.9 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.admin.AdminShell;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.Utility;

import java.lang.reflect.Method;

import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.RegistryPackage;


public class Cd extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String useArgs = args;

        String[] useArgsArray = useArgs.split("(?<=^|[^\\\\])\"", -1);

        useArgs = useArgsArray[0];

        if (context.getDebug()) {
            context.printMessage(args);

            for (int i = 0; i < useArgsArray.length; i++) {
                context.printMessage(useArgsArray[i] + ":");
            }

            context.printMessage();
        }

        if ((useArgsArray.length > 1)) {
            // An even number of quotes results in an odd-number array length
            if ((useArgsArray.length % 2) == 0) {
                context.printMessage(format(rb, "unbalancedQuotes"));

                return;
            }

            for (int i = 1; i < useArgsArray.length; i += 2) {
                useArgsArray[i] = useArgsArray[i].replaceAll(" ", "\\\\ ");
            }

            for (int i = 1; i < useArgsArray.length; i++) {
                useArgs += useArgsArray[i];
            }
        }

        useArgs = useArgs.replaceAll("\\\\\"", "\"");
        useArgs = useArgs.replaceAll("\\\\ ", " ");

        if (context.getDebug()) {
            context.printMessage(":" + useArgs + ":");
        }

        RegistryPackage newRP;

        if (Utility.getInstance().isValidURN(useArgs)) {
            newRP = context.getService().getRegistryPackageByID(useArgs);
        } else {
            newRP = context.getService().getRegistryPackage(useArgs);
        }

        context.setCurrentRP(newRP);
    }

    public String getUsage() {
        return format(rb, "usage.cd");
    }
}
