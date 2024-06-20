/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/AuditableEventTest.java,v 1.3 2006/11/07 20:11:20 dougb62 Exp $
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
import java.util.List;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;


/**
 * jUnit Test for AuditableEvent
 *
 * @author Farrukh S. Najmi
 */
public class AuditableEventTest extends ClientTest {

    public AuditableEventTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AuditableEventTest.class);
        return suite;
    }
    
    /** Test creation of an Extrinsic Object */
    public void testGetAssociatedObjects() throws Exception {
        RegistryPackage pkg1 = lcm.createRegistryPackage("AuditableTest.pkg1");
        String pkg1Id = pkg1.getKey().getId();        
	deleteIdToTypeMap.put(pkg1Id, lcm.REGISTRY_PACKAGE);
                        
        //Now save pkg1
        Collection saveObjects = new ArrayList();
        saveObjects.add(pkg1);
        lcm.saveObjects(saveObjects);
        
        //Now read back pkg1 to verify that it was saved
        pkg1 = (RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNotNull("pkg1 was not saved", pkg1);
        
        //Now getAuditTrail andmake sure it is not empty
        Collection aeList = pkg1.getAuditTrail();
        assertEquals("Invalid AuditableEvent count", 1, aeList.size());
        
        AuditableEventImpl ae = (AuditableEventImpl)aeList.iterator().next();
        // deprecated: RegistryObject ro = ae.getRegistryObject();
        // assertNotNull("AuditableEvent.getRegistryObject() returned null", ro);
	List affected = ae.getAffectedObjects();
	assertNotNull("AuditableEvent.getAffectedObjects() returned null",
		      affected);
	assertEquals("AuditableEvent.getAffectedObjects() unexpected list size",
		     1, affected.size());

	RegistryObject ro = (RegistryObject)affected.get(0);
	assertNotNull("AuditableEvent.getAffectedObjects() included null", ro);

        assertEquals("Unexpected object in affectedObjects",
		     pkg1Id, ro.getKey().getId());
    }
}
