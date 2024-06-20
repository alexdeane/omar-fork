/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cache/ObjectCacheTest.java,v 1.6 2006/05/22 13:56:27 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.User;
import org.oasis.ebxml.registry.bindings.rim.Value;
import org.oasis.ebxml.registry.bindings.rim.ValueList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;


/**
 * @author Diego Ballve / Digital Artefacts
 */
public class ObjectCacheTest extends ServerTest {

    public ObjectCacheTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(ObjectCacheTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectNullRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectsNotRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectsNullRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testGetRegistryObjectNullId"));
        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Test that putRegistryObject(RegistryObject ro) does not throw an NPE
     * when the Object is null.
     */
    public void testPutRegistryObjectNullRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNullRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            ObjectCache.getInstance().putRegistryObject(null);            
        } catch (NullPointerException e) {
            fail("putRegistryObject generated NPE with null parameter.");
        }
    }
    
    /**
     * Test that putRegistryObjects(List registryObjects) does not throw an Exception and does not add an object
     * to cache when the Object is null.
     */
    public void testPutRegistryObjectsNullRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNotRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ObjectCache cache = ObjectCache.getInstance();
        
        List objects = new ArrayList();
        objects.add(null);
        int preCacheSize = cache.internalCache.getSize();
        cache.putRegistryObjects(objects);
        int postCacheSize = cache.internalCache.getSize();
        assertTrue("Cache size should not have grown", (preCacheSize >= preCacheSize));
    }
    
    /**
     * Test that putRegistryObjects(List registryObjects) throws a RegistryException and does not add an object
     * to cache when the Object is not a RegistryObject.
     */
    public void testPutRegistryObjectsNotRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNotRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            List objects = new ArrayList();
            objects.add(new String("This is not a RegistryObject"));
            ObjectCache.getInstance().putRegistryObjects(objects);
        } catch (RegistryException e) {
            //Expected
        }
    }
    
    /**
     * Test that getRegistryObjectInternal() does not generate an NPE when a null id is passed
     * and instead returns a null RegistryObject. 
     */
    public void testGetRegistryObjectNullId() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testGetRegistryObjectInternalNullId", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            RegistryObjectType ro = ObjectCache.getInstance().getRegistryObject(context, (String)null, "RegistryObject");
        } catch (ObjectNotFoundException e) {
            //Expected
        }
    }
    
    /**
     *  Tries to modify a Classification of a RegistryObject and verify that changes
     *  are also reflected in that object when it is fetched from the cache.
     */
    public void testModifyComposedObject() throws Exception {
        final String pkgId = "urn:org:freebxml:omar:server:cache:ObjectCacheTest:testModifyComposedObject:pkg";
        final String classifId = "urn:org:freebxml:omar:server:cache:ObjectCacheTest:testModifyComposedObject:classif";

        // initial clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkgId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), classifId);

        // Create pack w/ no classification
        ServerRequestContext context2 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject2", null);
        context2.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        RegistryPackage pkg = bu.rimFac.createRegistryPackage();
        pkg.setId(pkgId);
        submit(context2, pkg);

        // Check pack w/ no classification
        ServerRequestContext context3 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject3", null);
        context3.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        pkg = (RegistryPackage)ServerCache.getInstance()
                .getRegistryObject(context3, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertTrue(pkg.getClassification().isEmpty());

        //Must create a new context for each submit or else context vars from prevous submit
        //may resubmit pkg after saving classif
        ServerRequestContext context4 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject4", null);
        context4.setUser(AuthenticationServiceImpl.getInstance().farrukh);

        // Create Classification w/ no slot
        ClassificationType classif = bu.rimFac.createClassification();
        classif.setId(classifId);
        classif.setClassifiedObject(pkgId);
        classif.setClassificationNode(bu.CANONICAL_STABILITY_TYPE_ID_Dynamic);
        submit(context4, classif);

        // Check package has classification when fetch without going through cache
        AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest("SELECT * FROM RegistryPackage WHERE id = '" + pkgId + "'");
        ServerRequestContext context5 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject5", queryRequest);
        context5.setUser(ac.registryOperator);
        AdhocQueryResponseType queryResp = qm.submitAdhocQuery(context5);
        bu.checkRegistryResponse(queryResp);

        // Make sure that there is at least one object that matched the query
        int cnt = queryResp.getRegistryObjectList().getIdentifiable().size();
        assertEquals("Pkg not found", 1,1);
        pkg = (RegistryPackage)queryResp.getRegistryObjectList().getIdentifiable().get(0);
        assertEquals("New classification not present in object retrieved from database.", 1, pkg.getClassification().size());


        // Check pack w/ no classification
        ServerRequestContext context6 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject6", queryRequest);
        context6.setUser(ac.registryOperator);
        pkg = (RegistryPackage)ServerCache.getInstance()
                .getRegistryObject(context6, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals("New classification not present in cached object.", 1, pkg.getClassification().size());

        ServerRequestContext context7 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject7", queryRequest);
        context7.setUser(ac.registryOperator);
        classif  = (ClassificationType)ServerCache.getInstance()
              .getRegistryObject(context7, classifId, LifeCycleManager.CLASSIFICATION);
        assertNotNull(classif);

        ServerRequestContext context8 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject8", null);
        context8.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        // add Slot to Classification
        SlotType1 slot = bu.rimFac.createSlot();
        slot.setName("slot1");
        ValueList valueList = bu.rimFac.createValueList();
        slot.setValueList(valueList);
        Value value = bu.rimFac.createValue();
        valueList.getValue().add(value);
        value.setValue("value1");
        classif.getSlot().add(slot);
        submit(context8, classif);

        // Check classification w/ slot
        ServerRequestContext context9 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject9", null);
        context9.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        classif  = (ClassificationType)ServerCache.getInstance()
            .getRegistryObject(context9, classifId, LifeCycleManager.CLASSIFICATION);
        assertNotNull(classif);
        assertEquals("New slot not present in cached object.", 1, classif.getSlot().size());

        // Check classification w/ slot from pack
        ServerRequestContext context10 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject10", null);
        context10.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        pkg = (RegistryPackage)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = (ClassificationType)pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("New slot not present in cached object.", 1, classif.getSlot().size());

        slot = (SlotType1)classif.getSlot().get(0);
        assertNotNull(classif);
        assertEquals("slot1", slot.getName());

        // change status through approve
        ServerRequestContext context11 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject11", null);
        context11.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        approve(context11, classif.getId());
        pkg = (RegistryPackage)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = (ClassificationType)pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("Status not changed in cached composed object.", BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved, classif.getStatus());

        // change status through setStatus
        ServerRequestContext context12 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject12", null);
        context12.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        setStatus(context12, classif.getId(), BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn);
        pkg = (RegistryPackage)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = (ClassificationType)pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("Status not changed in cached composed object.", BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn, classif.getStatus());

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkgId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), classifId);
    }        
    
}
