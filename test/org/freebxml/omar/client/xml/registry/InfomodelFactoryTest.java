/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/InfomodelFactoryTest.java,v 1.4 2006/01/19 20:05:41 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.RegistryObject;
import junit.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.UUIDFactory;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * Test class for InfomodelFactory.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class InfomodelFactoryTest extends ClientTest {
    
    private static String EO_NICKNAME = "EoNick";    
    private static String ASS_NICKNAME = "AssNick";
    private static String EO_EXT_UUID = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
    private static String ASS_EXT_UUID = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
    private static String EO_TEST_UUID = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
    private static String ASS_TEST_UUID = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
    private static String EO_EXT_CLASSNAME = MyExtrinsicObject.class.getName();
    private static String ASS_EXT_CLASSNAME = MyAssociation.class.getName();

    static {
        // Configure extensions. Must be done here so that InfomodelFactory will
        // have these settings available when it is instantiated
        Properties props = ProviderProperties.getInstance().getProperties();
        props.put("jaxr-ebxml.extensionclass.association." + ASS_EXT_UUID, ASS_EXT_CLASSNAME + "," + ASS_NICKNAME);
        props.put("jaxr-ebxml.extensionclass.extrinsicobject." + EO_EXT_UUID, EO_EXT_CLASSNAME + "," + EO_NICKNAME);
    }
    
    public InfomodelFactoryTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(InfomodelFactoryTest.class);
        return suite;
    }

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("\nThrowable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * Test of getInstance method, of class org.freebxml.omar.client.xml.registry.InfomodelFactory.
     */
    public void testGetInstance() {
        System.out.println("\ntestGetInstance");
        InfomodelFactory extUtil = InfomodelFactory.getInstance();
        assertNotNull(extUtil);
        
        Constructor c1, c2, c3, c4;
        c1 = extUtil.getConstructor1Arg(EO_EXT_UUID);
        c2 = extUtil.getConstructor2Args(EO_EXT_UUID);
        c3 = extUtil.getConstructor1Arg(EO_NICKNAME);
        c4 = extUtil.getConstructor2Args(EO_NICKNAME);
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertNotNull(c4);
        assertEquals("Configured class differs.", EO_EXT_CLASSNAME, c1.getDeclaringClass().getName());
        assertEquals("Configured class differs.", EO_EXT_CLASSNAME, c2.getDeclaringClass().getName());
        assertEquals("Configured class differs.", EO_EXT_CLASSNAME, c3.getDeclaringClass().getName());
        assertEquals("Configured class differs.", EO_EXT_CLASSNAME, c4.getDeclaringClass().getName());

        c1 = extUtil.getConstructor1Arg(ASS_EXT_UUID);
        c2 = extUtil.getConstructor2Args(ASS_EXT_UUID);
        c3 = extUtil.getConstructor1Arg(ASS_NICKNAME);
        c4 = extUtil.getConstructor2Args(ASS_NICKNAME);
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertNotNull(c4);
        assertEquals("Configured class differs.", ASS_EXT_CLASSNAME, c1.getDeclaringClass().getName());
        assertEquals("Configured class differs.", ASS_EXT_CLASSNAME, c2.getDeclaringClass().getName());
        assertEquals("Configured class differs.", ASS_EXT_CLASSNAME, c3.getDeclaringClass().getName());
        assertEquals("Configured class differs.", ASS_EXT_CLASSNAME, c4.getDeclaringClass().getName());
    }
    
    /**
     * Test of createAssociation method, of class org.freebxml.omar.client.xml.registry.InfomodelFactory.
     */
    public void testCreateAssociation() throws Exception {
        System.out.println("\ntestCreateAssociation");
        
        InfomodelFactory extUtil = InfomodelFactory.getInstance();
        Object o;

        // Try null
        o = extUtil.createAssociation(lcm, (String)null);
        assertNotNull(o);
        assertTrue("Association extensions must extend AssociationImpl",
            o instanceof AssociationImpl);
        assertEquals("Should have been default class",
            AssociationImpl.class.getName(), o.getClass().getName());

        // Try inexistent
        o = extUtil.createAssociation(lcm, "unknownw-type");
        assertNotNull(o);
        assertTrue("Association extensions must extend AssociationImpl",
            o instanceof AssociationImpl);
        assertEquals("Should have been default class",
            AssociationImpl.class.getName(), o.getClass().getName());

        // Try configured
        o = extUtil.createAssociation(lcm, ASS_EXT_UUID);
        assertNotNull(o);
        assertTrue("Association extensions must extend AssociationImpl",
            o instanceof AssociationImpl);
        assertEquals("Should have been default class",
            ASS_EXT_CLASSNAME, o.getClass().getName());
    }
        
    /**
     * Test lcm.create, lcm.save and query of extended Association
     */
    public void testExtendedAssociationLCM() throws Exception {
        System.out.println("\ntestExtendedAssociationLCM");
        
        Object o;
        InfomodelFactory extUtil = InfomodelFactory.getInstance();

        Concept parentConcept = (Concept)bqm.getRegistryObject(
            BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association, "ClassificationNode");
        Concept myAssociationType = lcm.createConcept(parentConcept,
            "MyAssociation", "MyAssociation");
        myAssociationType.setKey(lcm.createKey(ASS_EXT_UUID));

        try {
            BulkResponse br;
            // Save concept and update it, to get the path
            br = lcm.saveObjects(Collections.singleton(myAssociationType));
            assertResponseSuccess(br);
            Query q = dqm.createQuery(QueryImpl.QUERY_TYPE_SQL,
                "select * from ClassificationNode where id = '" + ASS_EXT_UUID + "'");
            br = dqm.executeQuery(q);
            assertResponseSuccess(br);
            myAssociationType = (Concept)br.getCollection().iterator().next();
        
            // test lcm.createObject(Concept)
            o = lcm.createObject(myAssociationType);
            assertNotNull(o);
            assertTrue("Association extensions must extend AssociationImpl",
                o instanceof AssociationImpl);
            assertEquals("Should have been default class",
                ASS_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyAssociation",
                myAssociationType, ((Association)o).getAssociationType());

            // test lcm.createObject(String)
            o = lcm.createObject(ASS_NICKNAME);
            assertNotNull(o);
            assertTrue("Association extensions must extend AssociationImpl",
                o instanceof AssociationImpl);
            assertEquals("Should have been default class",
                ASS_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyAssociation",
                myAssociationType, ((Association)o).getAssociationType());

            // test lcm.createAssociation
            o = lcm.createAssociation(myAssociationType);
            assertNotNull(o);
            assertTrue("Association extensions must extend AssociationImpl",
                o instanceof AssociationImpl);
            assertEquals("Should have been default class",
                ASS_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyAssociation",
                myAssociationType, ((Association)o).getAssociationType());
            ((RegistryObject)o).setKey(lcm.createKey(ASS_TEST_UUID));

            // Association needs a source and a target
            RegistryObject target = lcm.createExtrinsicObject();
            target.setKey(lcm.createKey(EO_TEST_UUID));
            assertNotNull(target);
            ((AssociationImpl)o).setTargetObject(target);
            ((AssociationImpl)o).setSourceObject(parentConcept);

            ArrayList objects = new ArrayList();
            objects.add(o);
            objects.add(target);
            br = lcm.saveObjects(objects);
            assertResponseSuccess(br);
            q = dqm.createQuery(QueryImpl.QUERY_TYPE_SQL,
                "select * from Association where id = '" + ASS_TEST_UUID + "'");
            br = dqm.executeQuery(q);
            assertResponseSuccess(br);
            o = br.getCollection().iterator().next();
            assertEquals("Should have been default class",
                ASS_EXT_CLASSNAME, o.getClass().getName());
        } finally {
            // cleanup
            deleteIfExist(ASS_EXT_UUID, LifeCycleManager.ASSOCIATION);
            deleteIfExist(EO_TEST_UUID, LifeCycleManager.EXTRINSIC_OBJECT);
            deleteIfExist(ASS_TEST_UUID, LifeCycleManager.ASSOCIATION);
        }
    }
    
    /**
     * Test of createExtrinsicObject method, of class org.freebxml.omar.client.xml.registry.InfomodelFactory.
     */
    public void testCreateExtrinsicObject() throws Exception {
        System.out.println("\ntestExtrinsicObject");
        
        InfomodelFactory extUtil = InfomodelFactory.getInstance();
        Object o;

        // Try null
        o = extUtil.createExtrinsicObject(lcm, (String)null);
        assertNotNull(o);
        assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
            o instanceof ExtrinsicObjectImpl);
        assertEquals("Should have been default class",
            ExtrinsicObjectImpl.class.getName(), o.getClass().getName());

        // Try inexistent
        o = extUtil.createExtrinsicObject(lcm, "unknownw-type");
        assertNotNull(o);
        assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
            o instanceof ExtrinsicObjectImpl);
        assertEquals("Should have been default class",
            ExtrinsicObjectImpl.class.getName(), o.getClass().getName());

        // Try configured
        o = extUtil.createExtrinsicObject(lcm, EO_EXT_UUID);
        assertNotNull(o);
        assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
            o instanceof ExtrinsicObjectImpl);
        assertEquals("Should have been default class",
            EO_EXT_CLASSNAME, o.getClass().getName());
    }

    /**
     * Test lcm.create, lcm.save and query of extended ExtrinsicObject
     */
    public void testExtendedExtrinsicObjectLCM() throws Exception {
        System.out.println("\ntestExtendedExtrinsicObjectLCM");

        Object o;
        InfomodelFactory extUtil = InfomodelFactory.getInstance();

        Concept parentConcept = (Concept)bqm.getRegistryObject(
            BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject, "ClassificationNode");
        Concept myExtrinsicObjectType = lcm.createConcept(parentConcept,
            "MyExtrinsicObject", "MyExtrinsicObject");
        myExtrinsicObjectType.setKey(lcm.createKey(EO_EXT_UUID));

        try {
            BulkResponse br;
            // Save concept and update it, to get the path
            br = lcm.saveObjects(Collections.singleton(myExtrinsicObjectType));
            assertResponseSuccess(br);
            Query q = dqm.createQuery(QueryImpl.QUERY_TYPE_SQL,
                "select * from ClassificationNode where id = '" + EO_EXT_UUID + "'");
            br = dqm.executeQuery(q);
            assertResponseSuccess(br);
            myExtrinsicObjectType = (Concept)br.getCollection().iterator().next();

            // test lcm.createObject(Concept)
            o = lcm.createObject(myExtrinsicObjectType);
            assertNotNull(o);
            assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
                o instanceof ExtrinsicObjectImpl);
            assertEquals("Should have been default class",
                EO_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyExtrinsicObject",
                myExtrinsicObjectType, ((ExtrinsicObject)o).getObjectType());

            // test lcm.createObject(String)
            o = lcm.createObject(EO_NICKNAME);
            assertNotNull(o);
            assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
                o instanceof ExtrinsicObjectImpl);
            assertEquals("Should have been default class",
                EO_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyExtrinsicObject",
                myExtrinsicObjectType, ((ExtrinsicObject)o).getObjectType());

            // test lcm.createExtrinsicObject
            o = lcm.createExtrinsicObject(myExtrinsicObjectType);
            assertNotNull(o);
            assertTrue("ExtrinsicObject extensions must extend ExtrinsicObjectImpl",
                o instanceof ExtrinsicObjectImpl);
            assertEquals("Should have been default class",
                EO_EXT_CLASSNAME, o.getClass().getName());
            assertEquals("Should be of type MyExtrinsicObject",
                myExtrinsicObjectType, ((ExtrinsicObject)o).getObjectType());
            ((RegistryObject)o).setKey(lcm.createKey(EO_TEST_UUID));

            br = lcm.saveObjects(Collections.singleton(myExtrinsicObjectType));
            assertResponseSuccess(br);

            br = lcm.saveObjects(Collections.singleton(o));
            assertResponseSuccess(br);
            q = dqm.createQuery(QueryImpl.QUERY_TYPE_SQL,
                "select * from ExtrinsicObject where id = '" + EO_TEST_UUID + "'");
            br = dqm.executeQuery(q);
            assertResponseSuccess(br);
            o = br.getCollection().iterator().next();
            assertEquals("Should have been default class",
                EO_EXT_CLASSNAME, o.getClass().getName());
        } finally {
            // cleanup
            deleteIfExist(EO_EXT_UUID, LifeCycleManager.EXTRINSIC_OBJECT);
            deleteIfExist(EO_TEST_UUID, LifeCycleManager.EXTRINSIC_OBJECT);
        }
    }
    
}
class MyExtrinsicObject extends ExtrinsicObjectImpl {
    public MyExtrinsicObject(LifeCycleManagerImpl lcm)
    throws JAXRException {
        super(lcm);
    }
    
    public MyExtrinsicObject(LifeCycleManagerImpl lcm,
    ExtrinsicObjectType ebExtrinsicObj) throws JAXRException {
        super(lcm, ebExtrinsicObj);
    }
}
class MyAssociation extends AssociationImpl {
    public MyAssociation(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public MyAssociation(LifeCycleManagerImpl lcm, AssociationType1 ebAss)
        throws JAXRException {
        super(lcm, ebAss);
    }    
}