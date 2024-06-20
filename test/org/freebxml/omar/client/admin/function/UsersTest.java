/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/function/UsersTest.java,v 1.1 2004/10/01 16:22:43 tonygraham Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.admin.function;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.*;

import org.freebxml.omar.client.admin.AdminFunctionContext;

/**
 * A JUnit TestCase to test the admin tool 'users' command
 *
 * @author Tony Graham
 */
public class UsersTest extends AbstractFunctionTest {
    
    public UsersTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        return new TestSuite(UsersTest.class);
    }

    // test methods 
    
    //
    public void testExecute() throws Exception {
	Users users = new Users();

	users.execute(context,
		      "");

	assertNotNull("Expected registry to have users.",
		      context.getUsers());
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
