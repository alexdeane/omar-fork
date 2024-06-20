/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/lcm/FederationSystemTest.java,v 1.2 2005/11/21 04:28:27 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import junit.framework.Test;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.query.federation.FederatedQueryManager;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.Federation;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.Registry;

/**
 * @author Farrukh Najmi
 */
public class FederationSystemTest extends ServerTest {
    
    private static Federation federation = null;
    private static Registry registry1 = null;
    private static Registry registry2 = null;
    
    
    /** Creates a new instance of FederationTest */
    public FederationSystemTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        // Test creating and retrieving 2 internal schemes and 1 external scheme
        suite.addTest(new FederationSystemTest("testGetRegistries"));
        suite.addTest(new FederationSystemTest("testCreateFederation"));
        suite.addTest(new FederationSystemTest("testJoinFederation"));
        suite.addTest(new FederationSystemTest("testFederatedQuery"));
        suite.addTest(new FederationSystemTest("testLeaveFederation"));
        suite.addTest(new FederationSystemTest("testDissolveFederation"));
                        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    /**
     * Tests getting of Registry instance for the two test registries.
     */
    public void testGetRegistries() throws Exception {
        
    }
        
    /**
     * Tests creating a new Federation.
     */
    public void testCreateFederation() throws Exception {
        federation = bu.rimFac.createFederation();
        String fedId = org.freebxml.omar.common.Utility.getInstance().createId();
        federation.setId(fedId);
        
        //Add name to federation
        InternationalStringType nameIS = bu.createInternationalStringType("FederationSystemTest-TestFederation");
        federation.setName(nameIS);
    }
        
    /**
     * Tests the joining of registries with a Federation.
     */
    public void testJoinFederation() throws Exception {
    }
        
    /**
     * Tests executing a federated query across 2 registries in a Federation.
     */
    public void testFederatedQuery() throws Exception {
        FederatedQueryManager fqm = FederatedQueryManager.getInstance();
        AdhocQueryRequest req = bu.createAdhocQueryRequest("SELECT * FROM RegistryPackage WHERE lid LIKE '" + 
            BindingUtility.FEDERATION_TEST_DATA_LID_PREFIX + "%'");
        ServerRequestContext context = new ServerRequestContext("FederationSystemTest:testFederatedQuery", req);
        context.setUser(ac.registryOperator);
        AdhocQueryResponseType resp = fqm.submitAdhocQuery(context);
    }
    
    
    /**
     * Tests the leaving of registries from a Federation.
     */
    public void testLeaveFederation() throws Exception {
    }
    
    /**
     * Tests the dissolving of a Federation.
     */
    public void testDissolveFederation() throws Exception {
    }
    
}
