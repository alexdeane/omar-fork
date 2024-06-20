/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/AddAssociation.java,v 1.9 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.Utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public class AddAssociation extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        String type = null;

        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String[] tokens = args.split("\\s+");

        int tIndex = 0;

        for (tIndex = 0;
                ((tIndex < tokens.length) && tokens[tIndex].startsWith("-"));
                tIndex++) {
            String option = tokens[tIndex];

            if (collator.compare(option, "-type") == 0) {
                if (++tIndex == tokens.length) {
                    context.printMessage(getUsage());
                }

                type = tokens[tIndex];
            } else {
                context.printMessage(format(rb,"invalidArgument",
					    new Object[] { tokens[tIndex] }));

                return;
            }
        }

        if (type == null) {
            context.printMessage(format(rb,"associationTypeRequired"));

            return;
        }

        if (tIndex != (tokens.length - 2)) {
            context.printMessage(format(rb,"invalidArgument",
					new Object[] { tokens[tIndex] }));

            return;
        }

        String sourceID = tokens[tIndex++];

        if (!Utility.getInstance().isValidURN(sourceID)) {
            context.printMessage(format(rb,"urnRequired",
					new Object[] { sourceID }));

            return;
        }

        String targetID = tokens[tIndex];

        if (!Utility.getInstance().isValidURN(targetID)) {
            context.printMessage(format(rb,"urnRequired",
					new Object[] { targetID }));

            return;
        }

        // Check source and target aren't identical
        if (sourceID.equals(targetID)) {
            context.printMessage(format(rb,"sourceIsTarget",
					new Object[] { sourceID }));

            return;
        }

        // Check that the source exists
        String queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro WHERE ro.id = '" + sourceID + "'";

        javax.xml.registry.Query query = context.getService().getDQM()
                                                .createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        javax.xml.registry.BulkResponse resp = context.getService().getDQM()
                                                      .executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        Collection registryObjects = resp.getCollection();

        if (registryObjects.size() == 0) {
            context.printMessage(format(rb,"doesNotExist",
					new Object[] { sourceID }));

            return;
        } else if (registryObjects.size() > 1) {
            context.printMessage(format(rb,"multipleExist",
					new Object[] { sourceID }));

            return;
        }

        RegistryObject sourceRO = (RegistryObject) registryObjects.iterator()
                                                                  .next();

        // Check that the target exists
        queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro WHERE ro.id = '" + targetID + "'";

        query = context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        resp = context.getService().getDQM().executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        registryObjects = resp.getCollection();

        if (registryObjects.size() == 0) {
            context.printMessage(format(rb,"doesNotExist",
					new Object[] { targetID }));

            return;
        } else if (registryObjects.size() > 1) {
            context.printMessage(format(rb,"multipleExist",
					new Object[] { targetID }));

            return;
        }

        RegistryObject targetRO = (RegistryObject) registryObjects.iterator()
                                                                  .next();

        // Check that an association does not already exist
        queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro, Association ass " +
            "WHERE (ass.sourceObject = '" + sourceID +
            "' AND ass.targetObject = '" + targetID +
            "') OR (ass.sourceObject = '" + targetID +
            "' AND ass.targetObject = '" + sourceID + "')";

        query = context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        resp = context.getService().getDQM().executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        registryObjects = resp.getCollection();

        if (registryObjects.size() != 0) {
            context.printMessage(format(rb,"associationExists",
					new Object[] { sourceID, targetID }));
        }

        Concept assocConcept = context.getService().getBQM().findConceptByPath("/AssociationType/" +
                type);

        if (assocConcept == null) {
            context.printMessage(format(rb,"noConceptForType",
					new Object[] { type }));

            return;
        }

        Association assoc = context.getService().getLCM().createAssociation(targetRO,
                assocConcept);

        sourceRO.addAssociation(assoc);

        HashSet assocColl = new HashSet();

        assocColl.add(assoc);

        context.getService().getLCM().saveAssociations(assocColl, true);

        if (context.getVerbose() || context.getDebug()) {
            context.printMessage(assoc.getKey().getId());
        }
    }

    public String getUsage() {
        return format(rb, "usage.addAssoc");
    }
}
