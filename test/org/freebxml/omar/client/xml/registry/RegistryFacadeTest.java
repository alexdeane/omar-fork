/*
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/RegistryFacadeTest.java,v 1.14 2007/05/24 18:27:33 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;


import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.User;
import junit.framework.Test;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.profile.ws.wsdl.CanonicalConstants;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

/**
 * jUnit Test for RegistryFacade
 *
 * @author Farrukh Najmi
 */
public class RegistryFacadeTest extends ClientTest {
    
    private static Log log = LogFactory.getLog(RegistryFacadeTest.class.getName());

    private static RegistryFacade facade = null;
    private static User user = null;
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        // These tests need to be ordered
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new RegistryFacadeTest("testCreateFacade"));
        suite.addTest(new RegistryFacadeTest("testSetEndpoints"));
        suite.addTest(new RegistryFacadeTest("testRegisterUser"));
        suite.addTest(new RegistryFacadeTest("testRemoveRegisteredUser"));
        suite.addTest(new RegistryFacadeTest("testLogon"));
	if (canUseEbxmlrrSpecHome) {
	    suite.addTest(new RegistryFacadeTest("testPublishFilesAsZip"));
	    suite.addTest(new RegistryFacadeTest("testExecuteQuery"));
            suite.addTest(new RegistryFacadeTest("testLogoff"));
	} else {
            log.warn("Please checkout ebxmlrr-spec module to run all tests.");
        }
        
        return suite;
    }
    
    
    public RegistryFacadeTest(String testName) {
        super(testName);
    }

    public void testCreateFacade() throws Exception {
        facade = LifeCycleManagerImpl.createRegistryFacade();
        assertNotNull(facade);
    }
    
    public void testSetEndpoints() throws Exception {
        facade.setEndpoints(regSoapUrl, regSoapUrl);        
    }
    
    public void testRegisterUser() throws Exception {
        user = facade.getLifeCycleManager().createUser();
        String alias = user.getKey().getId();
        String keypass = alias;
        PersonName personName = facade.getLifeCycleManager().createPersonName("RegistryFacadeTest", alias,"TestUser");
        user.setPersonName(personName);

        facade.registerUser(user, alias, keypass); 
    }
    
    public void testRemoveRegisteredUser() throws Exception {        
        facade.logoff();
        
                
        String alias = user.getKey().getId();
        String keypass = alias;
        facade.logon(alias, keypass);        
                
        //Remove the test user
        //Specify user for deletion using query
        org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl ahq = (org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl)facade.getLifeCycleManager().createObject("AdhocQuery");
        ahq.setString("SELECT u.* FROM User_ u WHERE u.id = '" + user.getKey().getId() + "'");
        
        //Now do the delete
        BulkResponse br = facade.getLifeCycleManager().deleteObjects(new ArrayList(), ahq, forceRemoveRequestSlotsMap, null);
        assertTrue("User removal failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
                   
        //Now make sure that user is indeed removed
        User u = (User)facade.getDeclarativeQueryManager().getRegistryObject(user.getKey().getId(), LifeCycleManager.USER);
        assertNull("USer removal failed.", u);  
        
        //A bit of a hack that only works in localCall=true mode. Otherwise the following sub-test is skipped safely.
        //If in localCall mode then make sure that deleting the user aslo deleted the alias/cert in keystore
        //Tests Bug: http://sourceforge.net/tracker/index.php?func=detail&aid=1010978&group_id=37074&atid=418900
        if (((ConnectionImpl)facade.getConnection()).isLocalCallMode()) {
            AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
            
            try {
                Certificate serverCert = auth.getCertificate(alias);
                assertTrue("Cert was not deleted from keystore when user was deleted from server.", (null == serverCert));
            } catch (RegistryException e) {
                //Good. Expected.
                //Should really throw a new CertificateNotFoundException.
            }
        }                
    }        
    
    public void testLogon() throws Exception {
        String alias = getTestUserAlias();
        String keypass = getTestUserKeypass();
        facade.logon(alias, keypass);        
    }
    
    /**
     * Tests publishing a set of WSDL files as a single zip file.
     */
    public void testPublishFilesAsZip() throws Exception {        
        String baseDir = ebxmlrrSpecHome + "/misc/";
        String[] relativeFilePaths = {
            "3.0/services/ebXMLRegistryServices.wsdl",
            "3.0/services/ebXMLRegistryBindings.wsdl",
            "3.0/services/ebXMLRegistryInterfaces.wsdl",
            "3.0/schema/rs.xsd",
            "3.0/schema/lcm.xsd",
            "3.0/schema/query.xsd",
            "3.0/schema/rim.xsd",
        };
        
        String objectType = CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL;
        String id = "urn:freebxml:registry:test:RegistryFacadeTest:ebXMLRegistryWSDLFiles";
        String name = "ebXMLRegistryWSDL.zip";
        String description = "Zip file containing ebXML Registry WSDL files.";
        
        HashMap metadata = new HashMap();
        metadata.put(CanonicalConstants.CANONICAL_SLOT_IDENTIFIABLE_ID, id);
        metadata.put(CanonicalConstants.CANONICAL_SLOT_REGISTRY_OBJECT_NAME, name);
        metadata.put(CanonicalConstants.CANONICAL_SLOT_REGISTRY_OBJECT_DESCRIPTION, description);
        metadata.put(CanonicalConstants.CANONICAL_SLOT_REGISTRY_OBJECT_OBJECTTYPE, objectType);
        facade.publishFilesAsZip(
                baseDir,  
                relativeFilePaths, 
                metadata);
    }
    
    /**
     * Test executing WSDL Discovery queries.
     */
    public void testExecuteQuery() throws Exception {
        HashMap queryParams = new HashMap();
        Collection registryObjects = null;
        
        //WSDLDiscoveryQuery: Find all WSDL files
        queryParams.clear();
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_WSDL_DISCOVERY, queryParams);
        assertTrue("This is a known failure that needs to be investigated and fixed by Farrukh. " + "Failed to find WSDL documents.", registryObjects.size() >= 1);
        
        //WSDLDiscoveryQuery: Find WSDL files with $targetNamespace matching "%urn:goes:here"
        queryParams.clear();
        queryParams.put("$targetNamespace", "%urn:goes:here");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_WSDL_DISCOVERY, queryParams);
        assertTrue("This is a known failure that needs to be investigated and fixed by Farrukh. " + "Failed to find WSDL documents.", registryObjects.size() >= 1);
        
        //ServiceDiscoveryQuery: Find WSDL Service with $service.name matching "%regrep%"
        queryParams.clear();
        queryParams.put("$service.name", "%ebXML%");
        queryParams.put("$considerPort", "0");
        queryParams.put("$considerBinding", "0");
        queryParams.put("$considerPortType", "0");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_SERVICE_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Service.", registryObjects.size() >= 1);
        
        //PortDiscoveryQuery: Find WSDL Port with $binding.name matching "%query%"
        queryParams.clear();
        queryParams.put("$port.name", "%query%");
        queryParams.put("$considerBinding", "0");
        queryParams.put("$considerPortType", "0");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_PORT_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Port.", registryObjects.size() >= 1);
        
        //BindingDiscoveryQuery: Find WSDL Binding with $binding.name matching "%query%"
        queryParams.clear();
        queryParams.put("$binding.name", "%query%");
        queryParams.put("$considerPortType", "0");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_BINDING_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Binding.", registryObjects.size() >= 1);
        
        //PortTypeDiscoveryQuery: Find WSDL PortType with $binding.name matching "%query%"
        queryParams.clear();
        queryParams.put("$portType.name", "%query%");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_PORTTYPE_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL PortType.", registryObjects.size() >= 1);

        //Now test more complex params
        //ServiceDiscoveryQuery: Find WSDL Service with specified binding and portType params
        queryParams.clear();
        queryParams.put("$considerPort", "1");
        queryParams.put("$considerBinding", "1");
        queryParams.put("$binding.soapStyleType", "/urn:oasis:names:tc:ebxml-regrep:profile:ws:classificationScheme:SOAPStyleType/Document");
        queryParams.put("$considerPortType", "1");
        queryParams.put("$portType.name", "%query%");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_SERVICE_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Service.", registryObjects.size() >= 1);
        
        //ServiceDiscoveryQuery: Find WSDL Service with specified binding (and no portType) params
        queryParams.clear();
        queryParams.put("$considerPort", "1");
        queryParams.put("$considerBinding", "1");
        queryParams.put("$binding.targetNamespace", "%binding%");
        queryParams.put("$considerPortType", "0");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_SERVICE_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Service.", registryObjects.size() >= 1);
        
        //BindingDiscoveryQuery: Find WSDL Binding with specified binding and portType params
        queryParams.clear();
        queryParams.put("$considerPortType", "1");
        queryParams.put("$portType.name", "%query%");
        queryParams.put("$portType.targetNamespace", "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:interfaces:3.0");
        registryObjects = facade.executeQuery(CanonicalConstants.CANONICAL_QUERY_BINDING_DISCOVERY, queryParams);
        assertTrue("Failed to find WSDL Binding.", registryObjects.size() == 1);
        
    }
    
    public void testLogoff() throws Exception {
        facade.logoff();
        
        //Now try and publish and fail if there are no exceptions
        try {
	    // ??? this method may fail if canUseEbxmlrrSpecHome is false
	    // in any case
            testPublishFilesAsZip();
            fail("logoff did not work");
        } catch (RegistryException e) {
            //Expected UnauthorizedRequestException
        } 
    }
    
    
}
