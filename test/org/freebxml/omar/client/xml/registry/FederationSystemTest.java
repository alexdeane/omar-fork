/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/FederationSystemTest.java,v 1.5 2005/06/10 17:38:21 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.infomodel.FederationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.IdentifiableImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectRef;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;


/**
 * Client side System tests for testing multi-registry federation features.
 * Requires that there be 2 instance of registry deployed under omar.name of "omar" and "omar1"
 *
 * @author Farrukh Najmi
 */
public class FederationSystemTest extends MultiRegistrySystemTest {
        
    private static FederationImpl federation = null;
    
    /** Creates a new instance of FederationTest */
    public FederationSystemTest(String name) {
        super(name);        
    }
    
    public static Test suite() {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new FederationSystemTest("testGetRegistries"));
        suite.addTest(new FederationSystemTest("testCreateFederation"));
        suite.addTest(new FederationSystemTest("testJoinFederation"));
        suite.addTest(new FederationSystemTest("testAddObjectsToFederationMembers"));
        suite.addTest(new FederationSystemTest("testFederatedQuery"));
        suite.addTest(new FederationSystemTest("testLeaveFederation"));
        suite.addTest(new FederationSystemTest("testDissolveFederation"));
                        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    /**
     * Tests creating a new Federation.
     */
    public void testCreateFederation() throws Exception {        
        federation = lcm1.createFederation("FederationSystemTest Federation");        
        
        ArrayList objects = new ArrayList();
        objects.add(federation);
        BulkResponse br = lcm1.saveObjects(objects); // publish to registry1
        assertResponseSuccess("Error during save of Federation", br);        
    }
        
    /**
     * Tests the joining of registries with a Federation.
     */
    public void testJoinFederation() throws Exception {
        //Should read the object using connection1 to be safe and realistic
        federation = (FederationImpl)dqm1.getRegistryObject(federation.getKey().getId());
        assertNotNull("federation could not be read back after save", federation);
        
        //Have registry1 and registry2 join the federation
        federation.join(registry1);
                
        RegistryObjectRef registry2Ref = new RegistryObjectRef(lcm1);
        registry2Ref.setKey(registry2.getKey());
        registry2Ref.setHome(regSoapUrl2.replaceFirst("/soap",""));
        federation.join(registry2Ref);

        //Save modified federation to registry1 
        ArrayList objects = new ArrayList();
        objects.add(federation);
        
        BulkResponse br = null;
        //try {
        br = lcm1.saveObjects(objects); // publish to registry1
        assertResponseSuccess("Error during save of Federation", br);        
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    int i=0;
        //}
        //read the object using connection1
        federation = (FederationImpl)dqm1.getRegistryObject(federation.getKey().getId());
        assertNotNull("federation could not be read back after save", federation);
        
        Set members = federation.getFederationMembers();
        assertEquals("Federation member count was not correct", 2, members.size());
        assertTrue("Registry1 not a member of federation as expected", members.contains(registry1));
        assertTrue("Registry2 not a member of federation as expected", members.contains(registry2));        
    }
    
    /**
     * Tests adding a Service to each registry in federation for future federated query.
     */
    public void testAddObjectsToFederationMembers() throws Exception {
        service1 = lcm1.createService("FederationSystemTest Service1");
        service1 = (Service)saveAndGetIdentifiable((IdentifiableImpl)service1);
        
        service2 = lcm2.createService("FederationSystemTest Service2");
        service2 = (Service)saveAndGetIdentifiable((IdentifiableImpl)service2);
    }
        
    /**
     * Tests executing a federated query across 2 registries in a Federation.
     */
    public void testFederatedQuery() throws Exception {        
        String queryStr = "SELECT s.* FROM Service s WHERE s.id IN ('" + 
            service1.getKey().getId() + "', '" + service2.getKey().getId() + "' )";
        QueryImpl query = (QueryImpl)dqm1.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        query.setFederated(true);
        query.setFederation(federation.getId());
        
        BulkResponse br = null;
        //try {
            br = dqm1.executeQuery(query);
            assertResponseSuccess("Error during federated query", br);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    int i=0;
        //}
        
        //Now check that service1 and service2 are both returned by federated query (one from each registry)
        assertTrue("Federated query result does not contain service1", (br.getCollection().contains(service1)));
        assertTrue("Federated query result does not contain service2", (br.getCollection().contains(service2)));
    }
    
    
    /**
     * Tests the leaving of registries from a Federation.
     */
    public void testLeaveFederation() throws Exception {
        //Should read the object using connection1 to be safe and realistic
        federation = (FederationImpl)dqm1.getRegistryObject(federation.getKey().getId());
        assertNotNull("federation could not be read back after save", federation);
        
        //Have registry1 and registry2 leave the federation
        federation.leave(registry1);
        RegistryObjectRef registry2Ref = new RegistryObjectRef(lcm1);
        registry2Ref.setKey(registry2.getKey());
        registry2Ref.setHome(regSoapUrl2.replaceFirst("/soap",""));
        federation.leave(registry2Ref);

        //Save modified federation to registry1 
        ArrayList objects = new ArrayList();
        objects.add(federation);
        BulkResponse br = lcm1.saveObjects(objects); // publish to registry1
        
        //read the object using connection1
        federation = (FederationImpl)dqm1.getRegistryObject(federation.getKey().getId());
        assertNotNull("federation could not be read back after save", federation);
        
        Set members = federation.getFederationMembers();
        assertTrue("Federation member count was not exactly 0.", (members.size() == 0));
        assertFalse("Registry1 did not leave federation as expected", members.contains(registry1));
        assertFalse("Registry2 did not leave of federation as expected", members.contains(registry2));        
    }
    
    /**
     * Tests the dissolving of a Federation.
     */
    public void testDissolveFederation() throws Exception {
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(federation.getKey());
        BulkResponse br = lcm1.deleteObjects(deleteObjects);
        assertTrue("federation deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        
        //Should read the object using connection1 to make sure it was deleted
        federation = (FederationImpl)dqm1.getRegistryObject(federation.getKey().getId());
        assertNull("federation still in registry after delete", federation);
    }
    
}
