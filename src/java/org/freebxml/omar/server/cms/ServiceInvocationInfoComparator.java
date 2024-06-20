/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ServiceInvocationInfoComparator.java,v 1.3 2005/04/21 23:11:26 joehw Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.IdentifiableComparator;
import org.freebxml.omar.server.common.AbstractFactory;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.util.ServerResourceBundle;

import java.util.Comparator;


/**
 * Compares two <code>ServiceInvocationInfo</code>.
 *
 * @author  Tony Graham
 */
public class ServiceInvocationInfoComparator implements Comparator {
    static final Comparator identifiableComparator = new IdentifiableComparator();

    public int compare(Object o1, Object o2) throws ClassCastException {
        if (!(o1 instanceof ServiceInvocationInfo)) {
            throw new ClassCastException(ServerResourceBundle.getInstance().getString("message.objectNotServiceInvocationInfo", new Object[]{o1}));
        }

        if (!(o2 instanceof ServiceInvocationInfo)) {
            throw new ClassCastException(ServerResourceBundle.getInstance().getString("message.objectNotServiceInvocationInfo", 
                    new Object[]{o2}));
        }

        ServiceInvocationInfo si1 = (ServiceInvocationInfo) o1;
        ServiceInvocationInfo si2 = (ServiceInvocationInfo) o2;

        int result = 0;
        int serviceResult = identifiableComparator.compare(si1.getService(),
                si2.getService());

        if (serviceResult != 0) {
            result = serviceResult;
        } else {
            // TODO: This should at least compare on IDs of extrinsic
            // objects representing control files.
            result = 1;
        }

        return result;
    }
}
