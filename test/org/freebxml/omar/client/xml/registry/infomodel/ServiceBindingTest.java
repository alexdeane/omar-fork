/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ServiceBindingTest.java,v 1.1 2004/10/01 18:00:42 farrukh_najmi Exp $
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

import javax.xml.registry.infomodel.ServiceBinding;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test for ServiceBinding.
 *
 * @author Farrukh S. Najmi
 */
public class ServiceBindingTest extends ClientTest {
    
    static String serviceId = "urn:uuid:2d97634e-c8d2-4fef-b57a-d3987dce16bd";
    
    public ServiceBindingTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ServiceBindingTest.class);
        return suite;
    }
    
    /** Test submit of a Service */
    public void testSetTargetBinding() throws Exception {
        ServiceBinding binding1 = lcm.createServiceBinding();
        ServiceBinding binding2 = lcm.createServiceBinding();
        binding1.setTargetBinding(binding2);
        
        ServiceBinding targetBinding = binding1.getTargetBinding();
        assertEquals("Could not match targetBinding after set/get.", binding2, targetBinding);
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
