/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/InternationalStringImplTest.java,v 1.7 2007/05/04 18:30:12 farrukh_najmi Exp $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;

/**
 * jUnit Test for InternationlStringImpl.
 *
 * @author Diego Ballve / Republica Corp.
 */
public class InternationalStringImplTest extends ClientTest {
    
    public InternationalStringImplTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    public static Test suite() throws Exception {
       
        TestSuite suite = new TestSuite(InternationalStringImplTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new InternationalStringImplTest("testClone"));
        return suite;
    }

    /**
     * Tests for bug where InternationalStringImpl.clone was modifying
     * source InternationalString due to shared copy of LocalizedStrings
     * being added to clone and marking them as modified.
     *
     * Fix was to add a clone method to LocalizedStringImpl and use it in 
     * clone method of InternationalStringImpl.
     */
    public void testClone() throws Exception {
        InternationalStringImpl is1 = (InternationalStringImpl)lcm.createInternationalString("Test String");
        is1.setModified(false);
        InternationalStringImpl is2 = (InternationalStringImpl)is1.clone();
        LocalizedString ls = (LocalizedString) is2.getLocalizedStrings().toArray()[0];
        ls.setValue("New name");
        
        assertTrue("InternationalStringImpl.clone incorrectly modified source.", (!is1.isModified()));
    }
    
    public void testEmptyCharsetFromBindingObject() throws Exception {
        System.out.println("\ntestEmptyCharsetFromBindingObject");
        InternationalStringImpl iString = (InternationalStringImpl)getLCM()
            .createInternationalString();

        Locale pt_BR = new Locale("pt", "BR");
        org.freebxml.omar.common.BindingUtility bu =
                org.freebxml.omar.common.BindingUtility.getInstance();
        org.oasis.ebxml.registry.bindings.rim.LocalizedString ebLS =
                bu.rimFac.createLocalizedString();
        ebLS.setCharset("");
        ebLS.setLang("pt-BR");
        ebLS.setValue("pt_BR");
        LocalizedStringImpl lString = new LocalizedStringImpl((LifeCycleManagerImpl)getLCM(), ebLS);
        iString.addLocalizedString(lString);
        assertEquals(pt_BR.toString(), iString.getValue(pt_BR));
        assertEquals(pt_BR.toString(), iString.getClosestValue(pt_BR));
    }
    
    /** Test of addSlot method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testGetClosestValue() throws Exception {
        System.out.println("\ntestGetClosestValue");

        Locale DEFAULT_LOCALE = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("pt","BR"));

            Locale loc_empty = new Locale("");
            Locale loc_en = new Locale("en");
            Locale loc_en_US = new Locale("en", "US");
            Locale loc_en_UK = new Locale("en", "UK");
            Locale loc_en_UK_var = new Locale("en", "UK", "var");

            Locale loc_pt = new Locale("pt");
            Locale loc_pt_BR = new Locale("pt", "BR");
            Locale loc_pt_PT = new Locale("pt", "PT");

            Locale loc_fi = new Locale("fi");
            Locale loc_fi_FI = new Locale("fi", "FI");
            Locale loc_fi_FI_var = new Locale("fi", "FI", "var");

            InternationalStringImpl iString = (InternationalStringImpl)getLCM()
                .createInternationalString(loc_en_US, loc_en_US.toString());
//????            assertNull("Empty IString value be null.", iString.getClosestValue(Locale.getDefault()));

//            iString.addLocalizedString(getLCM().createLocalizedString(loc_en, loc_en.toString()));
            assertEquals(loc_en_US.toString(), iString.getClosestValue(loc_pt));

            iString.addLocalizedString(getLCM().createLocalizedString(loc_pt, loc_pt.toString()));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt_PT));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt_BR));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_fi));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_fi_FI));

            iString.addLocalizedString(getLCM().createLocalizedString(loc_pt_BR, loc_pt_BR.toString()));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt_PT));
            assertEquals(loc_pt_BR.toString(), iString.getClosestValue(loc_pt_BR));
            assertEquals(loc_pt_BR.toString(), iString.getClosestValue(loc_fi));
            assertEquals(loc_pt_BR.toString(), iString.getClosestValue(loc_fi_FI));

            iString.addLocalizedString(getLCM().createLocalizedString(loc_fi, loc_fi.toString()));
            iString.addLocalizedString(getLCM().createLocalizedString(loc_fi_FI, loc_fi_FI.toString()));
            iString.addLocalizedString(getLCM().createLocalizedString(loc_fi_FI_var, loc_fi_FI_var.toString()));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt));
            assertEquals(loc_pt.toString(), iString.getClosestValue(loc_pt_PT));
            assertEquals(loc_pt_BR.toString(), iString.getClosestValue(loc_pt_BR));
            assertEquals(loc_fi.toString(), iString.getClosestValue(loc_fi));
            assertEquals(loc_fi_FI.toString(), iString.getClosestValue(loc_fi_FI));
            assertEquals(loc_fi_FI_var.toString(), iString.getClosestValue(loc_fi_FI_var));            
        } catch (Exception e) {
            throw e;
        } finally {
            Locale.setDefault(DEFAULT_LOCALE);
        }
    }
    
    /** Test to check UTF-8 */
    public void testGetUTF8Supported() throws Exception {
        System.out.println("\ntestGetUTF8Supported");

      String name="New�";

      LifeCycleManager lcm = getLCM();
      RegistryPackage rp1 = lcm.createRegistryPackage(name);
      String id = rp1.getKey().getId();

      ArrayList objectsToSave = new ArrayList();
      objectsToSave.add(rp1);
      lcm.saveObjects(objectsToSave);

      DeclarativeQueryManager dqm = getDQM();
      RegistryPackage rp2 = (RegistryPackage)dqm.getRegistryObject(id, lcm.REGISTRY_PACKAGE);
      assertNotNull(rp2);

      InternationalString is = rp2.getName();
      String newName = is.getValue();

      assertEquals("Check that db supports UTF-8 as default charset", name, newName);

    }
    
