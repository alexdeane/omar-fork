/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/util/CertificateUtilTest.java,v 1.4 2006/11/08 07:30:09 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.io.File;
import javax.xml.registry.infomodel.User;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test for CertificateUtil class
 *
 * @author Farrukh Najmi
 */
public class CertificateUtilTest extends ClientTest {
    static final String alias = "testGenerateRegistryIssuedCertificate";
    static final String p12FileName = alias + ".p12";

    public CertificateUtilTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(CertificateUtilTest.class);
        return suite;
    }

    /**
     * Ensure we don't get confused by a .p12 file that already exists or
     * leave one lying around
     */
    private void setUp_tearDown_internal() throws Exception {
	// TODO: could also move userRegInfo construction and removal of
	// existing certificate from client keystore into this method
        File p12File = new File(p12FileName);
        if (p12File.exists()) {
            p12File.delete();
        }
    }

    /**
     * Ensure we don't get confused by a .p12 file that already exists
     */
    public void setUp() throws Exception {
	super.setUp();
	setUp_tearDown_internal();
    }

    /**
     * Ensure we don't leave a .p12 file lying around
     */
    public void tearDown() throws Exception {
	super.tearDown();
        setUp_tearDown_internal();
    }

    /**
     * Tests generateRegistryIssuedCertificate method.
     */
    public void testGenerateRegistryIssuedCertificate() throws Exception {
        User user = createUser("TestUser." + alias);
        UserRegistrationInfo userRegInfo = new UserRegistrationInfo(user);
        char[] storePassword = userRegInfo.getStorePassword();
        char[] keyPassword = alias.toCharArray();

        userRegInfo.setAlias(alias);
        userRegInfo.setKeyPassword(keyPassword);
        userRegInfo.setP12File(p12FileName);

        // Remove existing cert if any
        if (CertificateUtil.certificateExists(alias, storePassword)) {
            CertificateUtil.removeCertificate(alias, storePassword);
        }

	CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
	assertTrue("Certificate not found after creating it",
		   CertificateUtil.certificateExists(alias, storePassword));

	File p12File = new File(p12FileName);
	assertTrue("p12 file was not created", p12File.exists());

	CertificateUtil.removeCertificate(alias, storePassword);
	assertFalse("Certificate found when it should have been removed",
		    CertificateUtil.certificateExists(alias, storePassword));
    }
}
