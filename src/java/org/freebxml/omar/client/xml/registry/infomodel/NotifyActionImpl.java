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

import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;

import javax.xml.registry.JAXRException;


/**
 * This Action class is used to notify clients of Registry events.
 * @author <a href="mailto:Paul.Sterk@Sun.COM">Paul Sterk</a>
 */
public class NotifyActionImpl extends ActionImpl {
    
    private String endPoint;
    private String notificationOption;
    
    public NotifyActionImpl(NotifyActionType notifyObj) throws JAXRException {
        endPoint = notifyObj.getEndPoint();
        notificationOption = notifyObj.getNotificationOption();
    }
    
    public String getEndPoint() {
        return endPoint;
    }
           
    public String getNotificationOption() {       
        return notificationOption;
    }
    
}
