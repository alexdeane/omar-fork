/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ClassificationSchemeTest.java,v 1.7 2006/06/30 20:05:03 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.CanonicalConstants;

/**
 * jUnit Test for ClassificationSchemes
 *
 * @author Based on test contributed by Steve Allman
 */
public class ClassificationSchemeTest extends ClientTest {
    
    
    public ClassificationSchemeTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClassificationSchemeTest.class);
        return suite;
    }
    
    /**
     * Tests: Server emits warnings when finding user-created ClassificationScheme
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6444687
     *
     * 
     */
    public void testExternalSchemeStatus() throws Exception {
      String extSchemeId = "urn:freebxml:registry:test:client:ClassificationSchemeTest:testExternalScheme:externalScheme";
            
      try {
          deleteIfExist(extSchemeId);
          ClassificationScheme extScheme = lcm.createClassificationScheme("NASDAQ", 
              "OTC Stock Exchange");
          Key extSchemeKey = lcm.createKey(extSchemeId);
          extScheme.setKey(extSchemeKey);
          BulkResponse response = lcm.saveObjects(Collections.singletonList(extScheme));
          assertResponseSuccess("External Scheme save failed.", response);      

          extScheme = (ClassificationScheme)dqm.getRegistryObject(extSchemeId, lcm.CLASSIFICATION_SCHEME);
          assertNotNull("Could not read back scheme.", extScheme);
      } finally {
        deleteIfExist(extSchemeId);
      }
    }
    
    /**
     * Finds and displays a classification scheme and a hierarchy of
     * concepts. First it displays all the descendant concepts of the 
     * classification scheme. Then it displays the concept hierarchy of 
     * the classification scheme.
     *
     * @param searchString  the classification scheme name
     */
    public void testGetDescendantConcepts6316600() throws Exception {
        
        ClassificationScheme scheme = lcm.createClassificationScheme("GeographyTestScheme", null);

        // create a children of scheme
        Concept usConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        usConcept.setName(lcm.createInternationalString("United States"));
        usConcept.setValue("US");

        Concept canConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        canConcept.setName(lcm.createInternationalString("Canada"));
        canConcept.setValue("CAN");

        Collection childConcepts = new ArrayList();        
        childConcepts.add(usConcept);
        childConcepts.add(canConcept);
        scheme.addChildConcepts(childConcepts);
        
        // create grand children via US child concept
        Concept akConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        akConcept.setName(lcm.createInternationalString("Alaska"));
        akConcept.setValue("US-AK");

        Concept caConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        caConcept.setName(lcm.createInternationalString("California"));
        caConcept.setValue("US-CA");

        // add children to US Concept so we will have descendents
        childConcepts = new ArrayList();
        childConcepts.add(caConcept);
        childConcepts.add(akConcept);
        usConcept.addChildConcepts(childConcepts);

        assertEquals(2, scheme.getChildConceptCount());           
        Collection concepts = scheme.getDescendantConcepts();
        //System.err.println("scheme.getDescendantConcepts() = " + concepts);
        assertNotNull(concepts);
        assertEquals(4, concepts.size());
        
        concepts = usConcept.getDescendantConcepts();
        //System.err.println("usConcept.getDescendantConcepts() = " + concepts);
        assertNotNull(concepts);
        assertEquals(2, concepts.size());
    }
    
    
    /** Test submit of a Service */
    public void testSubmit() throws Exception {
        
        ArrayList objects = new ArrayList();
        
        // create Class scheme
        ClassificationScheme testScheme = lcm.createClassificationScheme("Test Scheme", "DC");
        String schemeId = testScheme.getKey().getId();
        objects.add(testScheme);
        
        // create first child concept
        Concept node1 = lcm.createConcept(testScheme, "1", "1");
        String node1Id = node1.getKey().getId();
        testScheme.addChildConcept(node1);
        //objects.add(node1);
        
        // create second child concept
        Concept node1_2 = lcm.createConcept(testScheme, "2", "2");
        String node1_2Id = node1_2.getKey().getId();
        node1.addChildConcept(node1_2);        
        //objects.add(node1_2);
                
        // create second third concept
        // This is where the error arises: 2 levels of Concepts are fine, 3 causes problems
        Concept node1_2_3 = lcm.createConcept(testScheme, "3", "3");
        String node1_2_3Id = node1_2_3.getKey().getId();
        node1_2.addChildConcept(node1_2_3);
        objects.add(node1_2_3);
        
        BulkResponse resp = lcm.saveObjects(objects);        
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);

        node1_2_3 = (Concept)dqm.getRegistryObject(node1_2_3Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2_3", node1_2_3);
    }
    
    /** Test submit of a Service */
    public void testConceptAdding() throws Exception {
        
        ArrayList objects = new ArrayList();
        
        // create Class scheme
        ClassificationScheme testScheme = lcm.createClassificationScheme("Test Scheme", "DC1");
        String schemeId = testScheme.getKey().getId();
        
        // create first child concept
        Concept node1 = lcm.createConcept(testScheme, "1", "1");
        String node1Id = node1.getKey().getId();
        testScheme.addChildConcept(node1);
        
        // create second child concept
        Concept node1_2 = lcm.createConcept(testScheme, "2", "2");
        String node1_2Id = node1_2.getKey().getId();
        testScheme.addChildConcept(node1);     
                
        objects.add(testScheme);
        BulkResponse resp = lcm.saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);
        
        int childCount = testScheme.getChildConceptCount();
        assertEquals(2, childCount);
        
        Collection concepts = testScheme.getChildrenConcepts();
        assertEquals(2, concepts.size());
        
        // create second child concept
        Concept node1_3 = lcm.createConcept(testScheme, "3", "3");
        String node1_3Id = node1_3.getKey().getId();
        testScheme.addChildConcept(node1_3);     

        objects = new ArrayList();
        objects.add(testScheme);
        resp = lcm.saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);
        
        node1_3 = (Concept)dqm.getRegistryObject(node1_3Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_3", node1_3);

        childCount = testScheme.getChildConceptCount();
        assertEquals(3, childCount);
        
        concepts = testScheme.getChildrenConcepts();
        assertEquals(3, concepts.size());
        
    }

    public void testGetDescendantConcepts() throws JAXRException {
        String qString = "AssociationType";
        ClassificationScheme scheme = 
            bqm.findClassificationSchemeByName(null, qString);
       
        int numDescendants = scheme.getDescendantConcepts().size();
        
        int numChildren = scheme.getChildrenConcepts().size();       
        
        assertTrue("There are "+numChildren+" children, but the number of "+
            "descendents is "+numDescendants, 
            numChildren > 0 && numDescendants > 0);
        
    }
            
    /* 
     * This test checks that after updating a class scheme, any child concepts
     * are not dropped. 
     */
    public void testUpdateClassScheme() throws Exception {
        System.out.println("\ntestUpdateClassScheme");
        ArrayList objects = new ArrayList();
        ClassificationScheme scheme = null;
        Concept node = null;
        try {           
            scheme = lcm.createClassificationScheme("LifeCycleManagerTest.updateTest1", "LifeCycleManagerTest.updateTest1");            
            String schemeId = scheme.getKey().getId();
            objects.add(scheme);
            //Add a child Concept as pseudo-composed object
            node = lcm.createConcept(scheme, "LifeCycleManagerTest.testNode1", "LifeCycleManagerTest.testNode1");
            String nodeId = node.getKey().getId();
            scheme.addChildConcept(node);
            HashMap slotsMap = new HashMap();
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION, "true");
            BulkResponse br = lcm.saveObjects(objects, slotsMap);
            assertResponseSuccess(br);
            // check that the Concept has been saved
            node = (Concept)bqm.getRegistryObject(nodeId, LifeCycleManager.CONCEPT);
            assertNotNull(node);
            // get Scheme from database
            ClassificationScheme scheme2 = (ClassificationScheme)bqm.getRegistryObject(schemeId, LifeCycleManager.CLASSIFICATION_SCHEME);
            assertNotNull(scheme2);
            
            // update the ClassificationScheme
            InternationalString is = lcm.createInternationalString("LifeCycleManagerTest.testNode1.new");
            scheme2.setName(is);
            objects.clear();
            objects.add(scheme2);
            br = lcm.saveObjects(objects, slotsMap);
            assertResponseSuccess(br);
            // retrieve Class Scheme
            node = (Concept)bqm.getRegistryObject(nodeId, LifeCycleManager.CONCEPT);
            // check that the node is still there
            assertNotNull(node);
        } finally {
            objects.clear();
            if (scheme != null) {
                objects.add(scheme.getKey());
            }
            if (node != null) {
                objects.add(node.getKey());
            }
            BulkResponse br = lcm.deleteObjects(objects);
        }
    }
    
    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
