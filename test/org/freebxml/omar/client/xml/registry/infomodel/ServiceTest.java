/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ServiceTest.java,v 1.6 2006/07/19 19:32:43 farrukh_najmi Exp $
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
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;

/**
 * JUnit Test for ServiceImpl.
 *
 * Warning: This test MUST be run in sequence as test methods have dependencies.
 *
 * @author Diego Ballve / Republica Corp.
 */
public class ServiceTest extends ClientTest {
    
    static String serviceId = "urn:freebxml:registry:test:client:ServiceTest:service1";
    static String org1Id = "urn:freebxml:registry:test:client:ServiceTest:org1";
    static String org2Id = "urn:freebxml:registry:test:client:ServiceTest:org2";
    
    public ServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ServiceTest.class);
        return suite;
    }
    
    /** Test submit of a Service */
    public void testSubmit() throws Exception {
        Service service = (Service)getLCM().createService("ServiceTest_Service1");
        service.setKey(getLCM().createKey(serviceId));
        System.err.println("Adding service with id: " + serviceId);
        
        //Set to expire on my 100th birthday. Test expiration truncation bug reported by Rajesh.
        service.setExpiration(new java.util.Date((new java.util.GregorianCalendar(2061, 0, 10)).getTimeInMillis()));    

        service.setStability(Service.STABILITY_STATIC);
        
        ArrayList objects = new ArrayList();
        objects.add(service);
        
        BulkResponse resp = getLCM().saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);
    }
    
    /** Test query of a Service */
    //public void testQuery() throws Exception {        
    //}

    public void testSetProvidingOrganization() throws Exception {
        Organization org1 = createOrganization("ServiceTest_Org1");
        org1.setKey(getLCM().createKey(org1Id));
        System.err.println("Adding org with id: " + org1Id);
        
        Organization org2 = createOrganization("ServiceTest_Org2");
        org2.setKey(getLCM().createKey(org2Id));
        System.err.println("Adding org with id: " + org2Id);
        
        Collection objects = new ArrayList();
        objects.add(org1);
        objects.add(org2);
        BulkResponse resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);
        
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        
        // Set the providing organization to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);

        // Set the providing organization to 'org2'.
        service.setProvidingOrganization(org2);
        
        objects = new ArrayList();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org2Id);

        // Set providing organization to null.
        service.setProvidingOrganization(null);
        
        objects = new ArrayList();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, null);

        // Set the providing organization back to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);
    }
    
    /** Test delete of a Service */
    public void testDelete() throws Exception {
        //Delete the service that was created in testSubmit
        ArrayList keys = new ArrayList();
        keys.add(getLCM().createKey(serviceId));
        BulkResponse resp = getLCM().deleteObjects(keys, LifeCycleManager.SERVICE);
        JAXRUtility.checkBulkResponse(resp);
        
        // delete associations of service.
        resp = getBQM().findAssociations(null, null, serviceId, null);
        JAXRUtility.checkBulkResponse(resp);
        resp = lcm.deleteObjects(JAXRUtility.getKeysFromObjects(resp.getCollection()));
        JAXRUtility.checkBulkResponse(resp);
        
        //Delete the organization that was created in testSetProvidingOrganization().
        keys = new ArrayList();
        keys.add(getLCM().createKey(org1Id));
        keys.add(getLCM().createKey(org2Id));
        resp = getLCM().deleteObjects(keys, LifeCycleManager.ORGANIZATION);
        JAXRUtility.checkBulkResponse(resp);
    }

    protected void assertOrgIsProvidingOrg(String serviceId, String orgId) throws Exception {
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        
        if (orgId == null) {
            assertNull("Service should have null providing Organization.", service.getProvidingOrganization());
        } else {
            try {
                Organization org = service.getProvidingOrganization();
                assertNotNull("Service's providing Organization should not be null", org);
                assertEquals("Service's providing Organization id should match.", orgId, org.getKey().getId());
            } catch (ClassCastException e) {
                fail("Service's providing Organization should be an Organization.");
            }
	}
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
