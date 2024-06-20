/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/security/authorization/AuthorizationServiceImplTest.java,v 1.15 2007/01/11 14:21:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.AuthorizationException;
import org.freebxml.omar.common.exceptions.UnauthorizedRequestException;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.persistence.rdb.SQLPersistenceManagerImpl;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.Association;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.Federation;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.Registry;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/**
 * Tests the AuthorizationServiceImpl class in server code.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *
 */
public class AuthorizationServiceImplTest extends ServerTest {
    
    static String assId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:assId";
    static String eoId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:eoId";
    
    public AuthorizationServiceImplTest(java.lang.String testName) {
        super(testName);
        
    }
        
    public static Test suite() {
        //Warning: this test requires its methods to be executed in sequence.
        junit.framework.TestSuite suite = new TestSuite(AuthorizationServiceImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AuthorizationServiceImplTest("testReferenceByNonOwner"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonOwnerACPNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testOwnerACPAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testMultipleACPNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testAddMemberAction"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminDefaultDefineFederationNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminDefaultDefineRegistryNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminReferenceRegistryAllowed"));
        return suite;
    }
        
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    /*
     * Tests that defaultACP allows reference from an object A to object B
     * when object A is owned by User A and object B is owner by User B and
     * submitter of reference action is User A.
     *
     * Test for: 6430938 Unable to associate Service and Service Binding
     */
    public void testReferenceByNonOwner() throws Exception {
        
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testReferenceByNonOwner";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        
        final String pkgId = contextId + ":pkg1";
        final String assId = contextId + ":ass1";
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);

            // Create package
            RegistryPackage pkg = bu.rimFac.createRegistryPackage();
            pkg.setId(pkgId);
            submit(context, pkg);

