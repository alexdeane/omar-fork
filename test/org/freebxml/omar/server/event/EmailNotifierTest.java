/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/event/EmailNotifierTest.java,v 1.9 2006/06/23 13:58:47 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 * @author Tony Graham / Sun Microsystems
 */
public class EmailNotifierTest extends ServerTest {
    private EmailNotifier emailNotifier = new EmailNotifier();
    
    String smtpAddress;
    String fromAddress;
    String recipient;
    
    /**
     * Constructor for EmailNotifierTest
     *
     * @param name
     */
    public EmailNotifierTest(String name) {
        super(name);
        
        smtpAddress = RegistryProperties.getInstance().
        getProperty("omar.server.event.EmailNotifier.smtp.host");
        fromAddress = RegistryProperties.getInstance().
        getProperty("omar.server.event.EmailNotifier.smtp.from");
        recipient = RegistryProperties.getInstance().
        getProperty("omar.server.event.EmailNotifierTest.recipient");
    }
    
    public void testSendNotification() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("EmailNotifierTest.testSendNotification", null);
            NotifyActionType notifyAction = BindingUtility.getInstance().rimFac.createNotifyAction();

            notifyAction.setEndPoint("mailto:" + recipient);

            // Use ExternalLinkType just because it's simple to fill.
            ExternalLinkType el = BindingUtility.getInstance().rimFac.createExternalLink();

            el.setExternalURI("testSendNotification");
            el.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            ArrayList objectList = new ArrayList(1);

            objectList.add(el);

            // Copied from AbstractNotifier.java
            NotificationType notification =
                BindingUtility.getInstance().rimFac.createNotification();
            RegistryObjectListType roList =
                BindingUtility.getInstance().rimFac.createRegistryObjectList();
            roList.getIdentifiable().addAll(objectList);
            notification.setRegistryObjectList(roList);

            // End of code copied from AbstractNotifier.java

            notification.setId(org.freebxml.omar.common.Utility.getInstance().createId());
            notification.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            // Set subscription id to one created in DemoDB target
            notification.setSubscription("urn:freebxml:registry:demoDB:subscription:EpidemicAlert");
            
            AuditableEventType ae = BindingUtility.getInstance().rimFac.createAuditableEvent();
            ae.setEventType(bu.CANONICAL_EVENT_TYPE_ID_Updated);
            ae.setUser(ac.ALIAS_FARRUKH);

            emailNotifier.sendNotification(context, notifyAction, notification, ae);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }
    
    public static Test suite() {
        return new TestSuite(EmailNotifierTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
