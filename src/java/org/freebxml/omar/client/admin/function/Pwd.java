/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Pwd.java,v 1.3 2004/09/27 14:10:26 tonygraham Exp $
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


public class Pwd extends AbstractAdminFunction {
    private class Subpath {
        private RegistryPackage rp;
        private String subpath;

        protected Subpath() {
        }

        public Subpath(RegistryPackage rp, String subpath) {
            this.rp = rp;
            this.subpath = subpath;
        }

        public RegistryPackage getRP() {
            return rp;
        }

        public String getSubpath() {
            return subpath;
        }
    }

    public void execute(AdminFunctionContext context, String[] args)
        throws Exception {
        RegistryPackage currentRP = context.getCurrentRP();

        // if not in a RegistryPackage, then it's the root.
        if (currentRP == null) {
            context.printMessage("/");
        } else {
            LinkedList subpathList = new LinkedList();
            subpathList.add(new Subpath(currentRP,
                    currentRP.getName().toString()));

            while (!subpathList.isEmpty()) {
                Subpath currSubpath = (Subpath) subpathList.removeFirst();

                Collection sourceRPs = context.getService()
                                              .getSourceRegistryPackages(currSubpath.getRP());

                if (sourceRPs.size() == 0) {
                    context.printMessage("(" + Locale.getDefault() + ") /" +
                        currSubpath.getSubpath());
                } else {
                    Iterator rpIter = sourceRPs.iterator();

                    while (rpIter.hasNext()) {
                        RegistryPackage sourceRP = (RegistryPackage) rpIter.next();
                        String sourceRPName = sourceRP.getName().toString();

                        subpathList.add(new Subpath(sourceRP,
                                sourceRPName + "/" + currSubpath.getSubpath()));
                    }
                }
            }
        }
    }

    public String getUsage() {
        return "pwd";
    }
}
