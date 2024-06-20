/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/lcm/AuditableEventPruningTest.java,v 1.4 2007/04/13 03:20:09 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.registry.JAXRException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.UnauthorizedRequestException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.Classification;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * @author Farrukh Najmi
 */
public class AuditableEventPruningTest extends ServerTest {
            
    static RegistryPackage pkg1 = null;
    static String pkg1Id = "urn:freebxml:omar:server:lcm:AudtibleEventPruningTest:pkg1";
    static String getAuditableEventsQueryStr = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao WHERE ae.id = ao.eventId and ao.id = '" + pkg1Id + "' ORDER BY ae.timestamp_ DESC" ; 
    static String getPkg1QueryStr = "SELECT pkg.* FROM RegistryPackage pkg WHERE pkg.id = '" + pkg1Id + "'" ; 
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public AuditableEventPruningTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        // These tests need to be ordered
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new AuditableEventPruningTest("testSetup"));
        suite.addTest(new AuditableEventPruningTest("testCreateAuditTrail"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsOwner"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsRegistryAdmin"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsRegistryAdmin"));
	// Clean up because object may be left in an invalid state after
	// successfully pruning associated events
        suite.addTest(new AuditableEventPruningTest("testSetup"));
        
        return suite;
    }
    
    /**
     * Remove test objects from previous test iterations before starting test.
     */
    public void testSetup() throws Exception {
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        RegistryResponseType resp = null;
        
        //Remove pkg1
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getPkg1QueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testSetup", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        try {
            resp = lcm.removeObjects(context);
            //bu.checkRegistryResponse(resp);
        } catch (JAXRException e) {
            //Ignore
        }
        
        req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        context = new ServerRequestContext("AuditableEventPruningTest:testSetup", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);        
        
        try {
            resp = lcm.removeObjects(context);
            //bu.checkRegistryResponse(resp);
        } catch (JAXRException e) {
            //Ignore
        }
        
    }
    
    /**
     * Creates an audit trail by publishing and editing an object.
     */ 
    public void testCreateAuditTrail() throws Exception {
        //Create the Created event
        pkg1 = bu.rimFac.createRegistryPackage();
        pkg1.setId(pkg1Id);
        
        ArrayList objects = new ArrayList();
        objects.add(pkg1);
        
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
                
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now create the Updated event by just changing the name
        pkg1.setName(bu.getName(pkg1Id));
        
        objects = new ArrayList();
        objects.add(pkg1);
        
        //Now do the submit 
        submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        idToRepositoryItemMap = new HashMap();
                
        context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Fetch AuditableEvents for pkg1 and assert that there is at least 2 (Created and Updated)
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", req);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        AdhocQueryResponseType queryResp = qm.submitAdhocQuery(context);
        List res = queryResp.getRegistryObjectList().getIdentifiable();
        assertTrue("Must have at least 2 Auditable events. It is normal for this test to fail when versioning is turned on.", (res.size() >= 2));
    }    
    
    /**
     * Attempt to prune AuditableEvents for object using identity of objects owner.
     * It is expected that the registry will not allow this to happen as only 
     * RegistryAdministrator roles are allowed to prune AuditableEvents
     */ 
    public void testPruneEventsAsOwner() throws Exception {
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        
        //Now remove the AuditableEvents for pkg1         
        //Set the AdhocQuery param
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testPruneEventsAsOwner", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            RegistryResponseType resp = lcm.removeObjects(context);
            this.fail("Owner should not be allowed to delete AuditableEvents.");
        } catch (UnauthorizedRequestException e) {
            //All is well. This was expected.
        }
    }    
    
    /**
     * Attempt to prune AuditableEvents for object using identity RegistryOperator.
     * It is expected that the registry will allow this to happen as RegistryOperator user has 
     * RegistryAdministrator role.
     */ 
    public void testPruneEventsAsRegistryAdmin() throws Exception {
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testPruneEventsAsRegistryAdmin", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        //Now remove the AuditableEvents for pkg1         
        //Set the AdhocQuery param
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }    
    
    /**
     * Attempt to prune Created or Relocated AuditableEvents for object using identity RegistryOperator.
     * It is expected that the registry will not allow this to happen if there are objects effected
     * by this event as this would make it no longer possible to determine owner of object.
     */ 
    public void testPruneProvenanceEventsAsRegistryAdmin() throws Exception {
        //TBD. This functionality is being thought thru from a spec perspective
    }    
    
}
