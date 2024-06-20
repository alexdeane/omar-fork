/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/NotificationImpl.java,v 1.5 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.HashMap;
import javax.xml.registry.InvalidRequestException;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Implements future JAXR API interface named Notification.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class NotificationImpl extends RegistryObjectImpl implements Notification {
    NotificationType ebNotification = null;
    
    public NotificationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public NotificationImpl(LifeCycleManagerImpl lcm,
        NotificationType ebNotification) throws JAXRException {
        super(lcm, ebNotification);
        this.ebNotification = ebNotification;
    }


    /**
     * Gets the objects in the notification.
     *
     */
    public List getRegistryObjects() throws JAXRException {                
        return  JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, ebNotification.getRegistryObjectList().getIdentifiable(), null);
    }
    
    /**
     * Gets the reference to the Subscription object that this Notification is for.
     */
    public RegistryObjectRef getSubscriptionRef() throws JAXRException {
        RegistryObjectRef subscriptionRef = new RegistryObjectRef(lcm, ebNotification.getSubscription());
        return subscriptionRef;
    }    

    public Object toBindingObject() throws JAXRException {
        /*
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Notification ebNotification = factory.createNotification();

            setBindingObject(ebNotification);

            return ebNotification;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
         **/
        
        throw new InvalidRequestException("Cannot save Notification via client API to registry. It can only be read from registry.");
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.NotificationType ebNotification)
        throws JAXRException {
        super.setBindingObject(ebNotification);
    }

}
