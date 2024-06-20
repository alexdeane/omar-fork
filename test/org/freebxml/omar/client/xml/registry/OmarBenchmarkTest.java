/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/OmarBenchmarkTest.java,v 1.11 2006/10/26 22:18:33 psterk Exp $
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.CanonicalSchemes;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;

/**
 * jUnit Test used as micro benchmark for omar performance.
 *
 * testXXXQuery Notes:
 * 1. Parameterized queries should use different parameters in each invocation
 * otherwise a good DBMS impl will cache the query plan even when no PreparedStataments
 * are used making it hard to measure performance improvements of PreparedStataments
 *
 * @author Farrukh Najmi
 */
public class OmarBenchmarkTest extends ClientTest {
    
    static int count=0;
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(OmarBenchmarkTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new OmarBenchmarkTest("testGetRegistryObject"));
        //suite.addTest(new OmarBenchmarkTest("testPublishOneExtrinsicObject"));
        //suite.addTest(new OmarBenchmarkTest("testPublishOneExtrinsicObjectWithSlots"));
        //suite.addTest(new OmarBenchmarkTest("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem"));
        //suite.addTest(new OmarBenchmarkTest("testPublishOneExtrinsicObjectWithSlotsClassifications"));
        //suite.addTest(new OmarBenchmarkTest("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem"));
        