            //Now create and association between pkg and a service binding owned by someone else
            //This should work
            Association ass = bu.rimFac.createAssociation();
            ass.setId(assId);
            ass.setSourceObject(pkgId);
            ass.setTargetObject("urn:freebxml:registry:demoDB:ebXMLRegistryServiceBinding");
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            submit(context, ass);                        
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);            
        }                
        
    }
    
    /*
     * Test that server rejects the action when defaultACP
     * is submitted by a non-admin.
     */
    public void testNonAdminDefaultACPNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefaultACPNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        final String defaultACP = contextId + ":pkg1";
        
        try {            
            // Create package
            ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
            eo.setId(az.idForDefaultACP);
            
            try {
                submit(context, eo);
                fail("Non admin user should not have been able to submit defaultACP");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
        }                
    }    
    
    /**
     * Test that server rejects the action when Federation
     * is submitted by a non-admin.
     */
    public void testNonAdminDefineFederationNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefineFederationNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String federationId = contextId + ":federation";
                
        try {
            removeIfExist(context, federationId);
            
            // Create Federation
            Federation federation = bu.rimFac.createFederation();
            federation.setId(federationId);
            
            try {
                submit(context, federation);
                fail("Non admin user should not have been able to submit Federation");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, federationId);
        }                
    }    
    
    /**
     * Test that server rejects the action when Registry
     * is submitted by a non-admin.
     */
    public void testNonAdminDefineRegistryNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefineRegistryNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String registryId = contextId + ":registry";
                
        try {
            removeIfExist(context, registryId);
            
            // Create Federation
            Registry registry = bu.rimFac.createRegistry();
            registry.setId(registryId);
            
            try {
                submit(context, registry);
                fail("Non admin user should not have been able to submit Federation");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, registryId);
        }                
    }    
    
    
    /**
     * Test that server accepts the action when Registry or Federation
     * is referenced by a non-admin as long its not defining a HasFederationMember association.
     *
     */
    public void testNonAdminReferenceRegistryAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testNonAdminReferenceRegistryAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String assId = contextId + ":notHasFedarationAss";
                
        try {
            removeIfExist(context, assId);
            
            // Create Association of some type other than HasFederationMember between Federation and Registry
            Association ass = bu.rimFac.createAssociation();
            ass.setId(assId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);

            RegistryType registry = ServerCache.getInstance().getRegistry(context);            
            Federation federation = (Federation)SQLPersistenceManagerImpl.getInstance().getRegistryObjectMatchingQuery(context, "SELECT f.* FROM Federation f", null, "Federation");
            
            //The following should point to Federation and Registry instance but in this
            //context it matters not. Any Federation with this associationType is not 
            //allowed unless published by a RegistryAdmin.
            ass.setSourceObject(federation.getId());
            ass.setTargetObject(registry.getId());
            
            submit(context, ass);
        } finally {
            // final clean-up
            removeIfExist(context, assId);
        }                
    }    
    
    /**
     * Test that server rejects the action when an Association of AssociationType HasFederationMember
     * is submitted by a non-admin.
     */
    public void testNonAdminHasFederationMemberAssociationNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefaultDefineRegistryNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String assId = contextId + ":hasFedarationAss";
                
        try {
            removeIfExist(context, assId);
            
            // Create Federation
            Association ass = bu.rimFac.createAssociation();
            ass.setId(assId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember);

            //The following should point to Federation and Registry instance but in this
            //context it matters not. Any Federation with this associationType is not 
            //allowed unless published by a RegistryAdmin.
            ass.setSourceObject(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
            ass.setTargetObject(bu.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            
            try {
                submit(context, ass);
                fail("Non admin user should not have been able to submit HasFederationAssociation.");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, assId);
        }                
    }    
    
    /*
     * Test that server rejects the action when an AccessControlPolicyFor Association
     * is submitted by a non-owner of the targetObject.
     */
    public void testNonOwnerACPNotAllowed() throws Exception {
        ArrayList ros = new ArrayList();
        
        eoId = org.freebxml.omar.common.Utility.getInstance().createId();
        ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
        eo.setId(eoId);
        ros.add(eo);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testNonOwnerACPNotAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);                
        
        ros.clear();
        assId = org.freebxml.omar.common.Utility.getInstance().createId();
        Association ass = bu.rimFac.createAssociation();
        ass.setId(assId);
        ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
        ass.setTargetObject(eoId);
        ass.setSourceObject(az.idForDefaultACP);
        ros.add(ass);
        
        submitRequest = bu.createSubmitRequest(false, false, ros);
        context = new ServerRequestContext("AuthenticationServiceImpl:testNonOwnerACPNotAllowed", submitRequest);
        context.setUser(ac.nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        try {
            resp = lcm.submitObjects(context);        
            fail("Server did not throw RegistryException when AccessControlPolicyFor Association submitted by non-owner.");
        } catch (UnauthorizedRequestException e) {
            //Expected
        }
     }    
    
    /*
     * Test that server accepts the action when an AccessControlPolicyFor Association
     * is submitted by the owner of the targetObject when none existed before.
     */
    public void testOwnerACPAllowed() throws Exception {
        ArrayList ros = new ArrayList();
                
        Association ass = bu.rimFac.createAssociation();
        ass.setId(assId);
        ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
        ass.setTargetObject(eoId);
        ass.setSourceObject(az.idForDefaultACP);
        ros.add(ass);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testOwnerACPAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);                
     }    
    
    /*
     * Test that server throws Exception when an AccessControlPolicyFor Association
     * is submitted when an one exists already.
     */
    public void testMultipleACPNotAllowed() throws Exception {
        ArrayList ros = new ArrayList();
        
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testMultipleACPNotAllowed", null);
        context.setUser(ac.registryGuest);
        Association ass = (Association)qm.getRegistryObject(context, assId);
        assertNotNull("AccessControlPolicyFor Association not found", assId);
        
        //Save a a new association
        String newAssId = org.freebxml.omar.common.Utility.getInstance().createId();
        ass.setId(newAssId);
        ros.add(ass);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        context = new ServerRequestContext("AuthenticationServiceImpl:testMultipleACPNotAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        try {
            RegistryResponse resp = lcm.submitObjects(context);
            fail("Server did not throw RegistryException on duplicate ACP submission.");
        } catch (AuthorizationException e) {
            //Expected
        }
    }
    
            
    public void testAddMemberAction() throws Exception {
        //Make sure customACP are enabled for this test
        String customAccessControlPoliciesEnabled = RegistryProperties.getInstance().getProperty("omar.security.authorization.customAccessControlPoliciesEnabled", "false");
        RegistryProperties.getInstance().put("omar.security.authorization.customAccessControlPoliciesEnabled", "true");
        
        try {
            //The following id must be kept synced with misc/samples/demoDB/SUbmitObjectsRequst_Role.xml
            String parentFolderId = "urn:freebxml:registry:demoDB:folder1";
            ObjectRef parentFolderRef = bu.rimFac.createObjectRef();
            parentFolderRef.setId(parentFolderId);

            RegistryPackage childFolder = bu.rimFac.createRegistryPackage();
            String childFolderId = org.freebxml.omar.common.Utility.getInstance().createId();
            childFolder.setId(childFolderId);

            //Attempt to add childFolder to parentFolder as Developer and expect to fail
            Association ass = bu.rimFac.createAssociation();
            String assId = org.freebxml.omar.common.Utility.getInstance().createId();
            ass.setId(assId);
            ass.setSourceObject(parentFolderId);
            ass.setTargetObject(childFolderId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);

            ArrayList objects = new ArrayList();
            objects.add(ass);
            objects.add(childFolder);
            objects.add(parentFolderRef);

            //Now do the submit
            SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();

            roList.getIdentifiable().addAll(objects);
            submitRequest.setRegistryObjectList(roList);
            HashMap idToRepositoryItemMap = new HashMap();
            ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", submitRequest);
            context.setUser(ac.nikola);
            context.setRepositoryItemsMap(idToRepositoryItemMap);

            RegistryResponse resp = null;
            try {
                //Assert that a developer role cannot add members to folder
                resp = lcm.submitObjects(context);
                assertEquals("Developer should not have been able to add member.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure, resp.getStatus());
            } catch (UnauthorizedRequestException e) {
                //Expected once we fix spec to throw this Exception instead of IllegalStateException
            } catch (IllegalStateException e) {
                //Expected until we fix spec to throw UnauthorizedRequestException instead of IllegalStateException
            }

            //Assert that a ProjectLead role can add members to folder
            context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", submitRequest);
            context.setUser(ac.farrukh);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            resp = lcm.submitObjects(context);
            String respStr = bu.marshalObject(resp);
            assertEquals(respStr, BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());

            //Now do the remove of what was submitted
            ArrayList objectRefs = new ArrayList();
            ObjectRef childFolderRef = bu.rimFac.createObjectRef();
            childFolderRef.setId(childFolderId);
            objectRefs.add(childFolderRef);

            ObjectRef assRef = bu.rimFac.createObjectRef();
            assRef.setId(assId);
            objectRefs.add(assRef);

            RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
            orList.getObjectRef().addAll(objectRefs);
            removeRequest.setObjectRefList(orList);

            context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", removeRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            resp = lcm.removeObjects(context);
            bu.checkRegistryResponse(resp);
        } finally {
            RegistryProperties.getInstance().put("omar.security.authorization.customAccessControlPoliciesEnabled", customAccessControlPoliciesEnabled);
        }
    }
    
    /**
     * Tests that LCM does not throw IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor owns the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectSameUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectSameUser:farrukh", null);
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
        ServerRequestContext contextFarrukh = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ServerRequestContext contextNikola = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:nikola", null);
        contextNikola.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextNikola, "testNoNewRefsToDeprecatedObjectDifferentUser");
            fail("Did not throw IllegalStateException for new reference to a Deprecated object.");
        } catch (IllegalStateException e) {
            //expected. All is well
        }
    }
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object.
     *
     */
    private void internalTestNoNewRefsToDeprecatedObject(ServerRequestContext context1, ServerRequestContext context2, String contextId) throws Exception {
        final String pkgId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:" + contextId + ":pkg1";
        final String assId = "urn:org:freebxml:omar:server:security:authorization:AuthorizationServiceImplTest:" + contextId + ":ass1";

        ServerRequestContext contextRegAdmin = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObject:registryOperator", null);
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
    
}
