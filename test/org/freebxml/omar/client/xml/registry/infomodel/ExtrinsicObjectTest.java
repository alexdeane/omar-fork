/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ExtrinsicObjectTest.java,v 1.19 2007/04/18 19:10:11 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;

import org.freebxml.omar.common.exceptions.RepositoryItemNotFoundException;

/**
 * jUnit Test for ExtrinsicObject
 *
 * These tests must be run in specific order.
 * Currently they run in the order the test methods appear in code.
 * Do not change this order.
 *
 * @author Nikita Sawant
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtrinsicObjectTest extends ClientTest {
    
    String eoWithoutRIId = "urn:freebxml:registry:test:ExtrinsicObjectTest::EOWithoutRI";
    
    String eoWithRIId = "urn:freebxml:registry:test:ExtrinsicObjectTest::EOWithRI";
    String specLinkId = "urn:freebxml:registry:test:ExtrinsicObjectTest::SpecLink";
    String serviceId = "urn:freebxml:registry:test:ExtrinsicObjectTest::Service";
    String serviceBindingId = "urn:freebxml:registry:test:ExtrinsicObjectTest::ServiceBinding";
    
    public ExtrinsicObjectTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        
        // ExtrinsicObject without RepositoryItem
        suite.addTest(new ExtrinsicObjectTest("testSetObjectTypeRef"));        
        suite.addTest(new ExtrinsicObjectTest("testSetObjectType"));
        
        suite.addTest(new ExtrinsicObjectTest("testCreateExtrinsicObjectWithoutRI"));
        suite.addTest(new ExtrinsicObjectTest("testUpdateExtrinsicObjectWithoutRI"));
        suite.addTest(new ExtrinsicObjectTest("testDeleteExtrinsicObjectWithoutRI"));
        
        // ExtrinsicObject with RepositoryItem
        suite.addTest(new ExtrinsicObjectTest("testPublishRepositoryItem"));
        suite.addTest(new ExtrinsicObjectTest("testExtrinsicObjectTestQuery"));
        suite.addTest(new ExtrinsicObjectTest("testUpdateWithNoRepositoryItem"));
        suite.addTest(new ExtrinsicObjectTest("testRemoveRepositoryItem"));
        suite.addTest(new ExtrinsicObjectTest("testUpdateAfterRepositoryItemRemoval"));
        suite.addTest(new ExtrinsicObjectTest("testDeleteExtrinsicObject"));
        
        return suite;
    }
    
    /**
     * Tests that setObjectTypeRef with an invalid concept id throws an InvalidRequestException.
     * Invalid concept id is any id string that is not the id of a 
     * Concept that is a descendant of ObjectType ClassificationScheme.
     *
     * This Exception is caught on the server side.
     */
    public void testSetObjectTypeRef() throws Exception {
        
        //See 
        ExtrinsicObject eo = (ExtrinsicObject)lcm.createExtrinsicObject((DataHandler)null);
        RegistryObjectRef objectTypeRef = new RegistryObjectRef(lcm, bu.CANONICAL_ASSOCIATION_TYPE_ID_Uses);
        ((ExtrinsicObjectImpl)eo).setObjectTypeRef(objectTypeRef);
        try {
            
            Collection objectsToSave = new ArrayList();
            objectsToSave.add(eo);

            BulkResponse br = lcm.saveObjects(objectsToSave, dontVersionSlotsMap);
            fail("MUST not allow an id that is not an id of a ObjectType Concept to be used as objectType");
        } catch (RegistryException e) {
            //Expected
        }
        
    }
    
    /**
     * Tests that setObjectType with an invalid concept throws an InvalidRequestException.
     * Invalid concept is any Concept that is not a descendant of ObjectType ClassificationScheme.
     *
     */
    public void testSetObjectType() throws Exception {
        
        //See 
        ExtrinsicObject eo = (ExtrinsicObject)lcm.createExtrinsicObject((DataHandler)null);
        try {
            Concept nonAnObjectTypeConcept = (Concept)bqm.getRegistryObject(
                    bu.CANONICAL_ASSOCIATION_TYPE_ID_Uses, LifeCycleManager.CONCEPT);
            ((ExtrinsicObjectImpl)eo).setObjectType(nonAnObjectTypeConcept);
            fail("MUST not allow non ObjectType Concept to be used as objectType");
        } catch (InvalidRequestException e) {
            //Expected
        }
        
    }
    
    /** Publish WSDL ExtrinsicObject **/
    public void testPublishRepositoryItem() throws Exception {
        // pre test clean-up
        deleteIfExist(eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        deleteIfExist(specLinkId, LifeCycleManager.SPECIFICATION_LINK);
        deleteIfExist(serviceId, LifeCycleManager.SERVICE);
        deleteIfExist(serviceBindingId, LifeCycleManager.SERVICE_BINDING);
        
        // Create services and service
        Collection services = new ArrayList();
        Service service = lcm.createService("hello.service.name");
        service.setKey(lcm.createKey(serviceId));
        ((RegistryObjectImpl)service).setLid(serviceId);
        
        // Create service bindings
        Collection serviceBindings = new ArrayList();
        ServiceBinding binding = lcm.createServiceBinding();
        binding.setKey(lcm.createKey(serviceBindingId));
        ((RegistryObjectImpl)binding).setLid(serviceBindingId);
        binding.setName(lcm.createInternationalString("hello.svcbnd.description"));
        binding.setAccessURI("hello.svcbnd.uri");
        
        // Create specification link
        SpecificationLink specLink = lcm.createSpecificationLink();
        specLink.setKey(lcm.createKey(specLinkId));
        ((RegistryObjectImpl)specLink).setLid(specLinkId);
        specLink.setName(lcm.createInternationalString("hello.speclink.name"));
        
        // Get the RepositoryItem (WSDL file)
        // Warning: using other test's resource.
        String riResourceName = "/resources/StandaloneTest.wsdl";
        URL riResourceUrl = getClass().getResource(riResourceName);
        assertNotNull("Missing test resource: " + riResourceName, riResourceUrl);
        File repositoryItemFile = new File(riResourceUrl.getFile());
        assertTrue("Missing test resource: " + riResourceUrl.getFile(), repositoryItemFile.canRead());
        DataHandler repositoryItem = new DataHandler(new FileDataSource(repositoryItemFile));
        
        // Create Extrinsic Object
        ExtrinsicObject eo =
                lcm.createExtrinsicObject(repositoryItem);
        eo.setKey(lcm.createKey(eoWithRIId));
        ((RegistryObjectImpl)eo).setLid(eoWithRIId);
        eo.setName(lcm.createInternationalString(
                "ExtrinsicObjectTest - EO with RI"));
        eo.setMimeType("text/xml");

        //Expected size must be updated if ./test/resources/StandaloneTest.wsdl is ever changed
        long riSize = ((ExtrinsicObjectImpl)eo).getRepositoryItemSize();
        assertEquals("getRepositoryItemSize did not return expected size", riSize, (long)1808);
        
        //Make sure we can call method twice as it reads InputStream
        riSize = ((ExtrinsicObjectImpl)eo).getRepositoryItemSize();
        assertEquals("getRepositoryItemSize did not return expected size", riSize, (long)1808);
        
        Concept objectTypeConcept = (Concept)bqm.getRegistryObject(
                //The WSDL objectType is not being used as it create MissingRepositoryItemException
                //in WSDL Cataloger when later tests delete repositoryitem
                //"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL",
                "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:XML:Schematron",
                LifeCycleManager.CONCEPT);
        
        /* Cast to implementation-specific class; setObjectType
         * is not in JAXR 1.0
         */
        ((ExtrinsicObjectImpl)eo).setObjectType(objectTypeConcept);
        
        specLink.setSpecificationObject(eo);
        
        binding.addSpecificationLink(specLink);
        serviceBindings.add(binding);
        
        // Add service bindings to service
        service.addServiceBindings(serviceBindings);
        
        // Add service to services
        services.add(service);
        
        Collection objectsToSave = new ArrayList();
        objectsToSave.addAll(services);
        objectsToSave.addAll(serviceBindings);
        objectsToSave.add(specLink);
        objectsToSave.add(eo);
        
        BulkResponse br = lcm.saveObjects(objectsToSave, dontVersionSlotsMap);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        Collection exceptions = br.getExceptions();
    }
            
    /** Test query of a ExtrinsicObject **/
    public void testExtrinsicObjectTestQuery() throws Exception {
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNotNull(eo);
        
        DataHandler handler = eo.getRepositoryItem();
        assertNotNull("ExtrinsicObject must not have null for RepositoryItem", handler);
        
        // -- Create a temp file and write the data to it
        String filename = System.getProperty("java.io.tmpdir") + "/WSDL_TestFile";
        if ( handler != null ) {
            handler.writeTo( new java.io.FileOutputStream( new java.io.File(filename) ) );
        }
    }
    
    /**
     * Now update ExtrinsicObject but without a repository item. The repository item should not
     * be touched (it shall not be deleted!)
     */
    public void testUpdateWithNoRepositoryItem() throws Exception {
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNotNull(eo);
        assertNotNull(eo.getRepositoryItem());
        byte expContent[] = readBytes(eo.getRepositoryItem().getInputStream());
        
        eo.setRepositoryItem(null);
        
        ArrayList objects = new ArrayList();
        objects.add(eo);
        
        BulkResponse br = lcm.saveObjects(objects);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        
        //Now verify that the repository has not been changed
        //TODO: test content version too
        eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNotNull(eo);
        DataHandler handler = eo.getRepositoryItem();
        assertNotNull("Error. Missing RepositoryItem.", handler);
        byte actContent[] = readBytes(handler.getInputStream());
        assertTrue("Content has changed!", Arrays.equals(expContent, actContent));
    }
    
    /**
     * Now romove the repository item.
     */
    public void testRemoveRepositoryItem() throws Exception {
        //TODO: Remove impl class cast when method gets added to JAXR EO
        ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNotNull(eo);
        eo.removeRepositoryItem();
        
        /*//TODO: The removal should happen on save only
        ArrayList objects = new ArrayList();
        objects.add(eo);
         
        BulkResponse br = lcm.saveObjects(objects);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
         */
        
        //Now verify that the repository item is not there when object is re-retrieved.
        try {
            eo = (ExtrinsicObjectImpl) getBQM().getRegistryObject(
                    eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
            assertNotNull("RepositoryItem not deleted.", eo);
            DataHandler handler = eo.getRepositoryItem();
            assertNull("RepositoryItem not deleted.", handler);
            System.out.println("No exceptions in testRemoveRepositoryItem()");
        } catch (RepositoryItemNotFoundException e) {
            // Do nothing, we expect this exception.
        } catch (JAXRException e) {
            fail("Unexpected exception:\n" + e.getMessage());
        }
    }
    
    /**
     * Now update ExtrinsicObject that (now) has no Repository Item.  The
     * update should operate without an exception and a Repository Item
     * should not be added.
     */
    public void testUpdateAfterRepositoryItemRemoval() throws Exception {
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNotNull(eo);
        eo.setDescription(lcm.createInternationalString(
                "ExtrinsicObjectTest - EO with RI - Description"));
        
        ArrayList objects = new ArrayList();
        objects.add(eo);
        
        BulkResponse br = lcm.saveObjects(objects);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        
        //Now verify that the repository item is not there when object is re-retrieved.
        eo = (ExtrinsicObjectImpl) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        try {
            DataHandler handler = eo.getRepositoryItem();
            assertNull("RepositoryItem not deleted.", handler);
            System.out.println(
                    "No exceptions in testUpdateAfterRepositoryItemRemoved()");
        } catch (RepositoryItemNotFoundException e) {
            // Do nothing, we expect this exception.
        } catch (JAXRException e) {
            fail("Unexpected exception:\n" + e.getMessage());
        }
    }
    
    /** Test delete of Extrinsic Object **/
    public void testDeleteExtrinsicObject() throws Exception {
        
        //Delete the service that was created in testSubmit
        ArrayList keys = new ArrayList();
        keys.add(lcm.createKey(eoWithRIId));
        BulkResponse resp = lcm.deleteObjects(keys, null, forceRemoveRequestSlotsMap, (String)null);
        JAXRUtility.checkBulkResponse(resp);
        assertEquals(BulkResponse.STATUS_SUCCESS, resp.getStatus());
        
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNull("ExtrinsicObject not deleted correctly", eo);
    }
    
    // ExtrinsicObject without RepositoryItem ------------------------------- //
    
    /** Now, do much of the above without ever including a Repository Item */
    public void testCreateExtrinsicObjectWithoutRI() throws Exception {
        // pre test clean-up
        deleteIfExist(eoWithoutRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        
        ExtrinsicObject eo = (ExtrinsicObject)lcm.createExtrinsicObject((DataHandler)null);
        
        // Reuse old variable for new object.
        eo.setKey(lcm.createKey(eoWithoutRIId));
        ((RegistryObjectImpl)eo).setLid(eoWithoutRIId);
        eo.setName(lcm.createInternationalString("ExtrinsicObjectTest - EO withou RI"));
        eo.setExpiration(new java.util.Date((new java.util.GregorianCalendar(2061, 0, 10)).getTimeInMillis()));
        eo.setStability(RegistryEntry.STABILITY_STATIC);
        
        ArrayList objects = new ArrayList();
        objects.add(eo);
        
        BulkResponse br = lcm.saveObjects(objects, dontVersionSlotsMap);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
    }
    
    /** Now update ExtrinsicObject with a description */
    public void testUpdateExtrinsicObjectWithoutRI() throws Exception {
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithoutRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        InternationalString desc = lcm.createInternationalString(
                "ExtrinsicObjectTest - EO withou RI - Description");
        eo.setDescription(desc);
        
        ArrayList objects = new ArrayList();
        objects.add(eo);
        
        BulkResponse br = lcm.saveObjects(objects, dontVersionSlotsMap);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        
        eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithoutRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertEquals(desc.getValue(), eo.getDescription().getValue());
    }
    
    /** Test delete of Extrinsic Object **/
    public void testDeleteExtrinsicObjectWithoutRI() throws Exception {
        
        //Delete the service that was created in testSubmit
        ArrayList keys = new ArrayList();
        keys.add(lcm.createKey(eoWithoutRIId));
        BulkResponse resp = lcm.deleteObjects(keys, null, forceRemoveRequestSlotsMap, (String)null);
        JAXRUtility.checkBulkResponse(resp);
        assertEquals(BulkResponse.STATUS_SUCCESS, resp.getStatus());
        
        ExtrinsicObject eo = (ExtrinsicObject) getBQM().getRegistryObject(
                eoWithoutRIId, LifeCycleManager.EXTRINSIC_OBJECT);
        assertNull("ExtrinsicObject not deleted correctly", eo);
    }
    
    // Main method (why?) --------------------------------------------------- //
    
    public static void main(String[] args) {
        System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