        /*
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        suite.addTest(new OmarBenchmarkTest("testQueryRegistryObjectById"));
        for (int i=0; i<1; i++) {
            //suite.addTest(new OmarBenchmarkTest("testQueryAuditTrailForRegistryObject"));
            suite.addTest(new OmarBenchmarkTest("testQueryBasic"));
        }
         */
        return suite;
    }
    
    
    public OmarBenchmarkTest(String testName) {
        super(testName);        
    }
    
    public void testPublishOneExtrinsicObject() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObject: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObject", false, false, false);
        ArrayList saveObjects = new ArrayList();        
        saveObjects.add(eo);
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObject: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlots() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlots: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlots", true, false, false);        
        ArrayList saveObjects = new ArrayList();        
        saveObjects.add(eo);
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlots: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlotsClassifications() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassifications: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlotsClassifications", true, true, false);
        ArrayList saveObjects = new ArrayList();        
        saveObjects.add(eo);
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassifications: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem", true, true, true);        
        ArrayList saveObjects = new ArrayList();        
        saveObjects.add(eo);
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem: entered");
        ArrayList saveObjects = new ArrayList();        
        
        int eoCount = 10;
        for (int i=0; i<eoCount; i++) {
            ExtrinsicObject eo = createExtrinsicObject("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem", true, true, true);        
            saveObjects.add(eo);
        }
        
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem: exit elapedTimeMillis=" + (endTime-startTime));
    }
        
    private ExtrinsicObject createExtrinsicObject(
            String nameSuffix, 
            boolean addSlots, 
            boolean addClassifications, 
            boolean addRepositoryItem) throws Exception {
        
        ExtrinsicObject eo = lcm.createExtrinsicObject((javax.activation.DataHandler)null);
        InternationalString is = lcm.createInternationalString("OmarBenchmarkTest." + nameSuffix);
        eo.setName(is);
        
        if (addSlots) {
            ArrayList slots = new ArrayList();
            int slotCnt = 10;
            for (int i=0; i<slotCnt; i++) {
                String slotName = "slot" + i;
                Slot slot = lcm.createSlot(slotName, slotName + "Value1", null);
                slots.add(slot);
            }
            eo.addSlots(slots);
        }
        
        if (addClassifications) {
            ArrayList classifictions = new ArrayList();
            ClassificationScheme associationTypeScheme = (ClassificationScheme)bqm.getRegistryObject(CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType);
            Collection concepts = associationTypeScheme.getChildrenConcepts();
            Iterator iter = concepts.iterator();
            while (iter.hasNext()) {
                Concept concept = (Concept)iter.next();;
                Classification c = lcm.createClassification(concept);
                classifictions.add(c);
            }
            eo.addClassifications(classifictions);
        }
        
        if (addRepositoryItem) {
            //Add repository item: current size ~5KB
            String riResourceName = "/resources/StandaloneTest.wsdl";
            URL riResourceUrl = getClass().getResource(riResourceName);
            assertNotNull("Missing test resource: " + riResourceName, riResourceUrl);
            File repositoryItemFile = new File(riResourceUrl.getFile());
            assertTrue("Missing test resource: " + riResourceUrl.getFile(), repositoryItemFile.canRead());
            DataHandler repositoryItem = new DataHandler(new FileDataSource(repositoryItemFile));
            eo.setRepositoryItem(repositoryItem);
        }
        
        return eo;
    }
    
    public void testUpdate() throws Exception {
    }
    
    /**
     * Gets a RegistryObject by id using qm.getRegistryObject(...)
     */
    public void testGetRegistryObject() throws Exception {
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        RegistryObject existingRO = bqm.getRegistryObject(CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
        RegistryObject nonExistingRO = bqm.getRegistryObject(id);        
    }    
    
    /**
     * Gets a RegistryObject by id with table being RegistryObject.
     */
    public void testQueryRegistryObjectById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        queryParams.put("$id", id);
        queryParams.put("$tableName", "RegistryObject");
        try {
            Collection registryObjects = executeQuery(queryId, queryParams);
        } catch (ObjectNotFoundException e) {
            //These are being thrown incorrectly. Fix qm.getRegistryObject() to not 
            //handle this exception and return a null for backward compatibility
        }
    }
    
    /**
     * Gets a RegistryObject by id with table being a leaf RIM class
     * Served from cache so will be fast if cache is primed.
     */
    public void testQueryLeafObjectById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map queryParams = new HashMap();
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        queryParams.put("$id", id);
        queryParams.put("$tableName", "Service");
        try {
            Collection registryObjects = executeQuery(queryId, queryParams);
        } catch (ObjectNotFoundException e) {
            //These are being thrown incorrectly. Fix qm.getRegistryObject() to not 
            //handle this exception and return a null for backward compatibility
        }
    }
    
    /**
     * Gets audit trail for a RegistryObject.
     */
    public void testQueryAuditTrailForRegistryObject() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetAuditTrailForRegistryObject;
        Map queryParams = new HashMap();
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        queryParams.put("$lid", id);
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get scheme by id. 
     * Served from cache so will be fast if cache is primed.
     */
    public void testQuerySchemeById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetClassificationSchemesById;
        Map queryParams = new HashMap();
        queryParams.put("$idPattern", CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get User for caller.
     */
    public void testQueryCallersUser() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetCallersUser;
        Map queryParams = new HashMap();
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get RegistryObjects owned by caller. 
     * Potentially very time consuming.
     */
    public void testQueryCallersObjects() throws Exception {
        /* Need to implemented this query
        SAtring queryId = CanonicalConstants.CANONICAL_QUERY_GetCallersObjects;
        Map queryParams = new HashMap();
        Collection registryObjects = executeQuery(queryId, queryParams);
         */
    }
    
    /**
     *  Gets objects matching specified name, description, status, classifications.
     */
    public void testQueryBasic() throws Exception {
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        String queryId = CanonicalConstants.CANONICAL_QUERY_BasicQuery;
        Map queryParams = new HashMap();
        queryParams.put("$objectTypePath", "%/RegistryObject");
        queryParams.put("$name", id);
        queryParams.put("$classificationPath1", id);
        queryParams.put("$description", id+"%");
        queryParams.put("$status", "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:StatusType/Submitted");
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * WSDLDiscoveryQuery: Find WSDL files with $targetNamespace matching "%urn:goes:here"
     */
    public void testQueryWsdl() throws Exception {
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        String queryId = org.freebxml.omar.common.profile.ws.wsdl.CanonicalConstants.CANONICAL_QUERY_WSDL_DISCOVERY;
        Map queryParams = new HashMap();
        queryParams.put("$targetNamespace", id);
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * WSDL ServiceDiscoveryQuery: Find WSDL Service with $service.name matching "%regrep%"
     */
    public void testQueryWsdlService() throws Exception {
        String id = org.freebxml.omar.common.Utility.getInstance().createId();
        String queryId = org.freebxml.omar.common.profile.ws.wsdl.CanonicalConstants.CANONICAL_QUERY_SERVICE_DISCOVERY;
        Map queryParams = new HashMap();
        queryParams.put("$service.name", id);
        queryParams.put("$considerPort", "1");
        queryParams.put("$considerBinding", "0");
        queryParams.put("$considerPortType", "0");
        Collection registryObjects = executeQuery(queryId, queryParams);
    }
    
    public void testDelete() throws Exception {
        //Find and delete all objects created by this test
        //All objects created by this test are assumed to have name prefix OmarBenchmarkTest.
        //Also need to delete AuditableEvents for these objects somehow in future to have zero growth.
        String namePattern = "OmarBenchmarkTest%";

        org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl ahq = 
                    (org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl)lcm.createObject("AdhocQuery");
        ahq.setString("SELECT ro.id FROM RegistryObject ro, Name_ nm WHERE nm.value LIKE '" + namePattern + "' AND ro.id = nm.parent");
        ArrayList keys = new ArrayList();
        
        try {
            //Now do the delete
            BulkResponse br = ((LifeCycleManagerImpl)lcm).deleteObjects(keys, ahq, null, null);
        } catch (Exception e) {
            //Temporary hack to workaround concurrency related bug in server here.
            //Otherwise japex just hangs on the error.
        }
    }
    
}