    /** Test methods addLocalizedString, removeLocalizedString, addLocalizedStrings
     *  and removeLocalizedStrings. */
    public void testAddRemoveLocalizedString() throws Exception {
        System.out.println("\ntestAddRemoveLocalizedString");
        
        Locale locale1 = Locale.CANADA;
        String value1 = "value";
        Locale locale2 = Locale.CANADA_FRENCH;
        String value2 = "value2";
        Locale locale3 = Locale.CHINA;
        String value3 = "value3";
        
        // create test iString
        InternationalStringImpl iString = (InternationalStringImpl)getLCM().
                createInternationalString();

        // Verify default values, assert modified
        assertEquals(0, iString.getLocalizedStrings().size());
        assertTrue(iString.isModified());
        iString.setModified(false);
        
        LocalizedStringImpl lString1 = (LocalizedStringImpl)getLCM().
                createLocalizedString(locale1, value1);
        LocalizedStringImpl lString2 = (LocalizedStringImpl)getLCM().
                createLocalizedString(locale2, value2);
        LocalizedStringImpl lString3 = (LocalizedStringImpl)getLCM().
                createLocalizedString(locale3, value3);
        
        List lStrings = new ArrayList();
        lStrings.add(lString1);
        lStrings.add(lString2);

        // test addLocalizedStrings(Collection), verify values, assert modified
        iString.addLocalizedStrings(lStrings);        
        assertEquals(2, iString.getLocalizedStrings().size());
        assertLocalizedStringsEqual(lStrings, new ArrayList(iString.getLocalizedStrings()));
        assertTrue(iString.isModified());
        iString.setModified(false);

        // test remove inexistent LString, assert not modified
        iString.removeLocalizedString(lString3);        
        assertEquals(2, iString.getLocalizedStrings().size());
        assertLocalizedStringsEqual(lStrings, new ArrayList(iString.getLocalizedStrings()));
        assertTrue(!iString.isModified());
        
        // test addLocalizedString(LString), verify values, assert modified
        iString.addLocalizedString(lString3);
        lStrings.add(lString3);
        assertEquals(3, iString.getLocalizedStrings().size());
        assertLocalizedStringsEqual(lStrings, new ArrayList(iString.getLocalizedStrings()));
        assertTrue(iString.isModified());
        iString.setModified(false);
        
        // test removeLocalizedString(LString), verify values, assert modified
        iString.removeLocalizedString(lString2);
        lStrings.remove(lString2);
        assertEquals(2, iString.getLocalizedStrings().size());
        assertLocalizedStringsEqual(lStrings, new ArrayList(iString.getLocalizedStrings()));
        assertTrue(iString.isModified());
        iString.setModified(false);
        
        // test removeLocalizedStrings(LString), verify values, assert modified
        lStrings.clear();
        lStrings.add(lString1);
        lStrings.add(lString2);
        iString.removeLocalizedStrings(lStrings);
        lStrings.clear();
        lStrings.add(lString3);
        assertEquals(1, iString.getLocalizedStrings().size());
        assertLocalizedStringsEqual(lStrings, new ArrayList(iString.getLocalizedStrings()));
        assertTrue(iString.isModified());
        iString.setModified(false);
    }
    
