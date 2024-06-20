/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/ConceptTest.java,v 1.2 2005/11/21 04:28:19 farrukh_najmi Exp $
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
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;

/**
 * jUnit Test for ClassificationSchemes
 *
 * @author Based on test contributed by Steve Allman
 */
public class ConceptTest extends ClientTest {
    
    
    public ConceptTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ConceptTest.class);
        return suite;
    }
    
    /**
     * Tests publishing a new Concep with no parent and then
     * retrieving it successfully.
     *
     */
    public void testAddConceptWithoutParent() throws JAXRException {
    }
    
    public void testGetDescendantConcepts() throws JAXRException {
        String path = 
           "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject";
        Concept concept = bqm.findConceptByPath(path);
       
        int numDescendants = concept.getDescendantConcepts().size();
        
        int numChildren = concept.getChildrenConcepts().size();       
        
        assertTrue("There are "+numChildren+" children, but the number of "+
            "descendents is "+numDescendants, 
            numChildren > 0 && numDescendants > 0);
        
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
