/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cms/CanonicalXMLFilteringServiceTest.java,v 1.2 2007/06/20 19:28:25 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * JUnit TestCase for CanonicalXMLFilteringServiceTest.
 */
public class CanonicalXMLFilteringServiceTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();

    /**
     * Constructor for CanonicalXMLFilteringServiceTest
     *
     * @param name
     */
    public CanonicalXMLFilteringServiceTest(String name) {
        super(name);

    }

    public static Test suite() {
        return new TestSuite(CanonicalXMLFilteringServiceTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
        
    /*
     * This test verifies that the CanonicalXMLFilteringService 
     * filters results of CPP query when user is not authorized to
     * see some content.
     *
     * Strategy: simply query the CPP1.xml demo CPP as authorized user
     * and verify that no filtering is done.
     */
    public void testNoFilteringForAuthorizedUser() throws Exception {      
        String id = "urn:freebxml:registry:sample:profile:cpp:instance:cpp1";
        
        HashMap queryParamsMap = new HashMap();        
        queryParamsMap.put("$id", id);
        queryParamsMap.put("$tableName", "ExtrinsicObject");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetSchemesByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        List res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType, queryParamsMap);
        assertEquals("Did not find CPP1.xml", 1, res.size());
        
        ExtrinsicObjectType ro = (ExtrinsicObjectType)res.get(0);
        assertNotNull("RegistryObject should not be filtered for authothorized user", ro.getName());
        
        RepositoryItem ri = (RepositoryItem) context.getRepositoryItemsMap().get(id);
        String content = Utility.getInstance().unmarshalInputStreamToString(ri.getDataHandler().getInputStream());
        assertTrue("RepositoryItem should not be filtered for authorized user", (content.indexOf("tp:Comment") != -1));
    }
    
    /*
     * This test verifies that the CanonicalXMLFilteringService 
     * filters results of CPP query when user is not authorized to
     * see some content.
     *
     * Strategy: simply query the CPP1.xml demo CPP as RegistryGuest
     * and verify that name is filtered, decsription is masked and
     * tp:Comment in CPP is filtered.
     */
    public void testFilteringForUnauthorizedUser() throws Exception {      
        String id = "urn:freebxml:registry:sample:profile:cpp:instance:cpp1";
        
        HashMap queryParamsMap = new HashMap();        
        queryParamsMap.put("$id", id);
        queryParamsMap.put("$tableName", "ExtrinsicObject");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetSchemesByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType, queryParamsMap);
        assertEquals("Did not find CPP1.xml", 1, res.size());
        
        ExtrinsicObjectType ro = (ExtrinsicObjectType)res.get(0);
        assertNull("RegistryObject should be filtered for unauthothorized user", ro.getName());
        
        RepositoryItem ri = (RepositoryItem) context.getRepositoryItemsMap().get(id);
        String content = Utility.getInstance().unmarshalInputStreamToString(ri.getDataHandler().getInputStream());
        assertFalse("RepositoryItem should be filtered for unauthorized user", (content.indexOf("tp:Comment") == -1));        
    }
    

}
