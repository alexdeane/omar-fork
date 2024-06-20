/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/function/AbstractFunctionTest.java,v 1.2 2006/11/07 20:11:14 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import junit.framework.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.admin.JAXRService;
import org.freebxml.omar.client.common.ClientTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * Common code for JUnit tests for admin tool functions.
 *
 * @author Tony Graham
 */
public abstract class AbstractFunctionTest extends ClientTest {
    private static final Log log = LogFactory.getLog(AbstractFunctionTest.class.getName());
    protected AdminFunctionContext context;
    protected ByteArrayOutputStream testOut;

    public AbstractFunctionTest(String testName) {
        super(testName);
    }

    /** Creates and sets up an AdminFunctionContext for use by a test. */
    protected void setUp() throws Exception {
	super.setUp();
        context = AdminFunctionContext.getInstance();

        testOut = new ByteArrayOutputStream();
        context.setOutStream(new PrintStream(testOut));

        JAXRService service = new JAXRService();
        service.setAlias(getTestUserAlias());
        service.setKeyPass(getTestUserKeypass());
        service.connect();
        context.setService(service);
    }

    /** Returns the platform-specific line separator. */
    protected String newLine() {
        /* Be careful of the LineSeparator with println */
        String newLine = System.getProperty("line.separator");

        if (newLine == null) {
            newLine = "\n";
        }

        return newLine;
    }
}
