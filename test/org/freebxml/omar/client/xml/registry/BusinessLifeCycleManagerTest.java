/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/BusinessLifeCycleManagerTest.java,v 1.7 2006/02/05 17:17:34 farrukh_najmi Exp $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

/**
 * jUnit Test for BusinessLifeCycleManager
 *
 * @author Farrukh Najmi
 */
public class BusinessLifeCycleManagerTest extends ClientTest {
        
    public BusinessLifeCycleManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(BusinessLifeCycleManagerTest.class);
        return suite;
    }
    
    /**
     * Duplicates a very tough TCK test.
     * This test has been removed because it takes over 2 hours to run.
     */
    public void xxxtestIsConfirmedExtramuralPubl() throws Exception {
        BulkResponse br = null;
        Key savekey = null;
        Collection sourceKeys = null;
        Collection targetKeys = null;

        //Create 2 sets of connections etc. for 2 different identities
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        DeclarativeQueryManagerImpl farrukhDQM = (DeclarativeQueryManagerImpl)farrukhConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager farrukhBQM = farrukhConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        DeclarativeQueryManagerImpl nikolaDQM = (DeclarativeQueryManagerImpl)nikolaConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager nikolaBQM = nikolaConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
                
        try {
            Organization target = nikolaLCM.createOrganization(nikolaLCM.createInternationalString("Org Target"));
            Organization source = farrukhLCM.createOrganization(farrukhLCM.createInternationalString("Org Source"));
            
            // publish the organizations
            Collection orgs = new ArrayList();
            orgs.add(source);
            br = farrukhLCM.saveOrganizations(orgs); // publish to registry
            assertResponseSuccess("Error during saveOrganizations", br);

            sourceKeys = br.getCollection();
            Iterator iter = sourceKeys.iterator();
            while ( iter.hasNext() ) {
                savekey = (Key) iter.next();
            }
            String sourceId = savekey.getId();
            Organization pubSource =  (Organization)farrukhBQM.getRegistryObject(sourceId, LifeCycleManager.ORGANIZATION);

            orgs.clear();
            orgs.add(target);
            br = nikolaLCM.saveOrganizations(orgs); // publish to registry
            assertResponseSuccess("Error during saveOrganizations", br);
            
            targetKeys = br.getCollection();
            iter = targetKeys.iterator();
            while ( iter.hasNext() ) {
                savekey = (Key) iter.next();
            }
            String targetId = savekey.getId();

            Organization pubTarget =  (Organization)nikolaBQM.getRegistryObject(targetId, LifeCycleManager.ORGANIZATION);
            
            Concept associationType = (Concept)farrukhDQM.getRegistryObject(
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_Implements,
                LifeCycleManager.CONCEPT);
            assertNotNull("assocType is null", associationType);
            ArrayList assocTypes = new ArrayList();
            assocTypes.add(associationType);
            
            Association a = nikolaLCM.createAssociation(pubTarget, associationType);
            a.setSourceObject(pubSource);
            //System.err.println("Association: " + a.toString());
            
            nikolaLCM.confirmAssociation(a);
            //System.err.println("Association: " + a.toString());

            // publish the Association
            Collection associations = new ArrayList();
            associations.add(a);
            // user 2 saves the association.
            br = nikolaLCM.saveAssociations(associations, false);
            assertResponseSuccess("Error during saveAssociations", br);
            
            // get back the association
            Collection associationKeys = br.getCollection();
            iter = associationKeys.iterator();
            Key assocKey = (Key)iter.next();
            assertEquals("assocKey not matched.", a.getKey().getId(), assocKey.getId());
            
            Collection associationTypes = new ArrayList();
            associationTypes.add(associationType);
            //confirmedByCaller = false, confirmedByOtherParty = true.
            br = farrukhBQM.findCallerAssociations( null, Boolean.FALSE, Boolean.TRUE, associationTypes);
            assertResponseSuccess("Error during findCallerAssociations", br);
            
            associations = br.getCollection();
            assertFalse("findCallerAssociations did not return an association as expected", ( associations.size() == 0 ));
            
            assertTrue("Did not get expected association with findCallerAssociations", associations.contains(a));            
            iter = associations.iterator();
            while ( iter.hasNext() ) {
                Association ass = (Association)iter.next();
                if (ass.getKey().getId().equals(assocKey.getId())) {
                    a = ass;
                    break;
                }
            }            
            //System.err.println("Association: " + a.toString());
            
            assertFalse("isConfirmed incorrectly returned true", a.isConfirmed());
            assertFalse("isConfirmedBySourceOwner incorrectly returned true", a.isConfirmedBySourceOwner());
            
            // now confirm the association
            farrukhLCM.confirmAssociation(a);
            assertResponseSuccess("Error during confirmAssociation", br);
            
            associations.clear(); //*****************
            associations.add(a);
            br = farrukhLCM.saveAssociations(associations, false);
            assertResponseSuccess("Error during saveAssociation", br);

            br = farrukhBQM.findCallerAssociations( null, Boolean.TRUE, Boolean.TRUE, associationTypes);
            assertResponseSuccess("Error during findCallerAssociations", br);

            associations = br.getCollection();
            iter = associations.iterator();
            while ( iter.hasNext() ) {
                Association ass = (Association)iter.next();
                if (ass.getKey().getId().equals(assocKey.getId())) {
                    a = ass;
                    break;
                }
            }            

            assertTrue("isConfirmed incorrectly returned false", a.isConfirmed());
            assertTrue("isConfirmedBySourceOwner incorrectly returned false", a.isConfirmedBySourceOwner());        
         } finally {
               // clean up - get rid of published orgs
             try {
               farrukhLCM.deleteOrganizations(sourceKeys);
               nikolaLCM.deleteOrganizations(targetKeys);

             } catch (JAXRException je) { 
             }
         }

    } // end of method
    
    public void testConfirmAssociations() throws Exception {
        //farrukh own ass and srcObject. Nikola owns targetObjects
        
        ArrayList objects = new ArrayList();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        DeclarativeQueryManagerImpl nikolaDQM = (DeclarativeQueryManagerImpl)nikolaConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager nikolaBQM = nikolaConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        DeclarativeQueryManagerImpl farrukhDQM = (DeclarativeQueryManagerImpl)farrukhConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager farrukhBQM = farrukhConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        RegistryObject nikolaObject = nikolaLCM.createRegistryPackage("NikolaPackage");
        objects.clear();
        objects.add(nikolaObject);
        nikolaLCM.saveObjects(objects);
        nikolaObject = dqm.getRegistryObject(nikolaObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to save nikolaObject", nikolaObject);
        
        RegistryObject farrukhObject = farrukhLCM.createRegistryPackage("FarrukhPackage");
        objects.clear();
        objects.add(farrukhObject);
        farrukhLCM.saveObjects(objects);
        farrukhObject = dqm.getRegistryObject(nikolaObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to save farrukhObject", farrukhObject);
                
        Concept assocType = (Concept)farrukhDQM.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        ArrayList assocTypes = new ArrayList();
        assocTypes.add(assocType);
        
        //Create an extramural assoc 
        Association extramuralAss = farrukhLCM.createAssociation(nikolaObject, assocType);
        farrukhObject.addAssociation(extramuralAss);
        
        nikolaLCM.confirmAssociation(extramuralAss);
        
        objects.clear();
        objects.add(extramuralAss);
        nikolaLCM.saveObjects(objects);
                        
        //Get asses confirmed by other party 
        BulkResponse br = farrukhBQM.findCallerAssociations((Collection)null,
            Boolean.FALSE, Boolean.TRUE,
            assocTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);
        
        Collection asses = br.getCollection();
        assertTrue("This is a known failure that needs to be investigated and fixed by Farrukh. " + "isConfirmedByOtherParty extramuralAssociation not found by findCallersAssociations", asses.contains(extramuralAss));

        assertFalse("extramuralAss.isConfirmedBySourceOwner() should not return true.", (extramuralAss.isConfirmedBySourceOwner()));
        assertTrue("extramuralAss.isConfirmedByTargetOwner() should not return false.", (extramuralAss.isConfirmedByTargetOwner()));
        assertFalse("extramuralAss.isConfirmed() should not return true.", (extramuralAss.isConfirmed()));

        
        
        farrukhLCM.confirmAssociation(extramuralAss);
        objects.clear();
        objects.add(extramuralAss);
        farrukhLCM.saveObjects(objects);
        
        
        //Switch callers and find same asses by getting asses confirmed by caller 
        br = farrukhBQM.findCallerAssociations((Collection)null,
            Boolean.TRUE, Boolean.TRUE, 
            assocTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);
        
        asses = br.getCollection();
        assertTrue("isConfirmedByOtherParty extramuralAssociation not found by findCallersAssociations", asses.contains(extramuralAss));
        
        extramuralAss = (Association)farrukhBQM.getRegistryObject(extramuralAss.getKey().getId(), LifeCycleManager.ASSOCIATION);
        assertTrue("extramuralAss.isConfirmedBySourceOwner() should not return false.", (extramuralAss.isConfirmedBySourceOwner()));
        assertTrue("extramuralAss.isConfirmedByTargetOwner() should not return false.", (extramuralAss.isConfirmedByTargetOwner()));
        assertTrue("extramuralAss.isConfirmed() should not return false.", (extramuralAss.isConfirmed()));
        
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
