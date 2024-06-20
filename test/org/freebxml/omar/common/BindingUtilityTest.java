/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/common/BindingUtilityTest.java,v 1.1 2006/02/08 02:01:05 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.common;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.oasis.ebxml.registry.bindings.rim.ExternalLink;


/**
 * jUnit Test for BindingUtility class.
 *
 * @author Farrukh Najmi
 */
public class BindingUtilityTest extends OMARTest {
    
    static BindingUtility bu = BindingUtility.getInstance();
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(BindingUtilityTest.class);
        return suite;
    }
    
    
    public BindingUtilityTest(String testName) {
        super(testName);        
    }
    
    public void testGetObjectType() throws Exception {
        ExternalLink el = bu.rimFac.createExternalLink();
        String objectType = el.getObjectType();
        assertNull(objectType);
        objectType = bu.getObjectType(el);
        assertEquals(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_ExternalLink, 
                    objectType);
    }
}
