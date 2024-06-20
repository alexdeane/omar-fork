/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ExtensibleObjectImplTest.java,v 1.7 2007/05/04 12:09:12 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 - 2006 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.Slot;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 * jUnit Test for ExtensibleObjectImpl.
 *
 * @author Diego Ballve / Republica Corp.
 */
public class ExtensibleObjectImplTest extends ClientTest {
    
    private final String slotName1 = "mySlotName1";
    private final String slotName2 = "mySlotName2";
    private final String slotName3 = "mySlotName3";
    private static Slot slot1 = null;
    private static Slot slot2 = null;
    private static Slot slot3 = null;
    private static ExtensibleObjectImplImpl extObj = null;
    
    public ExtensibleObjectImplTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ExtensibleObjectImplTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        Collection values = new ArrayList();
        values.add("value");
        slot1 = getLCM().createSlot(slotName1, values, "type");
        slot2 = getLCM().createSlot(slotName2, values, "type");
        slot3 = getLCM().createSlot(slotName3, values, "type");
        extObj = new ExtensibleObjectImplImpl((LifeCycleManagerImpl)getLCM());
    }
    
    /*
     * Tests the setObjectType() method to ensure that it 
     * throws InvalidRequestException when objectType is null.
     */
    public void testSetNullObjectType() throws Exception {
        ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl)lcm.createExtrinsicObject();
        try {
            eo.setObjectType(null);
        } catch (InvalidRequestException e) {
            //Expected
        } catch (NullPointerException e) {
            fail("Did not throw InvalidRequestException");
        }
    }
    
    
    
    /** Test of addSlot method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testAddSlot() throws Exception {
        System.out.println("testAddSlot");
        Slot slot;
        
        extObj.addSlot(slot1);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot1);

        extObj.addSlot(slot2);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot2);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Previously inserted test slot missing.", slot, slot1);
        
        try {
            extObj.addSlot(slot1);
            fail("Exception should have been thrown: Duplicate Slot name");
        } catch (JAXRException e) {
           System.out.println("Attempt to add Slot with duplicate name catched with message: " + e.getMessage());
        }
    }
    
    /** Test of getSlot method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testGetSlot() throws Exception {
        System.out.println("testGetSlot");
        Slot slot;

        extObj.addSlot(slot1);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot1);
    }

    /** Test of removeSlot method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testRemoveSlot() throws Exception {
        System.out.println("testRemoveSlot");
        Slot slot;
        
        extObj.addSlot(slot1);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot1);

        extObj.removeSlot(slot1.getName());
        slot = extObj.getSlot(slot1.getName());
        assertNull("getSlot should be null for Slot: " + slot1.getName(), slot);
        
        extObj.addSlot(slot1);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot1);
        extObj.addSlot(slot2);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot2);

        extObj.removeSlot(slot1.getName());
        slot = extObj.getSlot(slot1.getName());
        assertNull("getSlot should be null for Slot: " + slot1.getName(), slot);
        extObj.removeSlot(slot2.getName());
        slot = extObj.getSlot(slot1.getName());
        assertNull("getSlot should be null for Slot: " + slot1.getName(), slot);
        extObj.removeSlot(slot1.getName());
        slot = extObj.getSlot(slot1.getName());
        assertNull("getSlot should be null for Slot: " + slot1.getName(), slot);
        
        extObj.removeSlot("anotherName");
    }

    /** Test of addSlots method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testAddSlots() throws Exception {
        System.out.println("testAddSlots");
        Slot slot;
        Collection slots;

        slots = new ArrayList();
        slots.add(slot1);
        slots.add(slot2);
        
        extObj.addSlots(slots);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot1.getName(), slot, slot1);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot2.getName(), slot, slot2);
        slot = extObj.getSlot(slot3.getName());
        assertNull("getSlot should be null for Slot: " + slot3.getName(), slot);

        
        try {
            slots = new ArrayList();
            slots.add(slot1);
            slots.add(slot2);
            slots.add(slot3);
            extObj.addSlots(slots);
            fail("Exception should have been thrown: Duplicate Slot names");
        } catch (JAXRException e) {            
            System.out.println("Attempt to add Slot with duplicate name catched with message: " + e.getMessage());
            slot = extObj.getSlot(slot3.getName());
            assertNull("getSlot should be null for Slot: " + slot3.getName(), slot);
        }

        try {
            slots = new ArrayList();
            slots.add(slot3);
            slots.add(slot3);
            extObj.addSlots(slots);
            fail("Exception should have been thrown: Duplicate Slot names");
        } catch (JAXRException e) {
            System.out.println("Attempt to add Slot with duplicate name catched with message: " + e.getMessage());
        }
        
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot1.getName(), slot, slot1);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot2.getName(), slot, slot2);
        slot = extObj.getSlot(slot3.getName());
        assertEquals("getSlot should be nuul for Slot: " + slot3.getName(), slot, slot3);
    }
    
    /** Test of getSlots method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testGetSlots() throws Exception {
        System.out.println("testGetSlots");
        Slot slot;
        Collection slots, slots2;

        slots2 = extObj.getSlots();
        assertTrue("Slots should have been initially empty.", slots2.isEmpty());
        
        slots = new ArrayList();
        slots.add(slot1);
        slots.add(slot2);
        slots.add(slot3);
        
        extObj.addSlots(slots);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot1.getName(), slot, slot1);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot2.getName(), slot, slot2);
        slot = extObj.getSlot(slot3.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot3.getName(), slot, slot3);
        
        slots2 = extObj.getSlots();
        assertTrue("Slots' Collection instances must be different.", slots != slots2);
        assertEquals("Returned slots' Collection has different size.", slots.size(), slots2.size());
        Iterator it = slots.iterator();
        while (it.hasNext()) {
            slot = (Slot)it.next();
            assertTrue("Missing inserted Slot in ComposedObjects: " + slot.getName(), slots2.contains(slot));
        }        
    }

    /** Test of removeSlots method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testRemoveSlots() throws Exception {
        System.out.println("testRemoveSlots");
        Slot slot;
        Collection slotNames;

        extObj.addSlot(slot1);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot1);
        extObj.addSlot(slot2);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot2);
        extObj.addSlot(slot3);
        slot = extObj.getSlot(slot3.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot3);

        slotNames = new ArrayList();
        slotNames.add(slot1.getName());
        slotNames.add(slot2.getName());
        slotNames.add("anotherName");
        extObj.removeSlots(slotNames);
        slot = extObj.getSlot(slot1.getName());
        assertNull("getSlot should be null for Slot: " + slot1.getName(), slot);
        slot = extObj.getSlot(slot2.getName());
        assertNull("getSlot should be null for Slot: " + slot2.getName(), slot);
        slot = extObj.getSlot(slot3.getName());
        assertEquals("Retrieved Slot is not the same as inserted.", slot, slot3);
    }
    
    /** Test of getComposedObjects method, of class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. */
    public void testGetComposedObjects() throws Exception {
        System.out.println("testGetComposedObjects");
        Slot slot;
        Collection slots, compObjs;

        compObjs= extObj.getSlots();
        assertTrue("Composed Objects should have been initially empty.", compObjs.isEmpty());
        
        slots = new ArrayList();
        slots.add(slot1);
        slots.add(slot2);
        slots.add(slot3);
        
        extObj.addSlots(slots);
        slot = extObj.getSlot(slot1.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot1.getName(), slot, slot1);
        slot = extObj.getSlot(slot2.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot2.getName(), slot, slot2);
        slot = extObj.getSlot(slot3.getName());
        assertEquals("Failed to retrive inserted Slot: " + slot3.getName(), slot, slot3);
        
        compObjs = extObj.getSlots();
        assertEquals("ComposedObjects should contain the same objects as inserted Slots' Collection. Different size.", slots.size(), compObjs.size());
        Iterator it = slots.iterator();
        while (it.hasNext()) {
            slot = (Slot)it.next();
            assertTrue("Missing inserted Slot in ComposedObjects: " + slot.getName(), compObjs.contains(slot));
        }        
    }
    
    /** Test of slot being owned by only 1 ExtensibleObjectImpl. */
    public void testOneParentPerSlot() throws Exception {
        ExtensibleObject extObj2 = new ExtensibleObjectImplImpl((LifeCycleManagerImpl)getLCM());
        extObj.addSlot(slot1);
        try {
            extObj2.addSlot(slot1);        
            fail("Exception should have been thrown: Multiple parents per Slot");
        } catch (InvalidRequestException e) {
            System.out.println("Attempt to add Slot to 2nd parent catched with message: " + e.getMessage());
        }
        extObj.removeSlot(slot1.getName());
        try {
            extObj2.addSlot(slot1);        
        } catch (JAXRException e) {
            fail("Slot should have no parent after being removed.");
        }
    }
    
    
    /** Generated implementation of abstract class org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl. Please fill dummy bodies of generated methods. */
    private class ExtensibleObjectImplImpl extends ExtensibleObjectImpl {
        
        ExtensibleObjectImplImpl(LifeCycleManagerImpl lcm)  throws JAXRException {
            super(lcm);
        }
        
        ExtensibleObjectImplImpl(LifeCycleManagerImpl lcm, RegistryObjectType ebObject) throws JAXRException {
            super(lcm, ebObject);
        }
    }

    public void tearDown() throws Exception {
	super.tearDown();
        System.out.println();
    }
}
