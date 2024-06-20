/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/persistence/rdb/AdhocQueryDAOTest.java,v 1.1 2006/06/30 05:17:32 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import javax.xml.bind.Unmarshaller;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.persistence.*;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;

import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AdhocQuery;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import java.util.HashMap;

/**
 * @author Farrukh S. Najmi
 */
public class AdhocQueryDAOTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    
    protected static final String packageId =  "urn:org:freebxml:omar:server:persistence:rdb:AdhocQueryDAOTest";
    private final String largeQueryId = packageId + ":largeQuery";

    /**
     * Constructor for AdhocQueryDAOTest
     *
     * @param name name of the test
     */
    public AdhocQueryDAOTest(String name) {
        super(name);
    }


    /**
     *
     * @return the suite of tests to run.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AdhocQueryDAOTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AdhocQueryDAOTest("testUpdateSpillOverQuery"));
        return suite;
        
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {
        final String contextId = packageId + ":setUp";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        removeIfExist(context, largeQueryId);
        
    }

    protected void tearDown() throws Exception {
        final String contextId = packageId + ":setUp";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        removeIfExist(context, largeQueryId);
    }
    
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testCreateSpillOverQuery() throws Exception {
        createSpillOverQuery();
        readSpillOverQuery();
        executeSpillOverQuery();
    }
    
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testUpdateSpillOverQuery() throws Exception {
        createSpillOverQuery();
        createSpillOverQuery();
        readSpillOverQuery();
        executeSpillOverQuery();
    }
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testDeleteSpillOverQuery() throws Exception {
        createSpillOverQuery();
        deleteSpillOverQuery();
    }
    
    /**
     * Utility method for creating a spillOverQuery
     */
    private void createSpillOverQuery() throws Exception {
        final String contextId = packageId + ":createSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {            
            String largeQueryPath = "/resources/SubmitObjectsRequest_LargeQuery.xml";
            URL largeQueryURL = getClass().getResource(largeQueryPath);
            String largeQueryFileName = largeQueryURL.getFile();
            
            Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
            SubmitObjectsRequest req = (SubmitObjectsRequest)unmarshaller.unmarshal(new File(largeQueryFileName));
            AdhocQuery ahq = (AdhocQuery)req.getRegistryObjectList().getIdentifiable().get(0);
            ahq.setId(largeQueryId);
            ahq.setLid(largeQueryId);
            
            submit(context, ahq);

        } finally {
            context.commit();
        }                
    }
    
    /**
     * Utility method for reading a spillOverQuery
     */
    private void readSpillOverQuery() throws Exception {        
        final String contextId = packageId + ":readSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        try {
            AdhocQuery ahq = (AdhocQuery)qm.getRegistryObject(context, largeQueryId);
            if (ahq == null) {
                fail("Could not read spillover query back.");
            }
            
            //Make sure query column does not contain a spillOverId
            QueryExpressionType queryExp = ahq.getQueryExpression();
            String query = (String)queryExp.getContent().get(0);
            if (query.startsWith("urn:")) {
                fail("Query column contains a spillover id instead of actual query.");
            }
            
            //Check length
            if (query.length() < 4000) {
                fail("Query column does not have enough characters.");
            }
        } finally {
            context.commit();
        }
    }
    
    /**
     * Utility method for executing a spillOverQuery
     */
    private void executeSpillOverQuery() throws Exception {
        final String contextId = packageId + ":executeSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            String id = org.freebxml.omar.common.Utility.getInstance().createId();
            HashMap queryParams = new HashMap();
            queryParams.put("$service.name", "%ebxml%");
            queryParams.put("$considerPort", "1");
            queryParams.put("$considerBinding", "0");
            queryParams.put("$considerPortType", "0");
            Collection registryObjects = executeQuery(context, largeQueryId, queryParams); 
        } finally {
            context.commit();
        }
    }
            
    /**
     * Utility method for deleting a spillOverQuery and its EO/RI sub-objects
     */
    private void deleteSpillOverQuery() throws Exception {        
        final String contextId = packageId + ":deleteSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        try {
            removeIfExist(context, largeQueryId);
            
            //Now check that EO/RI sub-objects have also been deleted 
            AdhocQueryDAO dao = new AdhocQueryDAO(context);
            String spillOverId = dao.getSpillOverRepositoryItemId(largeQueryId, AdhocQueryDAO.QUERY_COL_COLUMN_INFO);
            
            try {
                ExtrinsicObject eo = (ExtrinsicObject)qm.getRegistryObject(context, largeQueryId);
                fail("Deleting a spillover query did not delete its EO/RI sub-objects");
            } catch (ObjectNotFoundException e) {
                //Expected
            }
                        
        } finally {
            context.commit();
        }
    }
}
