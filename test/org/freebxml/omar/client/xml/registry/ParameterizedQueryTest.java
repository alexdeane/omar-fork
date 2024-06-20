/*
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/ParameterizedQueryTest.java,v 1.11 2006/03/13 19:50:14 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Concept;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;

/**
 * jUnit Test for QueryManager
 *
 * @author Farrukh Najmi
 */
public class ParameterizedQueryTest extends ClientTest {
    
    public ParameterizedQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ParameterizedQueryTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new ParameterizedQueryTest("testArbitraryQuery"));
        return suite;
    }
    

    /**
     * Test arbitrary query.
     */
    public void testArbitraryQuery() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery;
        Map queryParams = new HashMap();
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        queryParams.put("$query", "SELECT * FROM ClassificationScheme");
        Collection registryObjects = executeQuery(queryId, queryParams);
        assertTrue(registryObjects.size() > 0);
    }    
    
    public void testParameterizedQuery() throws Exception {
        /* TODO: This test getting OutOfMemory error as it fetches all objects for 
         each ObjectType which does not currently scale.
         * Currently, object types scaled down to a sample: Service, Service Binding
         * and Organization
         */
        Collection objectTypes = getObjectTypes();
        Map parameters = new HashMap();
        String paramQueryURN = BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID;
        parameters.put(paramQueryURN, "urn:freebxml:registry:query:BusinessQuery");
        testObjectTypes(parameters, objectTypes);
    }
    
    private void testObjectTypes(Map parameters, Collection objectTypes) 
        throws InvalidRequestException, JAXRException {
        Iterator itr = objectTypes.iterator();
        while (itr.hasNext()) {
            String objectType = (String)itr.next();
            parameters.put("$objectTypePath", objectType);
            Query query = ((DeclarativeQueryManagerImpl)dqm)
                .createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse bResponse = ((DeclarativeQueryManagerImpl)dqm)
                .executeQuery(query, parameters);
            Collection registryObjects = bResponse.getCollection();
            Iterator roItr = registryObjects.iterator();
            Object registryObject = null;
            if (roItr.hasNext()) {
                registryObject = roItr.next();
            }
            assertNotNull("Query returned no objects of type "+objectType, registryObject);
        }
    }
    
    /* This method will return a Collection of objectTypes that are found in
     * the minDB installation
     */
    private Collection getObjectTypes() {
        ArrayList list = new ArrayList();
//        list.add("RegistryObject");
        list.add("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/Service");
        list.add("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ServiceBinding");
//        list.add("Organization");
//        list.add("User");
//        list.add("ClassificationScheme");
//        list.add("ClassificationNode");
//        list.add("ExternalLink");
//        list.add("Association");
//        list.add("ExtrinsicObject");
//        list.add("AdhocQuery");
        return list;
    }

    private Collection getAllObjectTypes() throws JAXRException {   
        Collection concepts = bqm.findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject%");
        int size = concepts.size();
        ArrayList list = new ArrayList(size);
        Iterator iter = concepts.iterator();
        while (iter.hasNext()) {
            Concept concept = (Concept) iter.next();
            String objectType = concept.getValue();
            list.add(objectType);
        }
        return list;
    }
    
    public static void main(String[] args) {
	System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
