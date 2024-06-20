/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cache/ClassificationSchemeCacheTest.java,v 1.4 2005/12/02 17:50:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;


/**
 * @author Farrukh Najmi
 */
public class ClassificationSchemeCacheTest extends ServerTest {

    String node1Id = "urn:org:freebxml:omar:server:cache:ClassificationSchemeCacheTest:node1";

    
    public ClassificationSchemeCacheTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(ClassificationSchemeCacheTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testGetAllSchemes() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testGetAllSchemes", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        List schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
    }
    
    /*
     * Publishes a single node to AssociationType scheme and then callss getAllClassificationSchemes
     * and verifies all schemes are there and that new node is present.
     */
    public void testUpdateCacheWhenNodeInserted() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testUpdateCacheWhenNodeInserted", null);
        context.setUser(ac.farrukh);
        List schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        int oldSchemeSize = schemes.size();
        
        ClassificationNodeType node = bu.rimFac.createClassificationNode();
        node.setId(node1Id);
        node.setCode("testUpdateCacheWhenNodeInserted");
        node.setParent(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType);
        
        List objects = new ArrayList();
        objects.add(node);
        
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        Map idToRepositoryItemMap = new HashMap();
                
        //Now do the submit 
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            context.pushRegistryRequest(submitRequest);
            RegistryResponse resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
        } finally {
            context.popRegistryRequest();
        }
        
        schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        assertEquals("Cache update made cache lose some schemes", oldSchemeSize, schemes.size());
        assertTrue("Unable to readback node after inserting it to scheme.", schemesContainsNode(schemes, bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType, node1Id));
        
    }
    
    /*
     * Removes a single node to AssociationType scheme and then calls getAllClassificationSchemes
     * and verifies all schemes are there and that removed node is not present.
     */
    public void testUpdateCacheWhenNodeRemoved() throws Exception {
        ArrayList objectRefs = new ArrayList();
        ObjectRef nodeRef = bu.rimFac.createObjectRef();
        nodeRef.setId(node1Id);        
        objectRefs.add(nodeRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testUpdateCacheWhenNodeRemoved", removeRequest);
        context.setUser(ac.registryOperator);

        //Remember old scheme size
        List schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        int oldSchemeSize = schemes.size();        
        
        
        RegistryResponse resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Make sure # of schemes is same as before.
        schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        assertEquals("Cache update made cache lose some schemes", oldSchemeSize, schemes.size());
        
    }
    
    private ClassificationSchemeType getClassificationSchemeFromSchemeList(List schemes, String schemeId) {
        ClassificationSchemeType scheme = null;
        
        Iterator iter = schemes.iterator();
        while (iter.hasNext()) {
            ClassificationSchemeType currentScheme = (ClassificationSchemeType)iter.next();
            if (currentScheme.getId().equalsIgnoreCase(schemeId)) {
                scheme = currentScheme;
                break;
            }
        }
        return scheme;
    }
    
    private boolean schemesContainsNode(List schemes, String schemeId, String nodeId) {
        boolean containsNode = false;
        
        ClassificationSchemeType scheme = getClassificationSchemeFromSchemeList(schemes, schemeId);
        List children = scheme.getClassificationNode();
        
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            ClassificationNodeType node = (ClassificationNodeType)iter.next();
            //System.err.println("node id:" + node.getId());
            if (node.getId().equalsIgnoreCase(nodeId)) {
                containsNode = true;
                break;
            }
        }
        
        return containsNode;
    }
    
}
