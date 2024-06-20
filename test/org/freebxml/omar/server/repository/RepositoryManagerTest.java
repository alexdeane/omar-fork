/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/repository/RepositoryManagerTest.java,v 1.11 2007/02/22 00:25:28 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/**
 * Tests for RepositoryManager.
 *
 * IMPORTANT:
 * - The tests use the global final string as content
 * = The tests have to be executed in order. A failure in a test might imply in
 * failures in subsequent tests.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public abstract class RepositoryManagerTest extends ServerTest {
    
    private static final String id = org.freebxml.omar.common.Utility.getInstance().createId();
    private static final String lid = "urn:freebxml:registry:RepositoryManagerTest:TestExtrinsicObject";
    private static File content1K;
    private static File content1M;
    private static File content2M;
    protected RepositoryManager rm = null;
            
    public RepositoryManagerTest(java.lang.String testName) throws IOException {
        super(testName);
        if (content1K == null) {
            // initialize test content
            char content1KArray[] = new char[1024]; //1Kb
            char content1MArray[] = new char[1024*1024]; //1Mb
            char content2MArray[] = new char[1024*1024*2]; //2Mb
            Arrays.fill(content1KArray, 'a');
            Arrays.fill(content1MArray, 'b');
            Arrays.fill(content1MArray, 'c');
            content1K = createTempFile(true, new String(content1KArray));
            content1M = createTempFile(true, new String(content1MArray));
            content2M = createTempFile(true, new String(content2MArray));
        }        
    }
    
    /**
     * Cleans up any left over tests objects from previous runs.
     */
    public void testSetup() throws Exception {
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + lid + "' ");
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }
        
    /**
     * Tests insert of ExtrinsicObject with RepositoryItem of size 1K
     *
     */
    public void testInsertRI1K() throws Exception {
        internalTestInsertRI(content1K);
    }
    
    /**
     * Tests insert of ExtrinsicObject with RepositoryItem of size 1K
     *
     */
    public void testInsertRI1M() throws Exception {
        internalTestInsertRI(content1M);
    }
    
    /**
     * Tests insert of ExtrinsicObject with RepositoryItem of size 1K
     * Tests for 6429815: Unable to add files > 1M to repository [with Derby DB only]
     *
     */
    public void testInsertRI2M() throws Exception {
        internalTestInsertRI(content2M);
    }
    
    /**
     * Tests that the database can handle a RepositoryItem > 1M in size.
     *
     */
    public void internalTestInsertRI(File content) throws Exception {
        internalTestInsertRI(content, null, null, null, 0, null, null);
    }
    
    /**
     * Tests that the database can handle a RepositoryItem > 1M in size.
     *
     * @param slotsMap a map of slot name / value pairs. 
     *  If not null then it is added as slots to ExtrinsicObject for RepositoryItem
     * @param contextId if not null it is used as the contextId for the RequestContext.
     * @param objectType if not null it is used to set objectType of ExtrinsicObject for RepositoryItem
     * @param sizeTolerance Allows some tolerance in difference of size between 
     *  RepositoryItem when published and after being read from Repository.
     *  This is necessary when Repository implementation may preserve the content
     *  semantically but without keeping the bits literally intact.
     * @param queryId An id for an optional stored query that is used to read back the published data if specified.
     * @param queryParamsMap the parameters for the stored query specified by queryId parameter.
     *  Ignored if queryId is null
     *  
     */
    public void internalTestInsertRI(File content, 
            Map slotsMap, 
            String contextId,
            String objectType,
            int sizeTolerance,
            String queryId,
            Map queryParamsMap
            ) throws Exception {
        if (contextId == null) {
            contextId = "urn:org:freebxml:omar:server:repository:RepositoryManagerTest:internalTestInsertRI";
        }
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);

        final String eoId = contextId + ":eo1";
        
        try {            
            // initial clean-up
            removeIfExist(context, eoId);

            // Create package
            ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
            eo.setId(eoId);
            
            if (objectType != null) {
                eo.setObjectType(objectType);
            }
            
            if (slotsMap != null) {
                bu.addSlotsToRegistryObject(eo, slotsMap);
            }
            
            RepositoryItem ro = createRepositoryItem(eoId, content);
                        
            idToRepositoryItemMap.clear();
            idToRepositoryItemMap.put(eoId, ro);

            submit(context, eo, idToRepositoryItemMap);
            
            //Make sure ro exists
            List result = rm.itemsExist(Collections.singletonList(eoId));
            assertTrue("RepositoryItem not found in repository.", result.isEmpty());
            
            //Make sure ro is the expected size
            byte expected[] = readBytes(new DataHandler(new FileDataSource(content)).getInputStream());
            long expectedLength = expected.length;

            long actualLength = rm.getItemSize(eoId);
            assertTrue("Item size differs.", (Math.abs(expectedLength-actualLength) < sizeTolerance));
            
            if (queryId != null) {
                List res = executeQuery(context, queryId, queryParamsMap);
                assertTrue("Adhoc query published data failed.", (res.size() > 0));
            }
            
        } finally {
            // final clean-up
            removeIfExist(context, eoId);
       }                                
    }
    
    
    /**
     * Test of insert method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testInsert() throws Exception {
        System.out.println("\ntestInsert");
        
        ExtrinsicObjectType eo = createExtrinsicObject();
        RepositoryItem ro = createRepositoryItem(id, content1K);
                
        //submit eo and ro
        ArrayList ros = new ArrayList();
        ros.add(eo);
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(true, true, ros);
        
        idToRepositoryItemMap.clear();
        idToRepositoryItemMap.put(id, ro);
        ServerRequestContext context = new ServerRequestContext("RepositoryManagerTest:testInsert", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);        
    }
    
    /**
     * Test of itemsExist method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testItemsExist() throws Exception {
        System.out.println("\ntestItemsExist");
        
        // depends on testInsert
        List result = rm.itemsExist(Collections.singletonList(id));
        assertNotNull(result);
        assertTrue("All items should exist. Result should be empty.", result.isEmpty());
        
        String id2 = "non-existing-id";
        List queryList = new ArrayList(2);
        queryList.add(id);
        queryList.add(id2);
        result = rm.itemsExist(queryList);
        assertNotNull(result);
        assertEquals("1 items should not exist. Result size should be 1.", 1, result.size());
        assertEquals("Wrong not-found id", id2, result.get(0));
        
    }
    
    /**
     * Test of getRepositoryItem method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testGetRepositoryItem() throws Exception {
        System.out.println("\ntestGetRepositoryItem");
        
        // depends on testInsert
        
        RepositoryItem repositoryItem = rm.getRepositoryItem(id);
        assertNotNull(repositoryItem);
        
        InputStream in = repositoryItem.getDataHandler().getInputStream();
        assertNotNull(in);
        byte actual[] = readBytes(in);
        
//        assertEquals("signature", repositoryItem.getSignatureElement().getNodeName());
        byte expected[] = readBytes(new DataHandler(new FileDataSource(content1K)).getInputStream());
        
        //System.out.println("\nexpected:\n----------\n");
        //System.out.println(new String(expected));
        //System.out.println("\nactual:\n----------\n");
        //System.out.println(new String(actual));
        assertTrue("Content has changed!", Arrays.equals(expected, actual));
    }
    
    /**
     * Test of getItemSize method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testGetItemSize() throws Exception {
        System.out.println("\ntestGetItemSize");
        
        byte expected[] = readBytes(new DataHandler(new FileDataSource(content1K)).getInputStream());
        long expectedLength = expected.length;
        
        long actualLength = rm.getItemSize(id);
        assertEquals("Item size differs.", expectedLength, actualLength);
    }
    
    /**
     * Test of getAsStreamSource method, of class
     * org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testGetAsStreamSource() throws Exception {
        System.out.println("\ntestGetItemSize");
        
        byte[] expected = readBytes(new DataHandler(new FileDataSource(content1K)).getInputStream());
        long expectedLength = expected.length;
        
        CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(expected);
                
        StreamSource actualSS = rm.getAsStreamSource(id);
        byte[] actual = readBytes(actualSS.getInputStream());
        
        CRC32 actualChecksum = new CRC32();
        actualChecksum.update(actual);
        
        assertEquals("File checksums differ.",
        expectedChecksum.getValue(),
        actualChecksum.getValue());
    }
    
    /**
     * Test of update method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testUpdate() throws Exception {
        System.out.println("\ntestUpdate");
        
        ExtrinsicObjectType eo = createExtrinsicObject();

        // also test big file        
        RepositoryItem ro = createRepositoryItem(id, content1M);
                
        //submit eo and ro
        ArrayList ros = new ArrayList();
        ros.add(eo);
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(true, true, ros);
        
        idToRepositoryItemMap.clear();
        idToRepositoryItemMap.put(id, ro);
        ServerRequestContext context = new ServerRequestContext("RepositoryManagerTest:testUpdate", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        RegistryResponse resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
                        
        RepositoryItem gotRepositoryItem = rm.getRepositoryItem(id);
        assertNotNull(gotRepositoryItem);
        
        InputStream in = gotRepositoryItem.getDataHandler().getInputStream();
        assertNotNull(in);
        byte actual[] = readBytes(in);
        
//        assertEquals("signature", gotRepositoryItem.getSignatureElement().getNodeName());
        byte expected[] = readBytes(new DataHandler(new FileDataSource(content1M)).getInputStream());
        
        assertTrue("Content has changed!", Arrays.equals(expected, actual));
    }
    
    /**
     * Test of getItemsSize method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testGetItemsSize() throws Exception {
        System.out.println("\ntestGetItemsSize");
        
        byte expected[] = readBytes(new DataHandler(new FileDataSource(content1M)).getInputStream());
        long expectedLength = expected.length;
        
        long actualLength = rm.getItemsSize(Collections.singletonList(id));
        assertEquals("Item size differs.", expectedLength, actualLength);
    }
    
    /**
     * Test of getURIResolver method, of class org.freebxml.omar.server.repository.RepositoryManager.
     */
    public void testURIResolver1() throws Exception {
        System.out.println("\ntestURIResolver1");
        
        URIResolver uriResolver = rm.getURIResolver();
        
        Source source = null;
        boolean success;
        try {
            source = uriResolver.resolve(id, null);
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        
        assertTrue("Resolving known Id should have succeeded", success);
        assertNotNull("Resolving known Id should return Source", source);
    }
    
    /**
     * Test of getURIResolver method, of class org.freebxml.omar.server.repository.RepositoryManager.
     */
    public void testURIResolver2() throws Exception {
        System.out.println("\ntestURIResolver2");
        
        URIResolver uriResolver = rm.getURIResolver();
        
        Source source = null;
        boolean success;
        try {
            source = uriResolver.resolve(org.freebxml.omar.common.Utility.getInstance().createId(), null);
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        
        assertTrue("Resolving unknown Id should have succeeded", success);
        assertNull("Resolving unknown Id should return null", source);
    }
    
    /**
     * Test of delete method, of class org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager.
     */
    public void testDelete() throws Exception {
        System.out.println("\ntestDelete");
        
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + lid + "' ");
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        List result = rm.itemsExist(Collections.singletonList(id));
        assertNotNull(result);
        assertEquals("1 items should not exist. Result size should be 1.", 1, result.size());
        assertEquals("Wrong not-found id", id, result.get(0));
    }
    
    // util methods
    
    private ExtrinsicObjectType createExtrinsicObject() throws Exception {
        ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
        eo.setId(id);
        eo.setLid(lid);
        
        return eo;
    }
    
    private RepositoryItem createRepositoryItem(String id, File content) throws Exception {
        DataHandler contentDataHandler = new DataHandler(new FileDataSource(content));
        
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);
//        DocumentBuilder db = dbf.newDocumentBuilder();
//        Document sigDoc = db.parse(new InputSource(new StringReader(signature)));
        
        RepositoryItem repositoryItem = new RepositoryItemImpl(id, contentDataHandler);
        return repositoryItem;
    }
    
    //TODO: Need test where the same RI is shared by multiple EOs and then a single remove request removes all these EOs
    
}

