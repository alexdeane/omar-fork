/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header:
 * /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/persistence/rdb/NotifyActionDAO.java,v
 * 1.3 2003/11/03 01:10:09 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;


/**
 * The top level manager that manges all aspects of event management in the registry. This includes listening for events, matching events to subscriptions and notifying subscribers when an event matching their Subscription occurs.
 *
 * @author Farrukh S. Najmi
 * @author Nikola Stojanovic
 */
public class EventManager implements AuditableEventListener {
    private static final Log log = LogFactory.getLog(EventManager.class);
    
    /** Creates a new instance of SubscriptionManager */
    protected EventManager() {
        try {
            subscriptionMatcher = new SubscriptionMatcher();
            notifier = new NotifierImpl();
        }
        catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.ConstructorFailed"), e);
        }
    }
    
    /*
     * Responds to an AuditableEvent. Called by the PersistenceManager.
     * Gets the subscriptions that match this event.
     * For each matching Subscription, sends notifications to subscribers
     * regarding this event.
     *
     * @see org.freebxml.omar.server.persistence.AuditableEventListener#onEvent(org.oasis.ebxml.registry.bindings.rim.AuditableEventType)
     */
    public void onEvent(ServerRequestContext context, AuditableEventType ae)  {
        try {
            //Make a new Context since this once has just ben committed
            //Fixes an infinite loop or deadlock when xxCache.onEvent(...) did other queries
            ServerRequestContext newContext = new ServerRequestContext("org.freebxml.omar.server.event.EventManager.onEvent", null);
            newContext.setUser(context.getUser());
            context = newContext;
            
            OnEventRunnable r = new OnEventRunnable(context, ae);
            new Thread(r, "org.freebxml.omar.server.event.EventManager#onEvent").start();
        } catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.ExceptionCaughtOnEvent"), e);
        }
        
    }
    
    private void onEventInternal(ServerRequestContext context, AuditableEventType ae)  {
        
        try {
            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            //marshaller.marshal(ae, System.err);
            
            //Get the HashMap where keys are the Subscriptions that match this event
            //and values are the matchedObjects for that Subscription.
            HashMap subscriptionsMap = subscriptionMatcher.getMatchedSubscriptionsMap(context, ae);

            //Process each matching Subscription
            Iterator subscriptionsIter = subscriptionsMap.keySet().iterator();
            while (subscriptionsIter.hasNext()) {
                SubscriptionType subscription = (SubscriptionType)subscriptionsIter.next();

                processSubscription(context, subscription, (List)(subscriptionsMap.get(subscription)), ae);
            }
        }
        catch (Exception e) {
            try {
                context.rollback();
            } catch (RegistryException e1) {
                log.error(e1, e1);
            }
            log.error(ServerResourceBundle.getInstance().getString("message.ExceptionCaughtOnEvent"), e);
        }
        
        try {
            context.commit();
        } catch (RegistryException e) {
            log.error(e, e);
        }
    }
    
    /*
     * The Runnable used to spawn calls to onEventInternal() method in a separate thread.
     */
    class OnEventRunnable implements Runnable {
        ServerRequestContext context = null;
        AuditableEventType ae = null;
         OnEventRunnable(ServerRequestContext context, AuditableEventType ae) {
             this.context = context;
             this.ae = ae;
         }
 
         public void run() {
             onEventInternal(context, ae);
         }
     }
    
    
    private void processSubscription(ServerRequestContext context, SubscriptionType subscription, 
        List matchedObjects, AuditableEventType ae) throws RegistryException {

        notifier.sendNotifications(context, subscription, matchedObjects, ae);
                    
    }
            
    public synchronized static EventManager getInstance(){
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }
    
    /**
     * @link aggregationByValue
     */
    private SubscriptionMatcher subscriptionMatcher;
    
    /**
     * @link aggregationByValue
     */
    private NotifierImpl notifier;
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private EventManager _eventManager; */
    private static EventManager instance = null;
}
