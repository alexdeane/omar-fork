/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/AssociationTest.java,v 1.17 2006/07/26 17:27:34 farrukh_najmi Exp $
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Slot;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

/**
 * jUnit Test for Association
 *
 * @author Farrukh S. Najmi
 */
public class AssociationTest extends ClientTest {
    
    public AssociationTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AssociationTest.class);
        return suite;
    }
    
    /**
     * 
     * Test isConfirmedBySourceOwner, isConfirmedByTargetOwner methods. 
     * 
     *
     **/
    public void testIsConfirmedBy() throws Exception {
        RegistryObject mySourceObject = lcm.createRegistryPackage("testIsConfirmedBy mySourceObject");
        RegistryObject myTargetObject = lcm.createRegistryPackage("testIsConfirmedBy myTargetObject");
        
        RegistryObject othersObject = dqm.getRegistryObject(
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType, 
            LifeCycleManager.CLASSIFICATION_SCHEME);
        
        Concept assocType = (Concept)dqm.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        
        //Create an extramural assoc 
        Association extramuralAss = lcm.createAssociation(othersObject, assocType);
        mySourceObject.addAssociation(extramuralAss);
                
        boolean confirmedBySourceOwner = extramuralAss.isConfirmedBySourceOwner();
        assertFalse("isConfirmedBySourceOwner should have been false", confirmedBySourceOwner);
                
        Slot slot = lcm.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER, 
            "true", BindingUtility.CANONICAL_DATA_TYPE_ID_Boolean);
        extramuralAss.addSlot(slot);
        confirmedBySourceOwner = extramuralAss.isConfirmedBySourceOwner();
        assertTrue("isConfirmedBySourceOwner should have been true", confirmedBySourceOwner);
        
        boolean confirmedByTargetOwner = extramuralAss.isConfirmedByTargetOwner();
        assertFalse("isConfirmedByTargetOwner should have been false", confirmedByTargetOwner);
                
        slot = lcm.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER, 
            "true", BindingUtility.CANONICAL_DATA_TYPE_ID_Boolean);
        extramuralAss.addSlot(slot);
        confirmedByTargetOwner = extramuralAss.isConfirmedByTargetOwner();
        assertTrue("isConfirmedByTargetOwner should have been true", confirmedByTargetOwner);
    }
    
    /**
     * 
     * Test isExtramural method using two different connections with two different users.
     * Note that this users 2 different pre-defined users from the server keystore in two different
     * Connections.
     *
     **/
    public void testIsExtramural1() throws Exception {
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        DeclarativeQueryManagerImpl nikolaDQM = (DeclarativeQueryManagerImpl)nikolaConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager nikolaBQM = nikolaConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        DeclarativeQueryManagerImpl farrukhDQM = (DeclarativeQueryManagerImpl)farrukhConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager farrukhBQM = farrukhConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        RegistryObject nikolaUser = nikolaDQM.getCallersUser();
        RegistryObject farrukhUser = farrukhDQM.getCallersUser();
                
        Concept assocType = (Concept)farrukhDQM.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        
        //Create an extramural assoc 
        Association extramuralAss = farrukhLCM.createAssociation(nikolaUser, assocType);
        farrukhUser.addAssociation(extramuralAss);
        
        assertTrue("isExtraMural return false for extramural Association", (extramuralAss.isExtramural()));
        
    }
    
    /**
     * 
     * Test isExtramural method. 
     * 
     *
     **/
    public void testIsExtramural2() throws Exception {
        
        RegistryObject mySourceObject = lcm.createRegistryPackage("testIsExtramural mySourceObject");
        RegistryObject myTargetObject = lcm.createRegistryPackage("testIsExtramural myTargetObject");
        
        RegistryObject othersObject = dqm.getRegistryObject(
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType, 
            LifeCycleManager.CLASSIFICATION_SCHEME);
        
        Concept assocType = (Concept)dqm.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        
        //Create an extramural assoc 
        Association extramuralAss = lcm.createAssociation(othersObject, assocType);
        mySourceObject.addAssociation(extramuralAss);
        
        //Create an intramural assoc 
        Association intramuralAss = lcm.createAssociation(myTargetObject, assocType);
        mySourceObject.addAssociation(intramuralAss);
        
        ArrayList objects = new ArrayList();
        objects.add(mySourceObject);
        objects.add(myTargetObject);
        lcm.saveObjects(objects);
        
        mySourceObject = dqm.getRegistryObject(mySourceObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        myTargetObject = dqm.getRegistryObject(myTargetObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        
        extramuralAss = (Association)dqm.getRegistryObject(extramuralAss.getKey().getId(), LifeCycleManager.ASSOCIATION);
        assertTrue("isExtraMural return false for extramural Association", (extramuralAss.isExtramural()));
        
        intramuralAss = (Association)dqm.getRegistryObject(intramuralAss.getKey().getId(), LifeCycleManager.ASSOCIATION);
        assertFalse("isExtraMural return true for intramural Association", (intramuralAss.isExtramural()));
    }
    
    /**
     * 
     * Test for bug where association was modified after it was retrieved. 
     * 
     *
     **/
    public void testAssociationNotModifiedPostRetrieval() throws Exception {        
        String rootFolderId = "urn:oasis:names:tc:ebxml-regrep:RegistryPackage:registry";
        RegistryPackage rootFolder = (RegistryPackage) getBQM().getRegistryObject(rootFolderId);
        Assert.assertNotNull("Could not find root folder", rootFolder);
        
        //Attempt to save rootFolder. The bug caused the Associations for rootFolder to be fetched
        //and get marked as modified during loading. When the pkg was saved you got an AuthorizationException.
        ArrayList objectsToSave = new ArrayList();
        objectsToSave.add(rootFolder);
        BulkResponse br = getLCM().saveObjects(objectsToSave);
        JAXRUtility.checkBulkResponse(br);

        //If all is well then there should be no exception and nothing should be saved.
    }
    
    private Set getCredentialsFromKeystore(String keystorePath, String storepass, String alias, String keypass) throws Exception 
    {
        HashSet credentials = new HashSet();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new BufferedInputStream(new FileInputStream(keystorePath)), storepass.toCharArray());
        X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, keypass.toCharArray());
        credentials.add(new X500PrivateCredential(cert, privateKey, alias));        
        return credentials;
    }
    
    public void testSaveUnownedAssociations() throws Exception {

        String folderId = null;
        String farrukhExtrinsicObjectId = null;
        String farrukhAssociationId = null;
        String nikolaExtrinsicObjectId = null;
        String nikolaAssociationId = null;

        String keystorePath = RegistryProperties.getInstance().getProperty("omar.security.keystoreFile");
        String storepass = RegistryProperties.getInstance().getProperty("omar.security.keystorePassword");
        
        ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();
        Properties props = new Properties();
        props.put("javax.xml.registry.queryManagerURL", regSoapUrl);
        connFactory.setProperties(props);

        try {
            
            // create connection for Farrukh

            Connection farrukhConnection = connFactory.createConnection();
            String farrukhAlias = "urn:freebxml:registry:predefinedusers:registryoperator";
            Set farrukhCredentials = getCredentialsFromKeystore(keystorePath, storepass, farrukhAlias, farrukhAlias);
            farrukhConnection.setCredentials(farrukhCredentials);
            BusinessQueryManagerImpl farrukhBQM = 
                (BusinessQueryManagerImpl)farrukhConnection.getRegistryService().getBusinessQueryManager();
            LifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();

            // Create the folder as Farrukh

            RegistryPackage folder = farrukhLCM.createRegistryPackage("Farrukh's Folder");
            folderId = folder.getKey().getId();
            System.out.println("Created folder with id '" + folderId + "'");

            // using farrukh's connection, saveObjects() with extrinsic object and hasMember association
            File file = createTempFile(true);
            FileDataSource fds = new FileDataSource(file);
            DataHandler dataHandler = new DataHandler(fds);
            ExtrinsicObject farrukhExtrinsicObject = 
                (ExtrinsicObject)farrukhLCM.createExtrinsicObject(dataHandler);
            String eoId = Utility.getInstance().createId();
            farrukhExtrinsicObject.getKey().setId(eoId);
            farrukhExtrinsicObject.setName(farrukhLCM.createInternationalString("Farrukh's Extrinsic Object"));

            Concept associationConcept = farrukhBQM.findConceptByPath
                ("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasMember);
            Association farrukhAssociation = farrukhLCM.createAssociation(farrukhExtrinsicObject, associationConcept);
            farrukhAssociation.setSourceObject(folder);

            ArrayList objectsToSave = new ArrayList();
            objectsToSave.add(folder);
            objectsToSave.add(farrukhAssociation);
            objectsToSave.add(farrukhExtrinsicObject);
            System.out.println("Saving farrukh's extrinsic object '" + 
                farrukhExtrinsicObject.getKey().getId() + "' under folder '" + 
                folderId + "' with hasMember association '" + 
                farrukhAssociation.getKey().getId() + "'");
            BulkResponse br = farrukhLCM.saveObjects(objectsToSave);
            JAXRUtility.checkBulkResponse(br);
            farrukhExtrinsicObjectId = farrukhExtrinsicObject.getKey().getId();
            farrukhAssociationId = farrukhAssociation.getKey().getId();
            System.out.println("Objects '" + farrukhExtrinsicObject.getKey().getId() +
                "' and '" + farrukhAssociation.getKey().getId() + "' saved successfully");

            // create connection for Nikola

            Connection nikolaConnection = connFactory.createConnection();
            String nikolaAlias = "urn:freebxml:registry:predefinedusers:nikola";
            Set nikolaCredentials = getCredentialsFromKeystore(keystorePath, storepass, nikolaAlias, nikolaAlias);
            nikolaConnection.setCredentials(nikolaCredentials);
            BusinessQueryManagerImpl nikolaBQM = 
                (BusinessQueryManagerImpl)nikolaConnection.getRegistryService().getBusinessQueryManager();
            LifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();

            // Get the folder

            /*RegistryPackage*/ folder = (RegistryPackage)nikolaBQM.getRegistryObject(folderId);
            Assert.assertNotNull("Could not find root folder", folder);

            // using nikola's connection, saveObjects() with extrinsic object and hasMember association
            file = createTempFile(true);
            fds = new FileDataSource(file);
            dataHandler = new DataHandler(fds);
            ExtrinsicObject nikolaExtrinsicObject = 
                (ExtrinsicObject)nikolaLCM.createExtrinsicObject(dataHandler);
            eoId = Utility.getInstance().createId();
            nikolaExtrinsicObject.getKey().setId(eoId);
            nikolaExtrinsicObject.setName(nikolaLCM.createInternationalString("Nikola's Extrinsic Object"));

            associationConcept = nikolaBQM.findConceptByPath
                ("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasMember);
            Association nikolaAssociation = nikolaLCM.createAssociation(nikolaExtrinsicObject, associationConcept);
            nikolaAssociation.setSourceObject(folder);

            objectsToSave = new ArrayList();
            objectsToSave.add(folder);
            objectsToSave.add(nikolaAssociation);
            System.out.println("Saving nikola's extrinsic object '" + 
                nikolaExtrinsicObject.getKey().getId() + "' under folder '" + 
                folderId + "' with hasMember association '" + 
                nikolaAssociation.getKey().getId() + "'");
            objectsToSave.add(nikolaExtrinsicObject);
            br = nikolaLCM.saveObjects(objectsToSave);
            JAXRUtility.checkBulkResponse(br);
            nikolaExtrinsicObjectId = nikolaExtrinsicObject.getKey().getId();
            nikolaAssociationId = nikolaAssociation.getKey().getId();
            System.out.println("Objects '" + nikolaExtrinsicObject.getKey().getId() +
                "' and '" + nikolaAssociation.getKey().getId() + "' saved successfully");
        }
        finally {
            // remove extrinsic objects and associations as registry operator
            try {
                Connection roConnection = connFactory.createConnection();
                String roAlias = "urn:freebxml:registry:predefinedusers:registryoperator";
                Set roCredentials = getCredentialsFromKeystore(keystorePath, storepass, roAlias, roAlias);
                roConnection.setCredentials(roCredentials);
                LifeCycleManagerImpl roLCM = (LifeCycleManagerImpl)roConnection.getRegistryService().getBusinessLifeCycleManager();
                
                if (folderId != null) {
                    System.out.println("Deleting '" + folderId + "'");
                    HashSet keys = new HashSet();
                    keys.add(roLCM.createKey(folderId));
                    roLCM.deleteObjects(keys, null, forceRemoveRequestSlotsMap, null);
                    System.out.println("Successfully deleted '" + folderId + "'");
                }
                
                if (farrukhExtrinsicObjectId != null) {
                    System.out.println("Deleting '" + farrukhExtrinsicObjectId + "'");
                    HashSet keys = new HashSet();
                    keys.add(roLCM.createKey(farrukhExtrinsicObjectId));
                    roLCM.deleteObjects(keys, null, forceRemoveRequestSlotsMap, null);
                    System.out.println("Successfully deleted '" + farrukhExtrinsicObjectId + "'");
                }
                
                if (farrukhAssociationId != null) {
                    System.out.println("Deleting '" + farrukhAssociationId + "'");
                    HashSet keys = new HashSet();
                    keys.add(roLCM.createKey(farrukhAssociationId));
                    roLCM.deleteObjects(keys, null, forceRemoveRequestSlotsMap, null);
                    System.out.println("Successfully deleted '" + farrukhAssociationId + "'");
                }
                
                if (nikolaExtrinsicObjectId != null) {
                    System.out.println("Deleting '" + nikolaExtrinsicObjectId + "'");
                    HashSet keys = new HashSet();
                    keys.add(roLCM.createKey(nikolaExtrinsicObjectId));
                    roLCM.deleteObjects(keys, null, forceRemoveRequestSlotsMap, null);
                    System.out.println("Successfully deleted '" + nikolaExtrinsicObjectId + "'");
                }
                
                if (nikolaAssociationId != null) {
                    System.out.println("Deleting '" + nikolaAssociationId + "'");
                    HashSet keys = new HashSet();
                    keys.add(roLCM.createKey(nikolaAssociationId));
                    roLCM.deleteObjects(keys, null, forceRemoveRequestSlotsMap, null);
                    System.out.println("Successfully deleted '" + nikolaAssociationId + "'");
                }
                
            }
            catch (Throwable t) {
                System.err.println("Failed to remove some or all of the test objects. Exception: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    public void testDeleteAssociationSource() throws Exception {
        String mySourceObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationSource:srcpkg";
        String myTargetObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationSource:tgtpkg";
        String myAssocId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationSource:assoc";
        
        // pre test clean-up
        deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);
        
        RegistryObject mySourceObject = lcm.createRegistryPackage("testDeleteAssociationSource mySourceObject");
        RegistryObject myTargetObject = lcm.createRegistryPackage("testDeleteAssociationSource myTargetObject");
        mySourceObject.setKey(lcm.createKey(mySourceObjectId));
        myTargetObject.setKey(lcm.createKey(myTargetObjectId));
        
        Concept assocType = (Concept)dqm.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        //Create an assoc 
        Association myAssoc = lcm.createAssociation(myTargetObject, assocType);
        myAssoc.setKey(lcm.createKey(myAssocId));
        mySourceObject.addAssociation(myAssoc);
        
        Collection saveObjects = new ArrayList();
        saveObjects.add(mySourceObject);
        saveObjects.add(myTargetObject);
        saveObjects.add(myAssoc);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving test objects failed.", br);

        try {

            br = lcm.deleteObjects(Collections.singletonList(mySourceObject.getKey()), null, forceRemoveRequestSlotsMap, null);
            assertResponseSuccess("Deleting assoc.source failed.", br);

            RegistryObject gotSource = bqm.getRegistryObject(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNull("Source was not deleted", gotSource);

            RegistryObject gotTarget = bqm.getRegistryObject(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNotNull("Association target was also deleted", gotTarget);

            RegistryObject gotAssoc = bqm.getRegistryObject(myAssocId, LifeCycleManager.ASSOCIATION);
            assertNull("This is a known failure that needs to be investigated and fixed. " + "Association was not deleted", gotAssoc);

        } finally {
            // post test clean-up
            deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);
        }
    }
    
    public void testDeleteAssociationTarget() throws Exception {
        String mySourceObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationTarget:srcpkg";
        String myTargetObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationTarget:tgtpkg";
        String myAssocId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociationTarget:assoc";

        // pre test clean-up
        deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);

        RegistryObject mySourceObject = lcm.createRegistryPackage("testDeleteAssociationTarget mySourceObject");
        RegistryObject myTargetObject = lcm.createRegistryPackage("testDeleteAssociationTarget myTargetObject");
        mySourceObject.setKey(lcm.createKey(mySourceObjectId));
        myTargetObject.setKey(lcm.createKey(myTargetObjectId));
        
        Concept assocType = (Concept)dqm.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        
        //Create an assoc 
        Association myAssoc = lcm.createAssociation(myTargetObject, assocType);
        myAssoc.setKey(lcm.createKey(myAssocId));
        mySourceObject.addAssociation(myAssoc);
        
        Collection saveObjects = new ArrayList();
        saveObjects.add(mySourceObject);
        saveObjects.add(myTargetObject);
        saveObjects.add(myAssoc);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving test objects failed.", br);

        try {
            
            br = lcm.deleteObjects(Collections.singletonList(myTargetObject.getKey()), null, forceRemoveRequestSlotsMap, null);
            assertResponseSuccess("Deleting assoc.target failed.", br);

            RegistryObject gotSource = bqm.getRegistryObject(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNotNull("Association source was also deleted", gotSource);

            RegistryObject gotTarget = bqm.getRegistryObject(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNull("Target was not deleted", gotTarget);

            RegistryObject gotAssoc = bqm.getRegistryObject(myAssocId, LifeCycleManager.ASSOCIATION);
            assertNull("This is a known failure that needs to be investigated and fixed. " + "Association was not deleted", gotAssoc);

        } finally {
            // post test clean-up
            deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);
        }
    }

    public void testDeleteAssociation() throws Exception {
        String mySourceObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociation:srcpkg";
        String myTargetObjectId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociation:tgtpkg";
        String myAssocId = "urn:freebxml:registry:test:AssociationTest.testDeleteAssociation:assoc";

        // pre test clean-up
        deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
        deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);

        RegistryObject mySourceObject = lcm.createRegistryPackage("testDeleteAssociation mySourceObject");
        RegistryObject myTargetObject = lcm.createRegistryPackage("testDeleteAssociation myTargetObject");
        mySourceObject.setKey(lcm.createKey(mySourceObjectId));
        myTargetObject.setKey(lcm.createKey(myTargetObjectId));
        
        Concept assocType = (Concept)dqm.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        
        //Create an assoc 
        Association myAssoc = lcm.createAssociation(myTargetObject, assocType);
        myAssoc.setKey(lcm.createKey(myAssocId));
        mySourceObject.addAssociation(myAssoc);
        
        Collection saveObjects = new ArrayList();
        saveObjects.add(mySourceObject);
        saveObjects.add(myTargetObject);
        saveObjects.add(myAssoc);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving test objects failed.", br);

        try {
            
            br = lcm.deleteObjects(Collections.singletonList(myAssoc.getKey()), null, forceRemoveRequestSlotsMap, null);
            assertResponseSuccess("Deleting assoc failed.", br);

            RegistryObject gotSource = bqm.getRegistryObject(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNotNull("Association source was also deleted", gotSource);

            RegistryObject gotTarget = bqm.getRegistryObject(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            assertNotNull("Association target was also deleted", gotTarget);

            RegistryObject gotAssoc = bqm.getRegistryObject(myAssocId, LifeCycleManager.ASSOCIATION);
            assertNull("Association was not deleted", gotAssoc);

        } finally {
            // post test clean-up
            deleteIfExist(mySourceObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myTargetObjectId, LifeCycleManager.REGISTRY_PACKAGE);
            deleteIfExist(myAssocId, LifeCycleManager.ASSOCIATION);
        }
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
