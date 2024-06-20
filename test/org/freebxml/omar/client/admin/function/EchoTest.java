/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/function/EchoTest.java,v 1.4 2004/10/01 16:06:14 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import junit.framework.*;

import org.freebxml.omar.client.admin.AdminFunctionContext;

/**
 * A JUnit TestCase to test the admin tool 'echo' command.
 *
 * @author Tony Graham
 */
public class EchoTest extends AbstractFunctionTest {
    public EchoTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        return new TestSuite(EchoTest.class);
    }

    // test methods
    //

    /**
     * Checks that execute() echoes a string correctly.
     *
     * @exception Exception if an error occurs
     */
    public void testExecute() throws Exception {
        String testString = "EchoTest";

        Echo echo = new Echo();

        echo.execute(context, testString);

        String expected = testString + newLine();

        assertEquals("Unexpected output", expected, testOut.toString());
    }

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName() +
                " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
