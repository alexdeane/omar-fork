/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/AdminShellUtilityTest.java,v 1.2 2004/11/10 14:38:55 tonygraham Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.*;

import org.freebxml.omar.client.admin.AdminFunctionContext;

/**
 * A JUnit TestCase to test the admin tool 'echo' command
 *
 * @author Tony Graham
 */
public class AdminShellUtilityTest extends TestCase {
    private static final Log log = LogFactory.getLog(AdminShellUtilityTest.class.getName());
    
    private AdminShellUtility utility;

    public AdminShellUtilityTest(String testName) {
        super(testName);

	utility = AdminShellUtility.getInstance();
    }
    
    public static Test suite() throws Exception {
        return new TestSuite(AdminShellUtilityTest.class);
    }

    // test methods 
    
    // <"\""> --> <">
    public void testNormalizeArgs1() throws Exception {
        assertEquals("Unexpected output",
		     "\"",
		     utility.normalizeArgs("\"\\\"\""));
    }

    // <\"""> --> <">
    public void testNormalizeArgs2() throws Exception {
        assertEquals("Unexpected output",
		     "\"",
		     utility.normalizeArgs("\\\"\"\""));
    }

    // <" "> --> <\ >, which Java then evaluates as < >.
    public void testNormalizeArgs3() throws Exception {
        assertEquals("Unexpected output",
		     " ",
		     utility.normalizeArgs("\" \""));
    }

    // <"> --> Exception
    public void testNormalizeArgs4() throws Exception {
        assertEquals("Unexpected output",
		     null,
		     utility.normalizeArgs("\""));
    }

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
			       + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
