/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/ui/common/UIUtilityTest.java,v 1.2 2005/11/21 04:28:15 farrukh_najmi Exp $
 *
 */
package org.freebxml.omar.client.ui.common;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;


/**
 * jUnit Test for UIUtility
 *
 * @author <a href="farrukh.najmi@Sun.com">Farrukh S. Najmi</a>
 */
public class UIUtilityTest extends ClientTest {

    public UIUtilityTest(String testName) {
        super(testName);
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

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(UIUtilityTest.class);
        return suite;
    }
     
    public void testConvertToRimBinding() throws Exception{
        try {
        org.oasis.ebxml.registry.bindings.rim.Name rimName = null;
        UIUtility uiu = UIUtility.getInstance();
        
        String testName = "omar";
        
        org.freebxml.omar.client.ui.common.conf.bindings.Name uiName = uiu.fac.createName();
        org.freebxml.omar.client.ui.common.conf.bindings.LocalizedString ls = uiu.fac.createLocalizedString();
        ls.setValue(testName);
        uiName.getLocalizedString().add(ls);
        
        Object rimNameObj = uiu.convertToRimBinding(uiName);
        assertTrue("Returned value not a RIM type.", rimNameObj instanceof org.oasis.ebxml.registry.bindings.rim.Name);
        
        rimName = (org.oasis.ebxml.registry.bindings.rim.Name)rimNameObj;
        assertEquals("String did not match.", testName, ((org.oasis.ebxml.registry.bindings.rim.LocalizedString)(rimName.getLocalizedString().get(0))).getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("This is a known failure that needs to be investigated and fixed. ");
        } 
    }

}