/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004-2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/rest/RestTest.java,v 1.4 2006/02/24 02:35:19 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.rest;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test to test RestServlet interface
 *
 * @author Paul Sterk
 */
public class RestTest extends ClientTest {
    public RestTest(String testName) {
	// Do not create a JAXR connection, unecessary for this test
        super(testName, false);
    }

    public static Test suite() throws Exception {
	TestSuite suite = null;
	if (localCallMode) {
	    // No equivalent for localCall in REST interface; do nothing if
	    // in that mode
	    suite = new TestSuite();
	} else {
	    suite = new TestSuite(RestTest.class);
	}
        return suite;
    }

    public void testQueryManagerGetRegistryObject() throws Exception {
        String restURL = regHttpUrl +
            "?interface=QueryManager" +
            "&method=getRegistryObject" +
            "&param-id=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP";

        URL restUrl = new URL(restURL);
        HttpURLConnection urlConn =
	    (HttpURLConnection)restUrl.openConnection();
        urlConn.connect();

        int responseCode = urlConn.getResponseCode();
        assertTrue("Rest request failed.",
		   responseCode == HttpURLConnection.HTTP_OK);
    }

    public void testQueryManagerGetRepositoryItem() throws Exception {
        String restURL = regHttpUrl +
            "?interface=QueryManager" +
            "&method=getRepositoryItem" +
            "&param-id=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP" +
            "&param-lid=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP" +
            "&param-versionName=1.1";

        URL restUrl = new URL(restURL);
        HttpURLConnection urlConn =
	    (HttpURLConnection)restUrl.openConnection();
        urlConn.connect();

        int responseCode = urlConn.getResponseCode();
        assertTrue("Rest request failed.",
		   responseCode == HttpURLConnection.HTTP_OK);
    }
}
