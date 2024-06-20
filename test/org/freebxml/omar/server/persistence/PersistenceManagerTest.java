/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/persistence/PersistenceManagerTest.java,v 1.3 2007/05/24 18:51:08 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.freebxml.omar.server.persistence.rdb.SQLPersistenceManagerImpl;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;


/**
 * @author Tony Graham
 */
public class PersistenceManagerTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected UserType registryOperator;
    protected ServerRequestContext context;

    /**
     * Constructor for PersistenceManagerTest
     *
     * @param name name of the test
     */
    public PersistenceManagerTest(String name) {
        super(name);

        try {
            context = new ServerRequestContext("PersistenceManagerTest:PersistenceManagerTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, ac.ALIAS_REGISTRY_OPERATOR);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            fail("Couldn't initialize objects:\n" + baos);
        }
    }
    
    /**
     * Tests that getRegistryObjectMatchingQuery method 
     * returns first matched object when multiple objects match.
     */
    public void testGetRegistryObjectMatchingQuery() throws Exception {
        final String contextId = "testGetRegistryObjectMatchingQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        try {
            RegistryPackage pkg = (RegistryPackage)SQLPersistenceManagerImpl.getInstance().getRegistryObjectMatchingQuery(context, "SELECT p.* FROM RegistryPackage p", null, "RegistryPackage");
            assertTrue("Incorrectly returned null", pkg != null);
        } finally {
        }                
    }        

    /**
     * Tests getOwnersMap() after submission of an CPPA ExtrinsicObject to PersistenceManager and RepositoryManager
     *
     * @throws Exception if an error occurs
     */
    public void testGetOwnersMap_PMInsertCPPA() throws Exception {
        String eoId = org.freebxml.omar.common.Utility.getInstance().createId();

        ExtrinsicObjectType eo = createExtrinsicObject(eoId,
                "testGetOwnersMap_PMInsertCPPA", bu.CPP_CLASSIFICATION_NODE_ID);
        RepositoryItem ri = createCPPRepositoryItem(eoId);

        context = new ServerRequestContext("PersistenceManagerTest.testGetOwnersMap_PMInsertCPPA", null);
        context.setUser(registryOperator);

        try {
            ArrayList eoList = new ArrayList();
            eoList.add(eo);
            context.getRepositoryItemsMap().put(eoId, ri);
            pm.insert(context, eoList);
            rm.insert(context, ri);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }

        context.commit();

        List ids = new ArrayList();
        ids.add(eoId);

        HashMap ownersMap = pm.getOwnersMap(context, ids);
        String ownerId = (String) ownersMap.get(eoId);

        assertNotNull("ownerId of submitted RegistryObject should not be null.",
            ownerId);
        assertEquals("Owner should be RegistryOperator",
            ac.ALIAS_REGISTRY_OPERATOR, ownerId);
    }

    /**
     * Tests getOwnersMap() after submission of an CPPA ExtrinsicObject to LifeCycleManager
     *
     * @throws Exception if an error occurs
     */
    public void testGetOwnersMap_LCMSubmitCPPA() throws Exception {
        String eoId = org.freebxml.omar.common.Utility.getInstance().createId();

        ExtrinsicObjectType eo = createExtrinsicObject(eoId,
                "testGetOwnersMap_LCMSubmitCPPA", bu.CPP_CLASSIFICATION_NODE_ID);
        RepositoryItem ri = createCPPRepositoryItem(eoId);

        ArrayList objects = new ArrayList();
        objects.add(eo);

        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        RegistryObjectListType roList = bu.rimFac.createRegistryObjectList();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);

        HashMap idToRepositoryItemMap = new HashMap();
        idToRepositoryItemMap.put(eoId, ri);

        ServerRequestContext context = new ServerRequestContext("PersistenceManagerTest:testGetOwnersMap_LCMSubmitCPPA", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponse resp = lcm.submitObjects(context);
        BindingUtility.getInstance().checkRegistryResponse(resp);

        context = new ServerRequestContext("PersistenceManagerTest.testGetOwnersMap_LCMSubmitCPPA",
                null);
        context.setUser(registryOperator);

        List ids = new ArrayList();
        ids.add(eoId);

        HashMap ownersMap = pm.getOwnersMap(context, ids);
        String ownerId = (String) ownersMap.get(eoId);

        assertNotNull("ownerId of submitted RegistryObject should not be null.",
            ownerId);
        assertEquals("Owner should be RegistryOperator",
            ac.ALIAS_REGISTRY_OPERATOR, ownerId);
    }

    /**
     * Creates an ExtrinsicObject of specific type.
     *
     * @return an <code>ExtrinsicObjectType</code> value
     * @param id id of created ExtrinsicObject
     * @param desc description to add to generated ExtrinsicObject
     * @param objectType id of classification type of created ExtrinsicObject
     * @exception Exception if an error occurs
     */
    ExtrinsicObjectType createExtrinsicObject(String id, String desc,
        String objectType) throws Exception {
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();

        if (desc != null) {
            eo.setDescription(bu.createInternationalStringType(desc));
        }

        eo.setId(id);
        eo.setObjectType(objectType);
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        return eo;
    }

    /**
     *
     * @return
     */
    public static Test suite() {
        return new TestSuite(PersistenceManagerTest.class);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
