/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/common/UtilityTest.java,v 1.6 2007/05/18 18:59:00 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class UtilityTest extends OMARTest {
    
    private static final String TMP_DIR = System.getProperty(
            "java.io.tmpdir");
    
    
    public UtilityTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
	// we're explicit only to handle case where we can't use
	// ebxmlrrSpecHome
	if (canUseEbxmlrrSpecHome) {
	    suite.addTest(new UtilityTest("testCreateZipOutputStream"));
	}
	suite.addTest(new UtilityTest("testUnzip"));
	suite.addTest(new UtilityTest("testIsValidRegistryId"));
	suite.addTest(new UtilityTest("testCreateId"));
	suite.addTest(new UtilityTest("testStripId"));
	suite.addTest(new UtilityTest("testIsValidURN"));
	suite.addTest(new UtilityTest("testIsValidURI"));
	suite.addTest(new UtilityTest("testFixURN"));
	suite.addTest(new UtilityTest("testGetURLPathFromURI"));
        suite.addTest(new UtilityTest("testMakeValidFileName"));
        return suite;
    }
    
    public void testCreateZipOutputStream() throws java.lang.Exception {
        String baseDir = ebxmlrrSpecHome + "/misc/";
        String[] relativeFilePaths = {
            "3.0/services/ebXMLRegistryServices.wsdl",
            "3.0/services/ebXMLRegistryBindings.wsdl",
            "3.0/services/ebXMLRegistryInterfaces.wsdl",
            "3.0/schema/rim.xsd",
            "3.0/schema/query.xsd",
            "3.0/schema/rs.xsd",
            "3.0/schema/lcm.xsd",
            "3.0/schema/cms.xsd",
        };
        
        File zipFile = File.createTempFile("omar-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();
        
        FileInputStream fis = new FileInputStream(zipFile);
        ArrayList files = Utility.unZip(TMP_DIR, fis);
        assertTrue(files.size() == relativeFilePaths.length);
    }
    
    /*
     * Test bug where Utility.unzip was failing for certain zip files.
     */
    public void testUnzip() throws java.lang.Exception {
        URL url = new URL("http://docs.oasis-open.org/regrep/v3.0/regrep-3.0-os.zip");
        InputStream is = url.openStream();
        ArrayList files = Utility.unZip(TMP_DIR, is);
        assertTrue("unzip failed.", files.size() > 0);
    }

    /**
     * Test of isValidRegistryId method, of class org.freebxml.omar.common.Utility.
     */
    public void testIsValidRegistryId() {
        System.out.println("testIsValidRegistryId");
        
        String uuid = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
        assertTrue(Utility.getInstance().isValidRegistryId(uuid));
        
        //Tests for id too long
        assertFalse("Did not catch an id that was too long.", Utility.getInstance().isValidRegistryId("urn:freebxml:registry:test:org.freebxml.omar.common.UtilityTest:testIsValidRegistryId:idtoolong:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    }

    /**
     * Test of createId method, of class org.freebxml.omar.common.Utility.
     */
    public void testCreateId() {
        System.out.println("testCreateId");

        String uuid = Utility.getInstance().createId();
        assertTrue(Utility.getInstance().isValidRegistryId(uuid));
    }

    /**
     * Test of stripId method, of class org.freebxml.omar.common.Utility.
     */
    public void testStripId() {
        System.out.println("testStripId");
        
        String urn = "urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP";
        String expectedStrippedUrn = "urn_oasis_names_tc_ebxml-regrep_acp_adminOnlyACP";
        String actualStrippedUrn = Utility.getInstance().stripId(urn);
        assertEquals(expectedStrippedUrn, actualStrippedUrn);
        
        String uuid = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
        String expectedStrippedUuid = "4db0761c-e613-4216-9681-e59534a660cb";
        String actualStrippedUuid = Utility.getInstance().stripId(uuid);
        assertEquals(expectedStrippedUuid, actualStrippedUuid);
    }

    /**
     * Test of isValidURN method, of class org.freebxml.omar.common.Utility.
     */
    public void testIsValidURN() {
        System.out.println("testIsValidURN");
        
        String urn = "urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP";
        assertTrue(Utility.getInstance().isValidURN(urn));
        assertFalse(Utility.getInstance().isValidURN(null));
    }

    /**
     * Test of isValidURI method, of class org.freebxml.omar.common.Utility.
     */
    public void testIsValidURI() {
        System.out.println("testIsValidURI");
        
        assertFalse(Utility.getInstance().isValidURI(null));
        assertTrue(Utility.getInstance().isValidURI("myTestURI"));
        assertTrue(Utility.getInstance().isValidURI("urn:uuid:4db0761c-e613-4216-9681-e59534a660cb"));
        assertTrue(Utility.getInstance().isValidURI("urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP"));
        assertTrue(Utility.getInstance().isValidURI("http://www.google.com/"));
        assertFalse(Utility.getInstance().isValidURI("http://www.thisserverrrdusnottttexxxiiisst.com/"));
        assertTrue(Utility.getInstance().isValidURI("ftp://ftp.ibiblio.org/"));
    }
    
    public static void testFixURN() {
        String id = Utility.fixURN("a:b:c#:d/e_:");
        assertEquals(id, "urn:a:b:c_:d:e_:");
    }
    
    public static void testGetURLPathFromURI() throws Exception {
        String id = Utility.getURLPathFromURI("urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0");
        assertEquals (id, "urn/oasis/names/tc/ebxml_regrep/wsdl/registry/bindings/3_0");
        id = Utility.getURLPathFromURI("urn:uddi-org:api_v3_binding");
        assertEquals(id, "urn/uddi_org/api_v3_binding");
    }
    
    public static void testMakeValidFileName() {
        String filename = "urn:org:acme:filename-1.0.xsd";
        filename = Utility.makeValidFileName(filename);
        assertEquals(filename, "urn-org-acme-filename-1.0.xsd");
    }
}
