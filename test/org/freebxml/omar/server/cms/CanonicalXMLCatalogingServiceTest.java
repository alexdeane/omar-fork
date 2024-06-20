/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cms/CanonicalXMLCatalogingServiceTest.java,v 1.4 2006/08/02 13:59:49 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import javax.activation.DataHandler;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;

/**
 * JUnit TestCase for CanonicalXMLCatalogingServiceTest.
 */
public class CanonicalXMLCatalogingServiceTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();

    /**
     * Constructor for CanonicalXMLCatalogingServiceTest
     *
     * @param name
     */
    public CanonicalXMLCatalogingServiceTest(String name) {
        super(name);

    }

    public static Test suite() {
        return new TestSuite(CanonicalXMLCatalogingServiceTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /*
     * This test verifies that the CanonicalXMLCatalogingService can handle an xml file of
     * type 'CPP'
     * TODO: add more unit tests to verify handling of temporary ids
     */
    public void testCatalogContentExtrinsicObjectStandaloneCPP() throws Exception {      
        String id = "urn:freebxml:registry:test:CPP1_xml";
        String sqlString = "SELECT * FROM RegistryObject WHERE lid in ('"+id+"')";
        try {
            //TODO: use standard methods in ServerTest 
            // like executeQuery, submit, remove(), removeIfExists()
            // Add new methods to ServerTest as needed
            String fileName = "CPP1.xml";
            String baseDir = getClass().getResource("/resources/").toExternalForm();
            File file = new File(baseDir+fileName);
            String path = file.getPath();
            URL wsdlURL = new URL(path);
            SubmitObjectsRequest submitRequest = createSubmitRequest(id, "text/xml");
            RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);

            bu.checkRegistryResponse(resp);

            // Execute query to check we have required metadata
            resp = executeAdhocQueryRequest(sqlString);
            bu.checkRegistryResponse(resp);       
            //Make sure that we find the CPP EO
            assertEquals("Did not find this object: " + fileName, 
                          1, ((AdhocQueryResponseType)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    /*
     * This test verifies that the Canonical XML Cataloging Service can 
     * handle an Submit Objects Request with no RI
     */
    public void testCatalogContentExtrinsicObjectStandaloneCPPMissingRI() throws Exception {      
        String id = "urn:freebxml:registry:test:CPP1_xml";
        String sqlString = "SELECT * FROM RegistryObject WHERE lid in ('"+id+"')";
        try {
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("text/xml");
            eo.setObjectType(bu.CPP_CLASSIFICATION_NODE_ID);
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            ServerRequestContext context = new ServerRequestContext("WSDLCatalogerTest:testCatalogContentExtrinsicObjectStandaloneCPPNoRI", submitRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            submit(context, eo);
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }

    private RegistryResponseType executeAdhocQueryRequest(String sqlString) 
        throws Exception {
        AdhocQueryRequest req = bu.createAdhocQueryRequest(sqlString);
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testCatalogContentExtrinsicObject", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);

        QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
        return qm.submitAdhocQuery(context);
    }
    
    private RegistryResponseType executeSubmitRequest(SubmitObjectsRequest submitRequest, 
                                                      String id, URL wsdlURL) 
        throws Exception {
        
        System.err.println("URL:" + wsdlURL.toExternalForm());
        DataHandler dh = new DataHandler(wsdlURL);
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        HashMap idToRepositoryItemMap = new HashMap();
        idToRepositoryItemMap.put(id, ri);
        
        ServerRequestContext context = new ServerRequestContext("WSDLCatalogerTest:testCatalogContentExtrinsicObject", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        return lcm.submitObjects(context);
    }
        
    private SubmitObjectsRequest createSubmitRequest(String id, String mimeType) throws Exception {
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType(mimeType);
        eo.setObjectType(bu.CPP_CLASSIFICATION_NODE_ID);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        return submitRequest;
    }
        
    private void cleanUpCatalogContentExtrinsicObjects(String sqlString) throws Exception {
        // Delete this object first to prevent false success below?
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(sqlString);
        bu.addSlotsToRequest(removeRequest, forceRemoveRequestSlotsMap);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
    
 

}
