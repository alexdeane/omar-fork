/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ClassificationTest.java,v 1.12 2007/05/04 18:24:25 farrukh_najmi Exp $
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

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;

/**
 * jUnit Test for Classification
 *
 * @author Farrukh Najmi
 */
public class ClassificationTest extends ClientTest {

    public ClassificationTest(String testName) {
        super(testName);
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

    public static Test suite() throws Exception {
        junit.framework.TestSuite suite = new TestSuite(ClassificationTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new ClassificationTest("testNameChangeNotModifyConcept"));
        return suite;
    }

    /**
     * Test for bug where ClassificationImpl.getName() was calling 
     * InternationalStringImpl.clone on the classifying Concept's name
     * which made a shared copy of LocalizedString for classifying Concept's name.
     * When name was changed on Classification it changes the name of the 
     * Concept causing it to be marked as modified. This resulted in concept
     * incorrectly being saved and causing access control errors if concept was
     * not owned by caller.
     *
     */
    public void testNameChangeNotModifyConcept() throws Exception {
        //Concept MUST have a name
        ConceptImpl concept = (ConceptImpl)bqm.getRegistryObject(
                CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Implements,
                LifeCycleManager.CONCEPT);
        Classification classification = lcm.createClassification(concept);
        assertFalse("Concept modified incorrectly", (concept.isModified()));
        
        InternationalString name = classification.getName();
        LocalizedString ls = (LocalizedString) name.getLocalizedStrings().toArray()[0];
        ls.setValue("New name");
        
        assertFalse("Concept modified incorrectly", (concept.isModified()));        
    }
    
    /**
     * Test to get Classified Object of a classification
     * @throws Exception
     */
    public void testGetClassifiedObject() throws Exception {
        String classificationId = "urn:uuid:cc7278a3-acbf-46e2-9001-c65ad02a0a39";  //id for classification with name Developer
        Classification classi = (Classification)dqm.getRegistryObject(classificationId);
        assertNotNull("Could not find classification. Was Demo DB created?", classi);
        assertNotNull("Could not find classified object", classi.getClassifiedObject());
    }

    /**
     * Tests saving a classification directly and not through its clasified object.
     */
    public void testClassificationOnly() throws Exception {
        String pkg0Id = "urn:freebxml:registry:test:ClassificationTest.testClassificationOnly:pkg0";
        String clf0Id = "urn:freebxml:registry:test:ClassificationTest.testClassificationOnly:clf0";
                
        //Delete in case object is around from past failed run
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(lcm.createKey(pkg0Id));
        try {
            lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        } catch (RegistryException e) {
            //ObjectsNotFoundException may be ok.
        }

        deleteObjects.clear();
        deleteObjects.add(lcm.createKey(clf0Id));
        try {
            lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        } catch (RegistryException e) {
            //ObjectsNotFoundException may be ok.
        }
        
        // Create a package w/o classification
        RegistryPackage pkg0 = lcm.createRegistryPackage(pkg0Id+":name");
        pkg0.getKey().setId(pkg0Id);
        Collection saveObjects = new ArrayList();
        saveObjects.add(pkg0);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving pkg0 failed.", br);                

        //Create a classification for that pkg
        ClassificationScheme dunsScheme = (ClassificationScheme)dqm.getRegistryObject(DUNS_CLASSIFICATION_SCHEME);
        Classification clf0 = (Classification)lcm.createClassification(dunsScheme, "", clf0Id+":value");
        clf0.getKey().setId(clf0Id);
        //Manually set the classified obj
        clf0.setClassifiedObject(pkg0);
        
        //Save the classification separately (Should also save the package)
        saveObjects.clear();
        saveObjects.add(clf0);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving clf0 failed.", br);                

        //Now find pkg0 by DUNS classification
        ArrayList classifications = new ArrayList();
        classifications.add(clf0);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg0 failed.", br);
        assertTrue("pkg0 was not saved", br.getCollection().contains(pkg0));
        pkg0 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg0 does not have correct number of Classifications", 1, pkg0.getClassifications().size());
        
        //Modify the classification only, save it separately
        clf0.setName(lcm.createInternationalString(clf0+":name"));
        saveObjects.clear();
        saveObjects.add(clf0);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving clf0 failed.", br);                
        
        //Now find pkg0 by DUNS classification, again
        classifications.clear();
        classifications.add(clf0);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg0 failed.", br);
        assertTrue("pkg0 was not saved", br.getCollection().contains(pkg0));
        pkg0 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg0 does not have correct number of Classifications", 1, pkg0.getClassifications().size());
        assertEquals("clf0 has not been saved", clf0+":name",
                ((Classification)pkg0.getClassifications().toArray()[0]).getName().getValue());
                        
        //Now delete pkg0 and clf0
        deleteObjects = new ArrayList();
        deleteObjects.add(pkg0.getKey());
        deleteObjects.add(clf0.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg0=(RegistryPackage)dqm.getRegistryObject(pkg0Id);
        assertNull("pkg0 was not deleted", pkg0);
    }

    /**
     * Tests creating an object with an external classification and then
     * finding the object classified by that external Classification.
     * External Classifications use an external ClassificationScheme (a scheme
     * with no ClassificationNode) and a value.
     *
     */
    public void testExternalClassification() throws Exception {
        String pkg1Id = "urn:freebxml:registry:test:ClassificationTest.testExternalClassification:pkg1";

        RegistryPackage pkg1 = lcm.createRegistryPackage(pkg1Id);
        pkg1.getKey().setId(pkg1Id);
        
        //delete in case object is around from past failed run
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(pkg1.getKey());
        try {
            lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        } catch (RegistryException e) {
            //ObjectsNotFoundException may be ok.
        }
        
        //Add a Classification as a true composed object
        ClassificationScheme dunsScheme = (ClassificationScheme)dqm.getRegistryObject(DUNS_CLASSIFICATION_SCHEME);
        Classification dunsClassification = (Classification)lcm.createClassification(dunsScheme, "", pkg1Id);

        pkg1.addClassification(dunsClassification);
                
        //Now save pkg1 and its DUNS Classification
        Collection saveObjects = new ArrayList();
        saveObjects.add(pkg1);
        BulkResponse br = lcm.saveObjects(saveObjects);
        assertResponseSuccess("Saving pkg1 failed.", br);                
        
        //Now find pkg1 by DUNS classification
        ArrayList classifications = new ArrayList();
        classifications.add(dunsClassification);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg1 failed.", br);

        assertTrue("pkg1 was not saved", br.getCollection().contains(pkg1));
        pkg1 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg1 does not have correct number of Classifications", 1, pkg1.getClassifications().size());
                        
        //Now delete pkg1
        deleteObjects = new ArrayList();
        deleteObjects.add(pkg1.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);

    }

    public void testNestedClassificationUpdate() throws Exception {

        RegistryPackage pkg1 = lcm.createRegistryPackage("ClassificationTest.pkg1");
        String pkg1Id = pkg1.getKey().getId();        
        
        //Add a Classification as a true composed object
        Concept xacmlConcept = (Concept)dqm.getRegistryObject(BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML, LifeCycleManager.CONCEPT);
        assertNotNull("Unable to read xacmlConcept", xacmlConcept);

        Classification parentClassification = (Classification)lcm.createClassification(xacmlConcept);
        Classification childClassification = (Classification)lcm.createClassification(xacmlConcept);
        parentClassification.addClassification(childClassification);

        pkg1.addClassification(parentClassification);
                
        //Now save pkg1 and its nested Classification
        Collection saveObjects = new ArrayList();
        saveObjects.add(pkg1);
        BulkResponse response = lcm.saveObjects(saveObjects);
        
        //Now read back pkg1 to verify that it was saved
        pkg1 = (RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNotNull("pkg1 was not saved", pkg1);
        assertEquals("pkg1 does not have correct number of Classifications", 1, pkg1.getClassifications().size());

        parentClassification = (Classification)(pkg1.getClassifications().toArray())[0];
        assertEquals("parentClassification does not have correct number of child Classifications", 1, parentClassification.getClassifications().size());
        
                
        //Now delete pkg1
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(pkg1.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);

    }
}