    private void assertLocalizedStringsEqual(List expected, List actual) throws Exception {
        assertEquals(expected.size(), actual.size());
        
        Collections.sort(expected, lStringComparator);
        Collections.sort(actual, lStringComparator);

        for (int i = 0; i < expected.size(); i++) {
            LocalizedStringImpl lExpected = (LocalizedStringImpl)expected.get(i);
            LocalizedStringImpl lActual = (LocalizedStringImpl)expected.get(i);
            
            assertEquals(lExpected.getLocale(), lActual.getLocale());
            assertEquals(lExpected.getValue(), lActual.getValue());
            assertEquals(lExpected.getCharsetName(), lActual.getCharsetName());
        }
    }

    LStringLocaleComparator lStringComparator = new LStringLocaleComparator();
    private class LStringLocaleComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            LocalizedString lString1 = (LocalizedString)o1;
            LocalizedString lString2 = (LocalizedString)o2;
            try {
                return lString1.getLocale().toString().compareToIgnoreCase(
                        lString2.getLocale().toString());
            } catch (JAXRException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
        
    /** Test setValue method */
    public void testSetValue() throws Exception {
        System.out.println("\ntestSetValue");

        Locale locale = Locale.getDefault();
        String value = "value";

        // create test iString
        InternationalStringImpl iString = (InternationalStringImpl)getLCM()
            .createInternationalString(value);

        // Verify default values, assert modified
        assertEquals(1, iString.getLocalizedStrings().size());
        LocalizedStringImpl lString = (LocalizedStringImpl)iString.
                getLocalizedStrings().iterator().next();
        assertEquals(locale, lString.getLocale());
        assertEquals(value, lString.getValue());
        assertEquals(LocalizedStringImpl.DEFAULT_CHARSET_NAME, lString.getCharsetName());
        assertTrue(iString.isModified());
        iString.setModified(false);

        // set value to null, assert modified, lString removed
        iString.setValue(null);
        assertEquals(0, iString.getLocalizedStrings().size());
        assertTrue(iString.isModified());
        iString.setModified(false);
        
        locale = new Locale("en", "US");
        value = locale.toString();
        
        // set new value using locale, verify values, assert modified
        iString.setValue(locale, value);
        assertEquals(1, iString.getLocalizedStrings().size());
        lString = (LocalizedStringImpl)iString.
                getLocalizedStrings().iterator().next();
        assertEquals(locale, lString.getLocale());
        assertEquals(value, lString.getValue());
        assertEquals(LocalizedStringImpl.DEFAULT_CHARSET_NAME, lString.getCharsetName());
        assertTrue(iString.isModified());
        iString.setModified(false);
        
        // set value to null using locale, assert modified, lString removed
        iString.setValue(locale, null);
        assertEquals(0, iString.getLocalizedStrings().size());
        assertTrue(iString.isModified());
        iString.setModified(false);
    }
    
    /** 
     * Tests removeLocalizedStrings method of InternationalString.
     * Test for 6434516: InternationalString.removeLocalizedStrings(Collection) only remove the first LocalizedString
     *
     * Contributed by Dianne Jiao.
     * 
     */
    public void testRemoveLocalizedStrings() throws Exception {
        System.out.println("testRemoveLocalizedStrings");
        String name = "testRemoveLocalizedStrings";

        // set the value for the default locale.
        String value [] = {"US","UK","Canada","French"};
        Locale l[] = { Locale.US, Locale.UK, Locale.CANADA, Locale.FRENCH};

        InternationalString is = getLCM().createInternationalString();
        Collection cls = new ArrayList();
        for ( int i = 0; i < value.length; i++) {
            LocalizedString ls = getLCM().createLocalizedString(l[i],value[i]);
            is.addLocalizedString(ls);
            cls.add(ls);
        }

        //System.out.println("Created and added LocalizedStrings to InternationalString\n");
        //System.out.println("Verify that they were added \n");

        for ( int i=0; i < value.length; i++) {
            System.out.println("Locale: " + l[i] + " maps to " + value[i] + "\n");
            System.out.println("is.getValue(" + i + ") = " + is.getValue(l[i]) + "\n");
            assertEquals(is.getValue(l[i]), value[i]);
        }
        
        //System.out.println("Remove the localized strings\n");
        is.removeLocalizedStrings(cls);

        //System.out.println("Verify the removal\n");
        assertEquals("removeLocalizedStrings did not remove them.", 0, is.getLocalizedStrings().size());
    }     
}
