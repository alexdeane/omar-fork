/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/AdminToolTest.java,v 1.3 2006/09/21 10:08:58 vikram_blr Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test used to test AdmninTool class.
 *
 *
 * @author Farrukh Najmi
 */
public class AdminToolTest extends ClientTest {
    
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AdminToolTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AdminToolTest("testDummy"));
        
        return suite;
    }
    
    
    public AdminToolTest(String testName) {
        super(testName);        
    }
    
    public void testAddUser() throws Exception {
        String id = org.freebxml.omar.common.Utility.getInstance().createId();

        //add user -fn ble -ln bla -alias blablak -email ble@bla.com
        String [] args = {
            "-verbose", "-command", "add user -fn " + id + " -ln " + id + " -alias  " + id + " -keypass " + id + " -org someOrg -orgunit someOrgUnit -email farid@najmi.com"
        };
        
        AdminTool adminTool = new AdminTool();
        adminTool.run(args, System.in, System.out);       
    }
}
