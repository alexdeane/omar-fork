/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Chown.java,v 1.8 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Collection;

import javax.xml.registry.BulkResponse;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.Utility;

public class Chown extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context,
    String[] args) throws Exception {
        if (context.getRegistryObjects() == null ||
        context.getRegistryObjects().size() == 0) {
            context.printMessage(format(rb,"noRegistryObjects"));
            return;
        }
        
        if (args == null ||
        args.length != 1) {
            context.printMessage(format(rb,"oneUserId"));
            return;
        }
        
        String userId = args[0];
        String userID = null;
        
        if (userId.matches("^%[0-9]+$")) {
            try {
                int numericUserId =
                Integer.valueOf(userId.substring(1)).intValue();
                userID = context.getUsers()[numericUserId];
            } catch (Exception e) {
                context.printMessage(format(rb,"invalidIdReference"));
                return;
            }
        } else if (Utility.getInstance().isValidURN(userId)) {
            userID = userId;
        } else {
            context.printMessage(format(rb,"invalidIdReference"));
            return;
        }
        
        if (context.getDebug()) {
            context.printMessage(context.getService().getLCM().getClass().toString());
        }
        javax.xml.registry.BusinessLifeCycleManager lcm =
        context.getService().getLCM();
        Method m = null;
        try {
            m = lcm.getClass().getMethod("changeObjectsOwner",
            new Class[] {Collection.class,
            String.class});
        } catch (Exception e) {
            context.printMessage(format(rb, "changeObjectOwner"));
            return;
        }
        
        
        Collection keys = JAXRUtility.getKeysFromObjects(context.getRegistryObjects());
        
		BulkResponse response =
            (BulkResponse) m.invoke(lcm,
									new Object[] {keys, userID});
		JAXRUtility.checkBulkResponse(response);
    }
    
    public String getUsage() {
        return format(rb, "usage.chown");
    }
}
