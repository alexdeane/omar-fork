/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/ReplicationSystemTest.java,v 1.3 2005/06/10 17:38:21 dougb62 Exp $
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
public class ReplicationSystemTest extends MultiRegistrySystemTest {
            
    /** Creates a new instance of FederationTest */
    public ReplicationSystemTest(String name) {
        super(name);        
    }
    
    public static Test suite() {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new ReplicationSystemTest("testGetRegistries"));
        suite.addTest(new ReplicationSystemTest("testCreateReplicas"));
        suite.addTest(new ReplicationSystemTest("testQueryReplicas"));
        //TODO: uncomment once checks are done in server to prevent LCM operations on remote objects
        //suite.addTest(new ReplicationSystemTest("testLCMOnReplicas"));
        suite.addTest(new ReplicationSystemTest("testDeleteReplicas"));
                        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
            
    /**
     * Tests creating local replicas of remote objects.
     */
    public void testCreateReplicas() throws Exception {
        //Create service2 and save to registry2
        service2 = lcm2.createService("ReplicationSystemTest Service2");
        service2 = (Service)saveAndGetIdentifiable((IdentifiableImpl)service2);
        
        //Now submit remote ObjectRef to service2 to registry1
        //This should create a replica in registry1 for service2
        RegistryObjectRef service2Ref = new RegistryObjectRef(lcm1);
        service2Ref.setKey(service2.getKey());
        service2Ref.setHome(regSoapUrl2.replaceFirst("/soap",""));
        
        ArrayList objects = new ArrayList();
        objects.add(service2Ref);
        BulkResponse br = lcm1.saveObjects(objects);
        assertTrue("Save of remote ObjectRef failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
    }
        
    /**
     * Tests the queries that match expected local replicas.
     */
    public void testQueryReplicas() throws Exception {
        service2Replica = (Service)dqm1.getRegistryObject(service2.getKey().getId());
        assertNotNull("Local replica of service2 not found on registry1", service2Replica);        
    }
    
    /**
     * Tests that LCM operations on local replicas are not allowed.
     */
    public void testLCMOnReplicas() throws Exception {
        //Try updating service2replica and expect an error
        service2Replica.setName(lcm1.createInternationalString("Service1Replica updated"));
        ArrayList updateObjects = new ArrayList();
        updateObjects.add(service2Replica);
        BulkResponse br = lcm1.saveObjects(updateObjects);
        assertFalse("service2 replica update succeeded when it should not be allowed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
    }
        
    /**
     * Tests deletion of local replicas
     */
    public void testDeleteReplicas() throws Exception {
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(service2Replica.getKey());
        BulkResponse br = lcm1.deleteObjects(deleteObjects);
        assertTrue("service2 replica deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
    }
    
}
