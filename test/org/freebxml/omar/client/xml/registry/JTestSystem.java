/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/JTestSystem.java,v 1.7 2005/01/02 22:33:31 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;

/**
 *
 * @author  mzaremba
 */
public class JTestSystem extends ClientTest {

  
  /** Creates a new instance of JTestSystem */
  public JTestSystem(String testMethod) 
  {
    super(testMethod);
  }
  
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JTestSystem.class);
        return suite;
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
    
  /*
   * Test browsing for Classification Schemes (AssociationType used in this test) 
   * and for Classification Concepts
   */
  public void testClassificationSchemes() throws Exception {
    ArrayList al = new ArrayList();
    al.add("Asso%");
    BulkResponse br = bqm.findClassificationSchemes(null, al, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("AssociationType classification schemes could not be found");
    }
    Collection collection = br.getCollection();
    Iterator i = collection.iterator();
    ClassificationScheme cs = (ClassificationScheme)i.next();
    assertEquals("Did not find expected ClassificationScheme on name match.", "AssociationType", cs.getName().getValue());
    
    String[] children = new String[] {"AffiliatedWith", "EmployeeOf", "MemberOf", 
                                      "RelatedTo", "HasFederationMember", "HasMember", 
                                      "ExternallyLinks", "Contains", "EquivalentTo", 
                                      "Extends", "Implements", "InstanceOf", 
                                      "Supersedes", "Uses", "Replaces", "SubmitterOf",
                                      "ResponsibleFor", "OwnerOf", "OffersService",
                                      "ContentManagementServiceFor", "InvocationControlFileFor", 
                                      "AccessControlPolicyFor"};
    ArrayList childrenList = new ArrayList();
    for (int index =0; index < children.length; index++) {
        childrenList.add(children[index]);
    }
    
    for (Iterator it = childrenList.iterator(); it.hasNext(); ) {
        String conceptCode = (String)it.next();
        Concept con = bqm.findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType + "/" + conceptCode);
        assertNotNull("Cannot find concept: " + con);
    }
  }
  
  public void testFindConcept() throws Exception {
    String[] objectTypes = new String[] {
                                         "RegistryObject", "AdhocQuery", "Association", 
                                         "AuditableEvent", "Classification", "ClassificationNode",
                                         "ExternalIdentifier", "ExternalLink", "Organization",
                                         "ServiceBinding", "SpecificationLink", "Subscription", "User",
                                         "ClassificationScheme", "Federation", "Registry", "RegistryPackage",
                                         "Service", "ExtrinsicObject", "XACML", "Policy", "PolicySet" };
    ArrayList objectTypesList = new ArrayList();
    for (int index =0; index < objectTypes.length; index++) {
        objectTypesList.add(objectTypes[index]);
    }
    
    ArrayList cNamePats = new ArrayList();
    cNamePats.add("%");
    BulkResponse br = bqm.findConcepts(null, cNamePats, null, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("No Concept found that match patern %");
    }
    
    ArrayList conceptsList = new ArrayList();
    Collection collection = br.getCollection();
    for (Iterator it = collection.iterator(); it.hasNext(); ) {
        conceptsList.add(((Concept)it.next()).getValue());        
    }   
    assertTrue("Not all Object Types available in registry", conceptsList.containsAll(objectTypesList));
  }
} 

