/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/AuditableEventImpl.java,v 1.26 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl;

/**
 * Implements JAXR API interface named AuditableEvent.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class AuditableEventImpl extends RegistryObjectImpl
    implements AuditableEvent {
        
    //Add to AuditableEvent interface in JAXR 2.0??
    public static int eventUndeprecated = EVENT_TYPE_UNDEPRECATED;
    public static final int EVENT_TYPE_APPROVED = (eventUndeprecated + 1);
    public static final int EVENT_TYPE_DOWNLOADED = (EVENT_TYPE_APPROVED + 1);
    public static final int EVENT_TYPE_RELOCATED = (EVENT_TYPE_DOWNLOADED + 1);
    public static ArrayList eventTypes;

    static {
        eventTypes = new ArrayList();

        //Order is based on the numerical order of constants in AuditableEvent
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Created);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deprecated);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Updated);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Undeprecated);
                
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Approved);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Downloaded);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated);
    };
        
        
    private RegistryObjectRef userRef = null;
    private Timestamp timestamp = null;
    private String eventType = null;
    private String requestId = null;
    
    //List of RegistryObjectRefs
    private List affectedObjectRefs = new ArrayList();
    private List affectedObjects = new ArrayList();

    public AuditableEventImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public AuditableEventImpl(LifeCycleManagerImpl lcm, AuditableEventType ebAE)
        throws JAXRException {
        super(lcm, ebAE);

        // Set the eventType
        eventType = ebAE.getEventType();

        List _affectedObjectRefs = ebAE.getAffectedObjects().getObjectRef();
        Iterator iter = _affectedObjectRefs.iterator();
        while (iter.hasNext()) {
            ObjectRefType ref = (ObjectRefType)iter.next();            
            RegistryObjectRef registryObjectRef = new RegistryObjectRef(lcm, ref);
            affectedObjectRefs.add(registryObjectRef);
        }
        timestamp = new Timestamp(ebAE.getTimestamp().getTimeInMillis());
        userRef = new RegistryObjectRef(lcm, ebAE.getUser());
        requestId = ebAE.getRequestId();
    }

    //Possible addition to JAXR 2.0??
    public RegistryObjectRef getUserRef() throws JAXRException {
        return userRef;
    }

    public User getUser() throws JAXRException {
        User user = null;

        if (userRef != null) {
            user = (User) userRef.getRegistryObject("User");
        }

        return user;
    }

    public Timestamp getTimestamp() throws JAXRException {
        return timestamp;
    }

    /**
     * Deprecate in JAXR 2.0??
     */
    public int getEventType() throws JAXRException {
        int eventTypeAsInteger = eventTypes.indexOf(eventType);        

        return eventTypeAsInteger;
    }

    /**
     * Gets the event type as String.
     * Add to JAXR 2.0??
     * 
     */
    public String getEventType1() throws JAXRException {
        return eventType;
    }
    
    /**
     * An AuditableEvent is now associated with multiple objects
     * that were impacted in the same request. This method is left
     * for API compliance and backward compatibility.
     *
     * @retun the first RegistryObject in list of RegistryObjects affected by this AuditableEvent.
     *
     * @deprecated: Use getAffectedObjects instead.
     */
    public RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;
        
        List ros = getAffectedObjects();
        if (ros.size() > 0) {
            ro = (RegistryObject)ros.get(0);
        }
        
        return ro;
    }

    /**
     * Add to JAXR 2.0
     *
     * @return the List of objectReferences for objects affected by this event
     */
    public List getAffectedObjectRefs() throws JAXRException {
        return affectedObjectRefs;
    }   
    
    /**
     * Add to JAXR 2.0
     *
     * @return the List of objects affected by this event
     */
    public List getAffectedObjects() throws JAXRException {
        if (affectedObjects.size() < 1) {
            Iterator objRefItr = affectedObjectRefs.iterator();
            while (objRefItr.hasNext()) {
                RegistryObjectRef rof = (RegistryObjectRef)objRefItr.next();
                RegistryObject ro = rof.getRegistryObject("RegistryObject");
                if (!(ro.getKey().getId()).equals(getId())) {
                    affectedObjects.add(ro);
                } 
            }
        }
        return affectedObjects;
    }
    
    public HashSet getComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getAffectedObjects());
        return composedObjects;
    }
  
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

        try {
            org.oasis.ebxml.registry.bindings.rim.AuditableEvent ebOrg = bu.rimFac.createAuditableEvent();
            setBindingObject(ebOrg);

            return ebOrg;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(AuditableEventType ebAE)
        throws JAXRException {
        super.setBindingObject(ebAE);
        
        //No need to set field as object is immutable for now.
    }

    public String getRequestId() {
        return requestId;
    }
}
