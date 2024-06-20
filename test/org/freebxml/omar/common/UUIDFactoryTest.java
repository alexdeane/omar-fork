/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002-2004 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */
package org.freebxml.omar.common;

import junit.framework.TestCase;
import org.freebxml.omar.common.UUIDFactory;

/**
 * @author Tony Graham
 */
public class UUIDFactoryTest extends TestCase {

    /**
     * Constructor for UUIDFactoryTest.
     * @param name
     */
    public UUIDFactoryTest(String name) {
        super(name);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //this.endorsedDirs = System.getProperty("java.endorsed.dirs");
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsValidUUID() {
		UUIDFactory uf = UUIDFactory.getInstance();

		try {
			UUIDFactory.getInstance().isValidUUID(null);
			fail("isValidUUID is not expected to handle null values.");
		} catch (Exception e) {}

        assertTrue(!uf.isValidUUID(""));
        assertTrue(!uf.isValidUUID("a"));
        assertTrue(uf.isValidUUID("57d925e0-7ad2-4dc3-ace1-b8a4064abcc7"));
        assertTrue(!uf.isValidUUID("57d925e0-7ad2-4dc3-ace1-b8a4064abcc7"
								   + "a"));
    }
    
}
