/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/lcm/RelocationManagerImplTest.java,v 1.6 2005/11/21 04:28:27 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AdhocQuery;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/**
 * @author Farrukh Najmi
 */
public class RelocationManagerImplTest extends ServerTest {
                
    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public RelocationManagerImplTest(String name) {
        super(name);
    }
        
    
    /*
     * Test Impl specific feature to not commit a request.
     * Save an Object as Nikola and then assign it to farrukh using RegistryOperator identity.
     */
    public void testReAssignOwnerImmediately() throws Exception {
        
        //Save an Object as Nikola 
        RegistryPackage testFolder = bu.rimFac.createRegistryPackage();
        String testFolderId = org.freebxml.omar.common.Utility.getInstance().createId();
        testFolder.setId(testFolderId);

        ArrayList objects = new ArrayList();
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        roList.getIdentifiable().add(testFolder);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
           
        ServerRequestContext context = new ServerRequestContext("RelocationManagerImplTest:testReAssignOwnerImmediately", submitRequest);
        context.setUser(ac.nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponse resp = lcm.submitObjects(context);
        assertEquals("Request had errors.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());
        
        //Now assign it to farrukh using RegistryOperator identity
        RelocateObjectsRequest relocateReq = bu.lcmFac.createRelocateObjectsRequest();
        relocateReq.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        
        ObjectRefType ownerAtDest = bu.rimFac.createObjectRef();
        ownerAtDest.setId(AuthenticationServiceImpl.getInstance().ALIAS_FARRUKH);
        relocateReq.setOwnerAtDestination(ownerAtDest);
        
        AdhocQuery adhocQuery = bu.createAdhocQuery("SELECT p.* from RegistryPackage p WHERE p.id='" + testFolderId + "'");
        relocateReq.setAdhocQuery(adhocQuery);
        
        context = new ServerRequestContext("RelocationManagerImplTest:testReAssignOwnerImmediately", relocateReq);
        context.setUser(ac.registryOperator);
        resp = lcm.relocateObjects(context);
        assertEquals("Request had errors during relocateObjectRequest.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());
        
        System.err.println("Relocated object with id " +  testFolderId);
        System.err.println("");
    }        
    
    public static Test suite() {
        return new TestSuite(RelocationManagerImplTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}
