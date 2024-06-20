/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/lcm/LifeCycleManagerImplTest.java,v 1.31 2007/06/06 13:45:10 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBException;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.ReferencesExistException;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQuery;
import org.oasis.ebxml.registry.bindings.rim.Association;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.Classification;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.Slot;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.Value;
import org.oasis.ebxml.registry.bindings.rim.ValueList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * @author Farrukh Najmi
 */
public class LifeCycleManagerImplTest extends ServerTest {
        
    private static final Log log = LogFactory.getLog(LifeCycleManagerImplTest.class);
    
    //URL to remote registry for testing cooperating registries features
    //String remoteRegistryURL = System.getProperty("remoteRegistryURL", "http://rollsroyce.sfbay:8000/omar/registry");
    String remoteRegistryURL = System.getProperty("remoteRegistryURL", "http://localhost:8000/omar/registry");
    
    //id for a remote ClassificationNode
    String remoteRefId = System.getProperty("remoteRefId", BindingUtility.CANONICAL_OBJECT_TYPE_LID_RegistryObject);
    
    boolean skipReferenceCheckOnRemove = Boolean.valueOf(RegistryProperties.getInstance()
        .getProperty("omar.persistence.rdb.skipReferenceCheckOnRemove", "false")).booleanValue();
    
    
    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public LifeCycleManagerImplTest(String name) {
        super(name);
    }
    
