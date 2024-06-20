/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/common/OMARTest.java,v 1.13 2006/07/26 17:25:38 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Boolean;
import java.lang.Integer;
import java.util.HashMap;

import junit.framework.TestCase;

import org.freebxml.omar.common.CommonProperties;

/**
 * @author Farrukh Najmi
 *
 * Common base class for all tests in freebXML Registry.
 *
 */
public abstract class OMARTest extends TestCase implements DemoDBConstants {
    
    protected BindingUtility bu = BindingUtility.getInstance();
    protected HashMap dontVersionSlotsMap = new HashMap();
    protected HashMap dontVersionContentSlotsMap = new HashMap();
    protected HashMap dontCommitSlotsMap = new HashMap();
    
    protected HashMap forceRemoveRequestSlotsMap = new HashMap();    

    protected static final boolean canUseEbxmlrrSpecHome =
	Boolean.valueOf(CommonProperties.getInstance().
			getProperty("can.use.ebxmlrr-spec",
				    "false")).booleanValue();
    protected static final String ebxmlrrSpecHome =
	CommonProperties.getInstance().
	getProperty("ebxmlrr-spec.home", "../ebxmlrr-spec");
    protected static final int testRepetitionsInner =
	Integer.parseInt(CommonProperties.getInstance().
			 getProperty("test.repetitions.inner", "1"));
    protected static final int testRepetitionsOuter =
	Integer.parseInt(CommonProperties.getInstance().
			 getProperty("test.repetitions.outer", "1"));
                
    protected static final String TMP_DIR = System.getProperty(
            "java.io.tmpdir");
    
    protected static final String OMAR_DEFAULT_NAMESPACE = "urn:freebxml:registry";
    protected static String defaultNamespacePrefix = CommonProperties.getInstance().getProperty("omar.common.URN.defaultNamespacePrefix");

    static {
        if (defaultNamespacePrefix == null) {
            defaultNamespacePrefix = OMAR_DEFAULT_NAMESPACE;
            CommonProperties.getInstance().put("omar.common.URN.defaultNamespacePrefix", defaultNamespacePrefix);
        }
    }

    /** Creates a new instance of OMARTest */
    public OMARTest(String name) {
        super(name);
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
        dontVersionContentSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
        dontCommitSlotsMap.put(BindingUtility.getInstance().CANONICAL_SLOT_LCM_DO_NOT_COMMIT, "true");
        forceRemoveRequestSlotsMap.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "true");
    }
    
    public static File createTempFile(boolean deleteOnExit) throws IOException {
        return createTempFile(deleteOnExit, "aString");
    }
    
    public static File createTempFile(boolean deleteOnExit, String content) throws IOException {
        // Create temp file.
        File temp = File.createTempFile("omar", ".txt");
        
        // Delete temp file when program exits.
        if (deleteOnExit) {
            temp.deleteOnExit();
        }
        
        // Write to temp file
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(content);
        out.close();
        
        return temp;
    }
    
    /**
     * Reads bytes from InputStream until the end of the stream.
     *
     * @param in The InputStream to be read.
     * @return the read bytes
     * @thows Exception (IOEXception...)
     */
    public byte[] readBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamReader inr = new InputStreamReader(in);
        byte bbuf[] = new byte[1024];
        int read;
        while ((read = in.read(bbuf)) > 0) {
            baos.write(bbuf, 0, read);
        }
        return baos.toByteArray();
    }
}
