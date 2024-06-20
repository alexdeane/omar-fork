/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/RegistryPackageTest.java,v 1.13 2006/07/26 17:25:36 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;

/**
 * JUnit Test for RegistryPackage
 *
 * @author Nikita Sawant, Sun Microsystems
 */
public class RegistryPackageTest extends ClientTest {
    private static Log log =
	LogFactory.getLog(RegistryPackageTest.class.getName());
    
    String pkgId = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
    
    //The id for the canonical XML Cataloging Service guaranteed to be in an ebXML Registry
    String serviceId = "urn:oasis:names:tc:ebxml-regrep:Service:CanonicalXMLCatalogingService";
    
    public RegistryPackageTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RegistryPackageTest.class);
        return suite;
    }
    
    /** Test Creation of RegistryPackage*/
    public void testSubmit() throws Exception {
        // -- Create a Registry Package
        RegistryPackage pkg = getLCM().createRegistryPackage("RegistryPackage_SomeName");
        pkg.setKey(getLCM().createKey(pkgId));                
        
        // -- Add the Slots ( a.k.a Attributes to it )
        ArrayList slots = new ArrayList();
        slots.add( getLCM().createSlot("TargetNamespace", "TargetNamepsace", null) );
        pkg.addSlots(slots);
        
        // -- Save the Object
        ArrayList pkgs = new ArrayList();
        pkgs.add(pkg);
        BulkResponse resp = getLCM().saveObjects(pkgs);
        System.out.println("Created Registry Package with Id " + pkgId);
        JAXRUtility.checkBulkResponse(resp);
    }
    
    
    /** Test addition of Member Services to RegistryPackage */
    public void testAddRegistryObjects() throws Exception {
        
        // -- Get the Registry Package
        RegistryPackage pkg =
	    (RegistryPackage) getBQM().getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        Assert.assertNotNull("Could not retrieve test package submitted in testSubmit", pkg);
        
        // -- Get the Service
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        Assert.assertNotNull("Could not retrieve canonical XML Cataloging Service", service);
        
        // -- Add service to Registry Package and save
        ArrayList members = new ArrayList();
        members.add(service);
        
        pkg.addRegistryObjects(members);
        
        // -- Save the Object
        ArrayList objectsToSave = new ArrayList();
        objectsToSave.add(pkg);
        
        BulkResponse resp = getLCM().saveObjects(objectsToSave);
        
        JAXRUtility.checkBulkResponse(resp);
        
    }
    
    
    /** Test query of a RegistryPackage and member
     *  Services  **/
    public void testQuery() throws Exception {
        // -- Get the Registry Package
        RegistryPackage
        pkg = (RegistryPackage) getBQM().getRegistryObject(pkgId);
        Assert.assertNotNull("RegistryPackage was not found when queried by id.", pkg);
        
        // -- Get the Member Services
        java.util.Set members = pkg.getRegistryObjects();
        Assert.assertNotNull("RegistryPackage must not have null for members.", members);
        
        Assert.assertEquals("The RegistryPackage does not have expected number of members.", 1, members.size());                
        
        java.util.Iterator itr = members.iterator();
        RegistryObject service = (RegistryObject) itr.next();
        
        Assert.assertEquals("The member does not have the expected id.", serviceId, service.getKey().getId());                
        
    }
        
    /**
     * Test that removeRegistryObject really does remove the
     * association between this RegistryPackage and the member
     * RegistryObject.
     *
     * @exception Exception if an error occurs
     */
    public void testRemoveRegistryObject() throws Exception {
	HashMap saveObjectsSlots = new HashMap();

	//The bulk loader MUST turn off versioning because it updates
	//objects in its operations which would incorrectly be created as
	//new objects if versioning is ON when the object is updated.
	saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
	saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");        

	String testName = "testRemoveRegistryObject";

	String uuid1 =
	    org.freebxml.omar.common.Utility.getInstance().createId();

        RegistryPackage pkg1 = getLCM().createRegistryPackage(uuid1);
        pkg1.setKey(getLCM().createKey(uuid1));
	pkg1.setDescription(getLCM().createInternationalString(testName));

        // -- Save the Object
        ArrayList objects = new ArrayList();
        objects.add(pkg1);
        BulkResponse resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Created Registry Package with Id " + uuid1);

	String uuid2 =
	    org.freebxml.omar.common.Utility.getInstance().createId();

        RegistryPackage pkg2 = getLCM().createRegistryPackage(uuid2);
        pkg2.setKey(getLCM().createKey(uuid2));                
	pkg2.setDescription(getLCM().createInternationalString(testName));

        // -- Add pkg2 to Registry Package and save
        pkg1.addRegistryObject(pkg2);
        
        // -- Save the Object
	objects = new ArrayList();
        objects.add(pkg1);
        objects.add(pkg2);
        
	resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Added Registry Package with Id " + uuid2);
	
	// Remove the package.
	pkg1.removeRegistryObject(pkg2);
        // -- Save the Object
        objects = new ArrayList();
        objects.add(pkg1);
        
	resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Removed Registry Package with Id " + uuid2);

	// Get 'HasMember' associations of pkg1.
	ArrayList associationTypes = new ArrayList();
	associationTypes.add(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);

	resp = getBQM().findAssociations(null, uuid1, null, associationTypes);
	JAXRUtility.checkBulkResponse(resp);

	Collection coll = resp.getCollection();

	if (coll.size() != 0) {
	    Iterator iter = coll.iterator();

	    while (iter.hasNext()) {
		Association ass = (Association) iter.next();

		System.err.println("Association: " + ass.getKey().getId() +
				   "; sourceObject: " + ass.getSourceObject().getKey().getId() +
				   "; targetObject: " + ass.getTargetObject().getKey().getId());
	    }
	}

	assertEquals("uuid1 should not be the sourceObject in any HasMember associations.",
		     0, coll.size());

	objects = new ArrayList();
        objects.add(pkg1.getKey());
        objects.add(pkg2.getKey());
	if (coll.size() != 0) {
	    Iterator itr = coll.iterator();
	    while (itr.hasNext()) {
		RegistryObject ro = (RegistryObject)itr.next();
	        objects.add(ro.getKey());
	    }
	}
	resp = getLCM().deleteObjects(objects);
        JAXRUtility.checkBulkResponse(resp);
    }

    public void testDelete() throws Exception {        
        //Delete the service that was created in testSubmit
        deleteIfExist(pkgId, LifeCycleManager.REGISTRY_PACKAGE);        
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
    
}
