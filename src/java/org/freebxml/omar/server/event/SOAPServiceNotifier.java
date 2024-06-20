/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/event/SOAPServiceNotifier.java,v 1.12 2006/04/26 06:21:17 doballve Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;

import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.jaxrpc.notificationListener.client.NotificationListenerPortType_Stub;
import org.freebxml.omar.common.jaxrpc.notificationListener.client.NotificationListenerSOAPService_Impl;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;

/**
 * Notifier used to send notifications to a SOAP-based web service when its Subscription matches a registry event.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SOAPServiceNotifier extends AbstractNotifier {
    
    protected void sendNotification(ServerRequestContext context, NotifyActionType notifyAction, 
        NotificationType notification, AuditableEventType ae) throws RegistryException {
        System.err.println("Sending notification to web service");
        
        try {
            //Get the ServiceBinding id that represents the endPoint
            String endPoint =  notifyAction.getEndPoint();                       

            //Now get the ServiceBinding and its acceessURI
            ServiceBindingType serviceBinding = 
                (ServiceBindingType)PersistenceManagerFactory.getInstance().
                getPersistenceManager().
                getRegistryObject(context, endPoint, "ServiceBinding");
            
            String accessURI = serviceBinding.getAccessURI();
            
            NotificationListenerPortType_Stub stub = (NotificationListenerPortType_Stub)
            (new NotificationListenerSOAPService_Impl().getNotificationListenerPort());
            ((Stub)stub)._setProperty(
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            
            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(notification, System.err);
            
            SOAPElement soapElem = BindingUtility.getInstance().getSOAPElementFromBindingObject(notification);
            
            String notificationOption =  notifyAction.getNotificationOption();

            if (notificationOption.equals(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_Objects)) {
                stub.onObjectsNotification(soapElem);
            }
            else if (notificationOption.equals(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_ObjectRefs)) {
                stub.onObjectRefsNotification(soapElem);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RegistryException(e);
        } 
        
    }
    
}
