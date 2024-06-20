/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/QueryManagerTest.java,v 1.9 2006/07/29 05:53:38 dougb62 Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.User;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.oasis.ebxml.registry.bindings.query.ReturnType;


/**
 * jUnit Test for QueryManager
 *
 * @author Farrukh Najmi
 */
public class QueryManagerTest extends ClientTest {

    public QueryManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(QueryManagerTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new QueryManagerTest("testResponseOption"));
        return suite;
    }

    /**
     * Tests new features in DeclarativeQueryManagerImpl where transient slots that can be used to specify ResponseOption.
     */
    public void testResponseOption_LeafClass() throws Exception {

        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        queryParams.put("$id", "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        queryParams.put("$tableName", LifeCycleManager.EXTRINSIC_OBJECT);

        //First fetch defaultACP without RepositoryItem
        String returnType = ReturnType._LEAF_CLASS;
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);

        Collection registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof ExtrinsicObject));

        ExtrinsicObject eo = (ExtrinsicObject)obj;
        DataHandler repositoryItem = ((ExtrinsicObjectImpl)eo).getRepositoryItemInternal();
        assertTrue("Fetched repositoryItem when should not have done so", (repositoryItem == null));
    }

    public void testResponseOption_LeafClassWithRI() throws Exception {

        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        queryParams.put("$id", "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        queryParams.put("$tableName", LifeCycleManager.EXTRINSIC_OBJECT);

        //Now fetch defaultACP with RepositoryItem
        String returnType = ReturnType._LEAF_CLASS_WITH_REPOSITORY_ITEM;
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);

        Collection registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof ExtrinsicObject));

        ExtrinsicObject eo = (ExtrinsicObject)obj;
        DataHandler repositoryItem = ((ExtrinsicObjectImpl)eo).getRepositoryItemInternal();
        assertTrue("Did not fetch repositoryItem when should have done so", (repositoryItem != null));

    }

    public void testResponseOption_NoComposedObjects() throws Exception {

        // Use CanonicalXMLCatalogingService to check for slots
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        queryParams.put("$id", CanonicalConstants.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_ID);
        queryParams.put("$tableName", LifeCycleManager.SERVICE);
        // set the return composed objects to true
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS, "false");

        Collection registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof Service));

        Service s = (Service)obj;
        assertTrue("No composed objects (slots) expected. Maybe object was loaded from server cache.", s.getSlots().size() == 0);
    }

    public void testResponseOption_ComposedObjects() throws Exception {

        // Use CanonicalXMLCatalogingService to check for slots
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        queryParams.put("$id", CanonicalConstants.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_ID);
        queryParams.put("$tableName", LifeCycleManager.SERVICE);
        // set the return composed objects to true
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS, "true");

        Collection registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof Service));

        Service s = (Service)obj;
        assertTrue("No composed objects (slots)", s.getSlots().size() > 0);
    }

    public void testGetRegistryObjectsByType() throws Exception {

        User user = dqm.getCallersUser();
        BulkResponse br = dqm.getRegistryObjects(LifeCycleManager.USER);
        assertResponseSuccess("dqm.getRegistryObjects failed.", br);

        assertTrue("callers user not in dqm.getRegistryObjects()", br.getCollection().contains(user));
    }

    public void testGetRegistryObjects() throws Exception {

        User user = dqm.getCallersUser();
        BulkResponse br = dqm.getRegistryObjects();
        assertResponseSuccess("dqm.getRegistryObjects failed.", br);

        assertTrue("callers user not in dqm.getRegistryObjects()", br.getCollection().contains(user));
    }

    public void testGetCallersUser() throws Exception {
        User user = ((DeclarativeQueryManagerImpl)dqm).getCallersUser();
        assertNotNull("Callers user not found.", user);
    }

    /**
     * Tests bug fix where AuthorizationServiceImpl was throwing exception
     * when a non RegistryAdmin user tried to read the default ACP
     */
    public void testReadDefaultACPAsNonRegistryAdmin() throws Exception {
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, "SELECT * FROM ExtrinsicObject WHERE objectType = '" +
            BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML + "'");
        BulkResponse br = dqm.executeQuery(query);
        assertTrue("Query matching all XACML Policies failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
    }

}
