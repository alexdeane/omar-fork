/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/SubscriptionTest.java,v 1.4 2006/02/14 08:57:14 lewiecd Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.infomodel.RegistryObject;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.infomodel.SubscriptionImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.NotifyAction;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;
import org.oasis.ebxml.registry.bindings.rim.impl.SubscriptionTypeImpl;

/**
 * jUnit Test for Subscriptions.
 *
 * @author "Raavicharla, Praveena" <praavicharla@fgm.com>
 */
public class SubscriptionTest extends ClientTest {
    
    
    public SubscriptionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SubscriptionTest.class);
        return suite;
    }
    
    public void testSubmitSubscription() throws Exception {
        
        // the id of the selector is an adhoc query id from the demo DB.
        SubscriptionImpl subs = lcm.createSubscription("urn:freebxml:registry:demoDB:query:EpidemicAlertQuery");        
      
        List actionl = subs.getAction();
        NotifyAction notifyAct = BindingUtility.getInstance().rimFac.createNotifyAction();
        notifyAct.setEndPoint("urn:freebxml:registry:demoDB:serviceBinding:EpidemicAlertListenerServiceBinding");
        notifyAct.setNotificationOption(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_Objects);
        actionl.add(notifyAct);
        
        String id = subs.getKey().getId();
        List l = new ArrayList();
        l.clear();
        l.add(subs);
        lcm.saveObjects(l);

        RegistryObject ro = bqm.getRegistryObject(id);
        assertNotNull("The Subscription Object is not saved.", ro);
        
        l.clear();
        l.add(subs.getKey());
        lcm.deleteObjects(l);
        ro = bqm.getRegistryObject(id);
        assertNull("The Subscription Object is not deleted.", ro);
    }
    
    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
