/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Users.java,v 1.5 2004/11/11 20:56:05 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public class Users extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        String queryStr;

        queryStr = "SELECT DISTINCT u.* FROM user_ u";

        Collection coll = context.getService().doSQLQuery(queryStr);
        Iterator iter = coll.iterator();

        String[] users = new String[coll.size()];

        int usersIndex = 0;

        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject) iter.next();
            context.printMessage("%" + usersIndex + ":  " +
                ro.getKey().getId() + "  " + ro.getName());
            users[usersIndex++] = ro.getKey().getId();
        }

        context.setUsers(users);
    }

    public String getUsage() {
        return "users";
    }
}
