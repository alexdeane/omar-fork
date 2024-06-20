/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/event/NotifierImpl.java,v 1.8 2006/04/26 06:21:17 doballve Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;

/**
 * Notifier used to send notifications.
 * It uses EmailNotifier and SOAPServiceNotifier
 * depending upon the type of endPoint configured within
 * the NotifyAction. 
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class NotifierImpl extends AbstractNotifier {
    
    NotifierImpl() {
        soapNotifier = new SOAPServiceNotifier();
        emailNotifier = new EmailNotifier();            
        pluginNotifier = new PluginNotifier();            
    }
    
    protected void sendNotification(ServerRequestContext context, NotifyActionType notifyAction, 
        NotificationType notification, AuditableEventType ae) throws RegistryException {
        
        String endPoint =  notifyAction.getEndPoint();
        if (endPoint.startsWith("mailto:")) {
            //Email Notifier
            emailNotifier.sendNotification(context, notifyAction, notification, ae);
        } else if (endPoint.startsWith("urn:")) {
            //Web Service Listener notifier
            soapNotifier.sendNotification(context, notifyAction, notification, ae);
        } else {
            //In process plugin notifier
            pluginNotifier.sendNotification(context, notifyAction, notification, ae);
        }
    }
    
    
    /**
     * @link aggregationByValue
     */
    private SOAPServiceNotifier soapNotifier;
    
    /**
     * @link aggregationByValue
     */
    private EmailNotifier emailNotifier;
    
    /**
     * @link aggregationByValue
     */
    private PluginNotifier pluginNotifier;
}
