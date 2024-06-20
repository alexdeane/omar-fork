/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/Notification.java,v 1.2 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.List;
import javax.xml.registry.JAXRException;

/**
 * Future JAXR API interface for representing a registry notification.
 * Proposed to be added to JAXR 2.0 API??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public interface Notification {
    /**
     * Gets the objects in the notification.
     *
     */
    public List getRegistryObjects() throws JAXRException;
    
    /**
     * Gets the reference to the Subscription object that this Notification is for.
     */
    public RegistryObjectRef getSubscriptionRef() throws JAXRException;
}
