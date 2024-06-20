/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;


/**
 * Implements future JAXR API interface named Subscription.
 *
 * @author <a href="mailto:Paul.Sterk@Sun.COM">Paul Sterk</a>
 */
public class SubscriptionImpl extends RegistryObjectImpl {

    private List action = new ArrayList();
    private Calendar endTime;
    private String notificationInterval;
    
    // Reference to an AdhocQuery object
    private RegistryObjectRef selector;
    private Calendar startTime;
    
    public SubscriptionImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm); 
    }

    public SubscriptionImpl(LifeCycleManagerImpl lcm,
        SubscriptionType subscriptionObj) throws JAXRException {
        super(lcm, subscriptionObj);

        action = subscriptionObj.getAction();
        endTime = subscriptionObj.getEndTime();
        notificationInterval = subscriptionObj.getNotificationInterval();
        selector = new RegistryObjectRef(lcm, subscriptionObj.getSelector());
        startTime = subscriptionObj.getStartTime();
    }
    
    public List getAction() {
        return action;
    }
    
    public Calendar getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }
           
    public String getNotificationInterval() {
        return notificationInterval;
    }
           
    public void setNotificationInterval(String notificationInterval) {
        this.notificationInterval = notificationInterval;
    }
  
    public RegistryObjectRef getSelector() {
        return selector;
    }
    
    public void setSelector(RegistryObjectRef selector) {
        this.selector = selector;
    }
    
    public Calendar getStartDate() {
        return startTime;
    }
    
    public void setStartDate(Calendar startTime) {
        this.startTime = startTime;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Subscription ebBinding = 
                factory.createSubscription();

            setBindingObject(ebBinding);

            return ebBinding;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    
    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.Subscription ebSubscription)
        throws JAXRException {
        super.setBindingObject(ebSubscription);
        
        ebSubscription.getAction().addAll(action);
        ebSubscription.setSelector(selector.getId());
        ebSubscription.setStartTime(startTime);
        ebSubscription.setEndTime(endTime);
        ebSubscription.setNotificationInterval(notificationInterval);
    }
}
