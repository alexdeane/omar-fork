/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/ObjectCacheTest.java,v 1.4 2006/07/29 05:53:38 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;

/**
 * Unit tests for ObjectCache.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class ObjectCacheTest extends ClientTest {

    public ObjectCacheTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ObjectCacheTest.class);
        return suite;
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

    /**
     * Test of isCached method, of class org.freebxml.omar.client.xml.registry.ObjectCache.
     */
    public void testIsCached() {
        System.out.println("/ntestIsCached");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();
        assertTrue(!cache.isCached("non-existing-id"));
    }

    /**
     * Test of putRegistryObject method, of class org.freebxml.omar.client.xml.registry.ObjectCache.
     */
    public void testPutRegistryObject() throws Exception {
        System.out.println("/ntestPutRegistryObject");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        assertTrue(cache.isCached(testObject.getKey().getId()));
    }

    /**
     * Test of getReference method, of class org.freebxml.omar.client.xml.registry.ObjectCache.
     */
    public void testGetReference() throws Exception {
        System.out.println("/ntestGetReference");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a new RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        String id = testObject.getKey().getId();
        String type = "RegistryPackage";
        assertNotNull(cache.getReference(id, type));

        // Use Contains Association Concept as test material
        id = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Contains;
        type = "ClassificationNode";
        assertTrue("Test object already cached", !cache.isCached(id));
        assertNotNull(cache.getReference(id, type));
        assertTrue(cache.isCached(id));

        // Try non-exixstent object
        try {
            cache.getReference("non-existing-id", type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }

    /**
     * Test of getRegistryObject method, of class org.freebxml.omar.client.xml.registry.ObjectCache.
     */
    public void testGetRegistryObject() throws Exception {
        System.out.println("/ntestGetRegistryObject");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a new RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        String id = testObject.getKey().getId();
        String type = "RegistryPackage";
        RegistryObject cachedObject = cache.getRegistryObject(id, type);
        assertNotNull(cachedObject);
        assertEquals(testObject, cachedObject);

        // Use Extends Association Concept as test material
        id = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Extends;
        type = "ClassificationNode";
        assertTrue("Test object already cached", !cache.isCached(id));
        cachedObject = cache.getRegistryObject(id, type);
        assertNotNull(cachedObject);
        assertEquals(id, cachedObject.getKey().getId());

        // Try non-exixstent object
        try {
            cachedObject = cache.getRegistryObject("non-existing-id", type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }

    /**
     * Test of getRegistryObjects method, of class org.freebxml.omar.client.xml.registry.ObjectCache.
     */
    public void testGetRegistryObjects() throws Exception {
        System.out.println("/ntestGetRegistryObjects");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // Use Contains, Extends and Uses -Association Concepts as test material
        String id1 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Contains;
        String id2 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Extends;
        String id3 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Uses;
        Collection ids = new ArrayList();
        ids.add(id1);
        ids.add(id2);
        ids.add(id3);
        String type = "ClassificationNode";

        Collection cachedObjects = cache.getRegistryObjects(new ArrayList(ids), type);
        assertNotNull(cachedObjects);
        assertEquals(ids.size(), cachedObjects.size());
        for(Iterator iter = cachedObjects.iterator(); iter.hasNext(); ) {
            RegistryObject object = (RegistryObject)iter.next();
            ids.remove(object.getKey().getId());
        }
        assertTrue("Returned RegistryObjects did not match queried objects.", ids.isEmpty());

        // Try non-exixstent object
        try {
            ids = new ArrayList();
            ids.add(id1);
            ids.add("non-existing-id");
            cachedObjects = cache.getRegistryObjects(ids, type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }
}