    /**
     * This test if for making sure that a transaction involving operations across
     * registry and repository are atomic.
     *
     * Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6444810 
     */
    public void testAtomicTransactionAcrossRegistryAndRepository() throws Exception {
        final String contextId1 = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context1";
        ServerRequestContext context1 = new ServerRequestContext(contextId1, null);
        context1.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        final String contextId2 = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context2";
        ServerRequestContext context2 = new ServerRequestContext(contextId2, null);
        context2.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        final String contextId3 = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context3";
        ServerRequestContext context3 = new ServerRequestContext(contextId3, null);
        context3.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        String eoId = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:deleteSpillOverQuery:eo";
                
        try {
            removeIfExist(context1, eoId);
            
            //In contxt1 save an EO by iteself first. Do commit context.
            ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
            eo.setId(eoId);
            eo.setLid(eoId);

            submit(context1, eo);
            
            //In context2 and save EO/RI pair and dont commit context
            //Do not use ServerTest.submit as we want to keep the context open.

            RepositoryItem ri = createRepositoryItem(eoId, "1.1", ac.ALIAS_NIKOLA, ac.ALIAS_NIKOLA, true);
            idToRepositoryItemMap.clear();
            idToRepositoryItemMap.put(eoId, ri);

            SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);

            roList.getIdentifiable().add(eo);
            submitRequest.setRegistryObjectList(roList);
            context2.setRepositoryItemsMap(idToRepositoryItemMap);

            context2.pushRegistryRequest(submitRequest);
            RegistryResponse resp = lcm.submitObjects(context2);
            bu.checkRegistryResponse(resp);
            
            //Do not commit context and instead use a separate context to look up the repository item
            try {                
                rm.getRepositoryItem(eoId);
                fail("This is a known issue for Farrukh to fix. Able to see RepositoryItem from a context that has not yet committed");
            } catch (ObjectNotFoundException e) {
                //Correct behavior: RI should not be found by context3 because context2 has not committed yet.
            }
                        
        } finally {
            removeIfExist(context3, eoId);
            context3.commit();
            context2.rollback();
            context1.rollback();
        }
        
    }

    /** 
     * Tests that RemoveObjectsRequest supports the AdhocQuery param added in 3.0
     * which specifies additional objects that MUST be removed in addition to those
     * specified by the ObjectRefList param.
     *
     * AdhocQuery: This parameter specifies a query. A registry MUST remove all objects that match the specified query in addition to any other objects identified by other parameters.
     * ObjectRefList:  This parameter specifies a collection of references to existing RegistryObject instances in the registry. A registry MUST remove all objects that are referenced by this parameter in addition to any other objects identified by other parameters.
     *
     */
    public void testRemoveWithAdhocQueryAsParam() throws Exception {        
        RegistryPackage pkg1 = bu.rimFac.createRegistryPackage();
        String pkg1Id = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:pkg1";
        pkg1.setId(pkg1Id);
        
        RegistryPackage pkg2 = bu.rimFac.createRegistryPackage();
        String pkg2Id = "urn:freebxml:omar:server:lcm:LifeCycleManagerImplTest:pkg2";
        pkg2.setId(pkg2Id);
        
        ArrayList objects = new ArrayList();
        objects.add(pkg1);
        objects.add(pkg2);
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
                
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Test audit trail 
        List auditTrail = getAuditTrailForRegistryObject(context, pkg1);
        AuditableEventType firstEvent = (AuditableEventType)auditTrail.get(0);
	// Timestamps are not unique enough to be sure earliest event is first
	for (int i = 1;
	     ( auditTrail.size() > i &&
	       !firstEvent.getEventType().
	       equals(bu.CANONICAL_EVENT_TYPE_ID_Created) &&
	       firstEvent.getTimestamp().equals(((AuditableEventType)auditTrail.
						 get(i)).getTimestamp()) );
	     i++) {
	    // Try another 'firstEvent' -- also with earliest known timestamp
	    firstEvent = (AuditableEventType)auditTrail.get(i);
	}
        assertEquals("First auditable event should have been a create event.", bu.CANONICAL_EVENT_TYPE_ID_Created, firstEvent.getEventType());
        
        //Now remove of what was submitted such that pkg1 is specified by ObjectRefList param
        //and pkg2 is specified by AdhocQuery params
        ArrayList objectRefs = new ArrayList();
        ObjectRef pkg1Ref = bu.rimFac.createObjectRef();
        pkg1Ref.setId(pkg1Id);        
        objectRefs.add(pkg1Ref);
                
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        
        //Set the ObjectRefList param
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        //Set the AdhocQuery param
        AdhocQueryRequestType req = bu.createAdhocQueryRequest("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg2Id + "'");
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now make sure that both pkg1 and pkg2 are indeed removed
        req = bu.createAdhocQueryRequest("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg1Id + "' OR rp.id = '" + pkg2Id + "'");
        context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        AdhocQueryResponseType resp1 = qm.submitAdhocQuery(context);
        bu.checkRegistryResponse(resp1);
        
        assertEquals("Remove failed", 0, resp1.getRegistryObjectList().getIdentifiable().size());        
    }
    
    /*
     * Gets the List of AuditableEventTypes for the specified RegistryObjects. 
     * 
     */
    public List getAuditTrailForRegistryObject(RequestContext context, RegistryObjectType ro) throws RegistryException {
        List auditTrail = null;

        try {
            HashMap queryParams = new HashMap();
            queryParams.put("$lid", ro.getLid());
            AdhocQueryRequestType req = bu.createAdhocQueryRequest("urn:oasis:names:tc:ebxml-regrep:query:GetAuditTrailForRegistryObject", queryParams);
            context.pushRegistryRequest(req);
            AdhocQueryResponseType resp = qm.submitAdhocQuery(context);

            auditTrail = resp.getRegistryObjectList().getIdentifiable();

        } catch (JAXBException e) {
            throw new RegistryException(e);
        }    
        return auditTrail;
    }
    
    
    /**
     * Creates a RegistryPackage, saves it, updates its name once and saves it,
     * and then update its name again and saves it.
     */
    public void testMultipleUpdatesOfNameWithContextualClassification() throws Exception {
        RegistryPackage pkg = bu.rimFac.createRegistryPackage();
        String pkgId = org.freebxml.omar.common.Utility.getInstance().createId();
        pkg.setId(pkgId);
        
        //Add name to pkg
        InternationalStringType nameIS = bu.createInternationalStringType("name0");
        pkg.setName(nameIS);
        
        ArrayList objects = new ArrayList();
        objects.add(pkg);
        
        //Add a Classification to pkg        
        Classification classification1 = bu.createClassification(pkg.getId(), bu.CANONICAL_OBJECT_TYPE_ID_XML);
        classification1.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        InternationalStringType classificationName1 = bu.createInternationalStringType("classification1");
        classification1.setName(classificationName1);
        pkg.getClassification().add(classification1);
        objects.add(classification1);
        
        /*
        //Add a Classification to classification1        
        Classification classification2 = bu.createClassification(classification1.getId(), bu.CANONICAL_OBJECT_TYPE_ID_XML);
        classification2.setId(org.freebxml.omar.common.Utility.getInstance().createId());        
        InternationalStringType classificationName2 = bu.createInternationalStringType("classification2");
        classification2.setName(classificationName2);
        classification1.getClassification().add(classification2);
        */
        
        
        //Create submit request 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
                
        //Now do the submit 
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now update name first time and save
        nameIS = bu.createInternationalStringType("name1");
        pkg.setName(nameIS);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now update name second time and save
        nameIS = bu.createInternationalStringType("name2");
        pkg.setName(nameIS);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
    }
    
    //public void testSubmitWithNoUser() throws Exception {
    //}
    
        
    //TODO: Add testInvalidObjectRef
    
    /** Tests submission of an object with an ObjectRef to a remote object */
    public void testSubmitRemoteObjectRef() throws Exception {        
        RegistryPackage pkg = bu.rimFac.createRegistryPackage();
        String pkgId = "urn:uuid:e2450db9-2fbc-4185-b5ae-e607dfbda524";
        pkg.setId(pkgId);
        
        //Create a remote ObjectRef to US ClassificationNode in ISO 3166 taxonomy
        ObjectRef remoteRef = bu.rimFac.createObjectRef();

        remoteRef.setId(remoteRefId);
        remoteRef.setHome(remoteRegistryURL);
        
        //Create a Clasification that has a remote ObjectRef to a ClassificationNode
        Classification classification = bu.rimFac.createClassification();
        classification.setClassificationNode(remoteRefId);        
        
        //Classify pkg with Classification that uses remote ClassificationNode
        classification.setClassifiedObject(pkgId);

        ArrayList objects = new ArrayList();
        objects.add(pkg);
        objects.add(classification);
        objects.add(remoteRef);
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
        
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testSubmitRemoteObjectRef", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now verify that local replica of remote object can be retrieved by id
        //Get the default ACP that is expected in any minDB        
        RegistryObjectType ro = qm.getRegistryObject(context, remoteRefId);
        if (ro == null) {
            //TODO: Add this once remote object ref feature is implemented
            fail("Failed to get local replica of remote object with id: " + remoteRefId);
        }
        
        
        //Now do the remove of what was submitted
        ArrayList objectRefs = new ArrayList();
        ObjectRef pkgRef = bu.rimFac.createObjectRef();
        pkgRef.setId(pkgId);        
        objectRefs.add(pkgRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        bu.addSlotsToRequest(removeRequest, forceRemoveRequestSlotsMap);
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testSubmitRemoteObjectRef", removeRequest);
        context.setUser(ac.registryOperator);
        resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }
    
    /*
     * Test Impl specific feature to not commit a request.
     */
    public void testDontCommitMode() throws Exception {
        RegistryPackage testFolder = bu.rimFac.createRegistryPackage();
        String testFolderId = org.freebxml.omar.common.Utility.getInstance().createId();
        testFolder.setId(testFolderId);

        ArrayList objects = new ArrayList();
        objects.add(testFolder);
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        
        HashMap slotsMap = new HashMap();
        slotsMap.put(bu.CANONICAL_SLOT_LCM_DO_NOT_COMMIT, "true");
        bu.addSlotsToRequest(submitRequest, slotsMap);

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
           
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDontCommitMode", submitRequest);
        context.setUser(ac.nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        //Save with CANONICAL_SLOT_LCM_DO_NOT_COMMIT true.
        RegistryResponse resp = lcm.submitObjects(context);
        assertEquals("Request had errors.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());
        
    }
    
    /*
     * Test Impl specific feature to re-assign owner to a different owner.
     * 
     */
    public void testOwnerReassignMode() throws Exception {
        //Save as a non-RegistryAdministrator role and expect an error.
        testOwnerReassignMode(AuthenticationServiceImpl.getInstance().nikola,
            AuthenticationServiceImpl.getInstance().nikola.getId(),
            "Request should have had errors because owner reassignment can only be done by RegistryAdministrator roles.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
        
        //Save as a RegistryAdministrator role but specify invalid ownerId and expect an error.
        testOwnerReassignMode(AuthenticationServiceImpl.getInstance().registryOperator,
            org.freebxml.omar.common.Utility.getInstance().createId(),
            "Request should have had errors because specified owner is not a registered user.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
        
        //Save as a RegistryAdministrator role and valid ownerId and expect success.
        RegistryPackage testFolder = testOwnerReassignMode(AuthenticationServiceImpl.getInstance().registryOperator,
            AuthenticationServiceImpl.getInstance().nikola.getId(),
            "Request should have been successful.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);                
        
        //TODO: Need to validate that user's owner is Nikola. Doing it manually for now.
        
        
        //Delete the testFolder.
        ArrayList objectRefs = new ArrayList();
        ObjectRef pkgRef = bu.rimFac.createObjectRef();
        pkgRef.setId(testFolder.getId());        
        objectRefs.add(pkgRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testOwnerReassignMode", removeRequest);
        context.setUser(ac.registryOperator);
        RegistryResponse resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
    }
    
    private RegistryPackage testOwnerReassignMode(UserType caller, String newOwnerId,
        String errorMsg,
        String expectedStatus) throws Exception {
            
        RegistryPackage testFolder = bu.rimFac.createRegistryPackage();
        String testFolderId = org.freebxml.omar.common.Utility.getInstance().createId();
        testFolder.setId(testFolderId);

        ArrayList objects = new ArrayList();
        objects.add(testFolder);
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        
        HashMap slotsMap = new HashMap();
        slotsMap.put(bu.CANONICAL_SLOT_LCM_OWNER, newOwnerId);
        bu.addSlotsToRequest(submitRequest, slotsMap);

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap idToRepositoryItemMap = new HashMap();
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testOwnerReassignMode", submitRequest);
        context.setUser(caller);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
           
        try {
            RegistryResponse resp = lcm.submitObjects(context);
            assertEquals(errorMsg, expectedStatus, resp.getStatus());
        } catch (RegistryException e) {
            //Allow this exception if expecting failure
            if (!expectedStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure)) {
                fail(errorMsg);
            }
        }
        
        return testFolder; 
    }
    
    
    /**
     * Tests that LCM does not throw IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor owns the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectSameUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectSameUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextFarrukh, "testNoNewRefsToDeprecatedObjectSameUser");
        } catch (IllegalStateException e) {
            fail("Threw IllegalStateException for new reference to a Deprecated object even when object is my own.");
        }
    }
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor does not own the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectDifferentUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ServerRequestContext contextNikola = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:nikola", null);
        contextNikola.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextNikola, "testNoNewRefsToDeprecatedObjectDifferentUser");
            fail("Did not throw IllegalStateException for new reference to a Deprecated object.");
        } catch (IllegalStateException e) {
            //expected. All is well
        }
    }
    
    
     /*
      * Test Mapping of temp ID's to permanent UUID's (in ServerRequestContext).
      */
    public void testTempIDMappings() throws Exception 
    {        
        RegistryPackage pkg = bu.rimFac.createRegistryPackage();
        String pkgTempId    = "tempID";
        
        pkg.setId( pkgTempId );
        
        //Create a Clasification 
        Classification classification = bu.rimFac.createClassification();
        classification.setClassificationNode( remoteRefId );        
        
        //Classify pkg with this Classification
        classification.setClassifiedObject( pkgTempId );

        ArrayList objects = new ArrayList();
        objects.add( pkg);
        objects.add( classification );
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll( objects );
        submitRequest.setRegistryObjectList( roList );
        HashMap idToRepositoryItemMap = new HashMap();
        
        ServerRequestContext context = new ServerRequestContext ( "LifeCycleManagerImplTest:testTempIDMappings", submitRequest );
        context.setUser( ac.registryOperator );
        context.setRepositoryItemsMap( idToRepositoryItemMap );
        
        RegistryResponse resp = lcm.submitObjects( context );
        bu.checkRegistryResponse( resp );
        
        org.freebxml.omar.common.Utility util = org.freebxml.omar.common.Utility.getInstance();
        
        assertTrue( "TempID not replaced with valid UUID", util.isValidRegistryId( pkg.getId() ) );
        assertEquals( "TempID not mapped properly in object reference", pkg.getId(), classification.getClassifiedObject() );
        
        //Now do the remove of what was submitted
        removeIfExist(context, pkg.getId());
    }
    
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object.
     *
     */
    private void internalTestNoNewRefsToDeprecatedObject(ServerRequestContext context1, ServerRequestContext context2, String contextId) throws Exception {
        final String pkgId = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:" + contextId + ":pkg1";
        final String assId = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:" + contextId + ":ass1";

        ServerRequestContext contextRegAdmin = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObject:registryOperator", null);
        contextRegAdmin.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);

            // Create package
            RegistryPackage pkg = bu.rimFac.createRegistryPackage();
            pkg.setId(pkgId);
            submit(context1, pkg);

            //Now create and association between ass and pkg
            //This should work as object being referenced is not deprecated
            Association ass = bu.rimFac.createAssociation();
            ass.setId(assId);
            ass.setSourceObject(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved); //id of some random existing object
            ass.setTargetObject(pkgId);
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            submit(context2, ass);
            
            //Now deprecate the pkg
            setStatus(context1, pkgId, BindingUtility.CANONICAL_STATUS_TYPE_ID_Deprecated);
            
            //Now submit ass again
            //This should fail as object being referenced is deprecated
            //as long as user is not superuser or owner of reference target
            submit(context2, ass);
        } finally {
            // final clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);            
        }                
    }    
    
    /**
     * Tests that LCM throws ReferencesExistException if an object is deleted when it has
     * references from one or more objects.
     *
     */
    public void testDeleteWhenReferencesExist() throws Exception {
        final String pkgId = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExist:pkg1";
        final String assId = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExist:ass1";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDeleteWhenReferencesExist", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);

            // Create package
            RegistryPackage pkg = bu.rimFac.createRegistryPackage();
            pkg.setId(pkgId);

            //Now create and association between ass and pkg
            //This should work as object being referenced is not deprecated
            Association ass = bu.rimFac.createAssociation();
            ass.setId(assId);
            ass.setSourceObject(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved); //id of some random existing object
            ass.setTargetObject(pkgId);
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            
            ArrayList objs = new ArrayList();
            objs.add(pkg);
            objs.add(ass);
            
            submit(context, objs);
            
            
            
            //Now try deleting the pkg and verify that a ReferenceExistsException is generated
            try {
                remove(context, pkgId);
                
                if (!skipReferenceCheckOnRemove) {
                    fail("Did not throw ReferencesExistsException when deleting an object with references.");
                }
            } catch (ReferencesExistException e) {
                if (skipReferenceCheckOnRemove) {
                    fail("Should not have thrown ReferencesExistException");
                } else {
                    //log.error(e, e);
                    //Expected. Good!
                }
            }
            
            //Now try deleting pkg and ass together. This should be allowed 
            //since referenceSource is also being deleted in same request
            HashSet objIds = new HashSet();
            objIds.add(pkg.getId());
            objIds.add(ass.getId());
            remove(context, objIds, (String)null);          
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);            
        }                
    }    

    /**
     * Tests that LCM throws ReferencesExistException if an object is deleted when it has
     * references from one or more objects via a Slot of slotType ObjectRef.
     *
     */
    public void testDeleteWhenReferencesExistViaSlot() throws Exception {
        final String pkgId = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot:pkg1";
        final String pkgId2 = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot:pkgId2";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, pkgId2);

            // Create package
            RegistryPackage pkg = bu.rimFac.createRegistryPackage();
            pkg.setId(pkgId);

            RegistryPackage pkg2 = bu.rimFac.createRegistryPackage();
            pkg2.setId(pkgId2);
                        
            Slot slot = bu.rimFac.createSlot();
            slot.setName("urn:freebxml:registry:test:LifeCycleManagerImpl:testDeleteWhenReferencesExistViaSlot:slot");
            slot.setSlotType(bu.CANONICAL_DATA_TYPE_ID_ObjectRef);
            
            ValueList valueList = BindingUtility.getInstance().rimFac.createValueList();
            
            String valueStr = pkg.getId(); //Reference to pkg
            Value value = bu.rimFac.createValue();
            value.setValue(valueStr);
            valueList.getValue().add(value);                
            slot.setValueList(valueList);
            pkg2.getSlot().add(slot);
            
            ArrayList objs = new ArrayList();
            objs.add(pkg);
            objs.add(pkg2);
            
            submit(context, objs);
            
            //Now try deleting the pkg and verify that a ReferenceExistsException is generated
            try {
                remove(context, pkgId);
                if (!skipReferenceCheckOnRemove) {
                    fail("Did not throw ReferencesExistsException when deleting an object with references.");
                }
            } catch (ReferencesExistException e) {
                if (skipReferenceCheckOnRemove) {
                    fail("Should not have thrown ReferencesExistException");
                } else {
                    //log.error(e, e);
                    //Expected. Good!
                }
            }
            
            //Now try deleting pkg and pkg2 together. This should be allowed 
            //since referenceSource is also being deleted in same request
            HashSet objIds = new HashSet();
            objIds.add(pkg.getId());
            objIds.add(pkg2.getId());
            remove(context, objIds, (String)null);          
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, pkgId2);            
        }                
    } 
   /*
    * Create AdhocQuery with null query String.
    * Bug Fix : 6480083-Regression with adhoc query creation
    */  
    public void testAdhocQueryNullString() throws Exception{
        final String adhocId = "urn:freebxml:registry:test:nullQueryString:testAdhocQueryNullString:AdhocQuery";
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, true, null);
        ServerRequestContext context = new ServerRequestContext("statusUpdate:testAdhocQueryNullString", submitRequest);
        context.setUser(ac.registryOperator);
        try{
          //create new AdhocQuery Object
          AdhocQuery adhoc = bu.rimFac.createAdhocQuery();
          adhoc.setId(adhocId);
          InternationalStringType adhocName = bu.createInternationalStringType("AdhocQuery_NullString _Test");
          adhoc.setName(adhocName);
          QueryExpressionType queryExp = bu.rimFac.createQueryExpression();
          queryExp.setQueryLanguage(bu.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);
          queryExp.getContent().add(null); 
          adhoc.setQueryExpression(queryExp);
          bu.addRegistryObjectToSubmitRequest(submitRequest, adhoc);
          RegistryResponse resp = lcm.submitObjects(context);
          bu.checkRegistryResponse(resp);
          remove(context, adhocId);
        }catch(Exception e) {
            if(e.getCause() instanceof NullPointerException) {
                fail("Query String is Null for AdhocQuery Object.");
            }
        }finally {
            // final clean-up
            removeIfExist(context, adhocId);
        }
    }
    
    /**
     * Tests that LCM allows nested members within a RegistryPackage
     * and automatically creates HasMember Associations with RegistryPackage
     * and nested members. Test also makes sure that this works to multiple levels
     * of nesting.
     */
    public void testNestedMembersInRegistryPackage() throws Exception {
        final String pkgId1 = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkg1";
        final String pkgId2 = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkgId2";
        final String pkgId3 = "urn:org:freebxml:omar:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkgId3";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testNestedMembersInRegistryPackage", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId1);
            removeIfExist(context, pkgId2);
            removeIfExist(context, pkgId3);

            // Create package
            RegistryPackage pkg1 = bu.rimFac.createRegistryPackage();
            pkg1.setId(pkgId1);

            RegistryPackage pkg2 = bu.rimFac.createRegistryPackage();
            pkg2.setId(pkgId2);
            
            RegistryPackage pkg3 = bu.rimFac.createRegistryPackage();
            pkg3.setId(pkgId3);
            
            pkg1.setRegistryObjectList(bu.rimFac.createRegistryObjectListType());
            pkg1.getRegistryObjectList().getIdentifiable().add(pkg2);
            pkg2.setRegistryObjectList(bu.rimFac.createRegistryObjectListType());
            pkg2.getRegistryObjectList().getIdentifiable().add(pkg3);
                        
            
            ArrayList objs = new ArrayList();
            objs.add(pkg1);
            
            submit(context, objs);

            //Now make sure pkg2 is a member of pkg1
            HashMap queryParamsMap = new HashMap();        
            queryParamsMap.put("$packageId", pkgId1);

            List res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetMembersByRegistryPackageId, queryParamsMap);
            assertEquals("Nested member not found for pkg1", 1, res.size());
            
            RegistryPackage p = (RegistryPackage)res.get(0);
            assertEquals("Nested member id not correct for pkg1", pkgId2, p.getId());
            
            //Now make sure pkg3 is a member of pkg2
            queryParamsMap.clear();
            queryParamsMap.put("$packageId", pkgId2);

            res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetMembersByRegistryPackageId, queryParamsMap);
            assertEquals("Nested member not found for pkg1", 1, res.size());
            
            p = (RegistryPackage)res.get(0);
            assertEquals("Nested member id not correct for pkg1", pkgId3, p.getId());
            
        } finally {
            // final clean-up
            removeIfExist(context, pkgId1);
            removeIfExist(context, pkgId2);            
            removeIfExist(context, pkgId3);            
        }                
    } 
    
    
    public static Test suite() {
        junit.framework.TestSuite suite = new TestSuite(LifeCycleManagerImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new LifeCycleManagerImplTest("testNestedMembersInRegistryPackage"));
        //suite.addTest(new LifeCycleManagerImplTest("testDeleteWhenReferencesExistViaSlot"));
        //suite.addTest(new LifeCycleManagerImplTest("testNoNewRefsToDeprecatedObjectDifferentUser"));
        //suite.addTest(new LifeCycleManagerImplTest("testRemoveWithAdhocQueryAsParam"));
        return suite;
    }
}
