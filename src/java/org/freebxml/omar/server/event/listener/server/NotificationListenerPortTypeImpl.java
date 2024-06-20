/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/event/listener/server/NotificationListenerPortTypeImpl.java,v 1.6 2005/09/20 23:11:52 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event.listener.server;

import java.rmi.RemoteException;

import javax.xml.bind.Marshaller;
import javax.xml.soap.SOAPElement;

import javax.xml.registry.JAXRException;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.jaxrpc.notificationListener.server.NotificationListenerPortType;

/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NotificationListenerPortTypeImpl
implements NotificationListenerPortType {
    
        /* (non-Javadoc)
         * @see org.freebxml.omar.server.event.listener.NotificationListenerPortType#onNotification(javax.xml.soap.SOAPElement)
         */
    public void onObjectRefsNotification(SOAPElement body) throws RemoteException {
        try {
            Object obj = BindingUtility.getInstance().getBindingObjectFromSOAPElement(body);
            Marshaller m = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            m.marshal(obj, System.err);
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
        /* (non-Javadoc)
         * @see org.freebxml.omar.server.event.listener.NotificationListenerPortType#onNotification(javax.xml.soap.SOAPElement)
         */
    public void onObjectsNotification(SOAPElement body) throws RemoteException {
        try {
            Object obj = BindingUtility.getInstance().getBindingObjectFromSOAPElement(body);
            Marshaller m = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            m.marshal(obj, System.err);
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
