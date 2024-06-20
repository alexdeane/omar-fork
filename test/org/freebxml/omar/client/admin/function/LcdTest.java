/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/admin/function/LcdTest.java,v 1.1 2004/10/01 16:21:46 tonygraham Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.admin.function;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import javax.swing.filechooser.FileSystemView;

import junit.framework.*;

import org.freebxml.omar.client.admin.AdminFunctionContext;

/**
 * A JUnit TestCase to test the admin tool 'lcd' command
 *
 * @author Tony Graham
 */
public class LcdTest extends AbstractFunctionTest {
    
    public LcdTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        return new TestSuite(LcdTest.class);
    }

    // test methods 
    
    //
    public void testExecute_NoArg() throws Exception {
	String testString = null;

	Lcd lcd = new Lcd();

	lcd.execute(context,
		    testString);

        String expectedLocalDir =
	    FileSystemView.getFileSystemView().getDefaultDirectory().getCanonicalPath();

        assertEquals("Expected user's home directory",
		     expectedLocalDir,
		     context.getLocalDir().getCanonicalPath());
    }

    //
    public void testExecute_DirArg() throws Exception {
	String testString =
	    FileSystemView.getFileSystemView().getDefaultDirectory().getCanonicalPath();

	Lcd lcd = new Lcd();

	lcd.execute(context,
		    testString);

        String expectedLocalDir = testString;

        assertEquals("Expected user's home directory",
		     expectedLocalDir,
		     context.getLocalDir().getCanonicalPath());
    }

    //
    public void testExecute_FileArg() throws Exception {
	File tmpFile = File.createTempFile("LcdTest", null);

	tmpFile.deleteOnExit();

	String testString =
	    tmpFile.getCanonicalPath();

	Lcd lcd = new Lcd();

	boolean success;
	try {
	    lcd.execute(context,
			testString);
	    success = true;
	} catch (Exception e) {
	    success = false;
	    e.printStackTrace();
	}

        String expectedLocalDir = testString;

	assertFalse("Lcd to file should fail.",
		    success);
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
