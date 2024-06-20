/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/LocalizedStringImplTest.java,v 1.1 2006/05/01 15:15:32 doballve Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Locale;
import javax.xml.registry.LifeCycleManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test for LocalizedStringImpl.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class LocalizedStringImplTest extends ClientTest {
    
    public LocalizedStringImplTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
       
        TestSuite suite = new TestSuite(LocalizedStringImplTest.class);
        return suite;
    }
    
    /** Test creating and modifying LString (client side only) */
    public void testLocalizedStringModified() throws Exception {
        LifeCycleManager lcm = getLCM();
        
        Locale locale = Locale.CANADA_FRENCH;
        String value = "textValue";
        String charset = "utf-8";
        
        // create new string, assert modified
        LocalizedStringImpl lString = (LocalizedStringImpl)lcm.createLocalizedString(locale, value, charset);
        assertTrue(lString.isModified());
        
        // clean modified flag, try setting same value to each component, assert not modified
        lString.setModified(false);
        
        lString.setLocale(locale);
        assertEquals(locale, lString.getLocale());
        assertTrue(!lString.isModified());
        
        lString.setCharsetName(charset);
        assertEquals(charset, lString.getCharsetName());
        assertTrue(!lString.isModified());
        
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(!lString.isModified());

        // set new value for each component, assert modified
        locale = Locale.CANADA;
        value = "textValue2";
        charset = "iso-8859-1";
        
        lString.setLocale(locale);
        assertEquals(locale, lString.getLocale());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        lString.setCharsetName(charset);
        assertEquals(charset, lString.getCharsetName());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // try null values
        locale = null;
        value = null;
        charset = null;
        
        // null locale: Locale.getDefault() is used
        lString.setLocale(locale);
        assertEquals(Locale.getDefault(), lString.getLocale());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // null charset: LocalizedStringImpl.DEFAULT_CHARSET_NAME is used
        lString.setCharsetName(charset);
        assertEquals(LocalizedStringImpl.DEFAULT_CHARSET_NAME, lString.getCharsetName());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // null value: null is used
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(lString.isModified());
        lString.setModified(false);
    }
}
