/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/query/QueryFilterPluginTest.java,v 1.1 2007/04/19 16:46:49 psterk Exp $
 * ====================================================================
 */

package org.freebxml.omar.server.query;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/**
 *
 * @author psterk
 */
public class QueryFilterPluginTest extends ServerTest {
    
    /**
     * Creates a new instance of QueryFilterPluginTest
     */
    public QueryFilterPluginTest(String name) {
        super(name);
        RegistryProperties.getInstance().put("org.freebxml.omar.server.query.bypassCMS", "false");
    }
    
    /**
     * This test method requests that the StandaloneTest.wsdl is to be compressed.
     * This method verifies that a zip file is returned.
     */
    public void testCompressContentRequestStandaloneWSDLFile() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:StandaloneFilterQueryTest.wsdl";                
        String queryString = "SELECT * FROM ExtrinsicObject WHERE id = '"+ id +"'";
        try {
            String fileName = "StandaloneTest.wsdl";
            String baseDir = getClass().getResource("/org/freebxml/omar/server/profile/ws/wsdl/data/").toExternalForm();
            File file = new File(baseDir+fileName);
            String path = file.getPath();
            URL wsdlURL = new URL(path);
            DataHandler dh = new DataHandler(wsdlURL);
            RepositoryItem ri = new RepositoryItemImpl(id, dh);
            HashMap idToRepositoryItemMap = new HashMap();
            idToRepositoryItemMap.put(id, ri);
            // Construct SubmitObjectsRequest and place WSDL in it
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);
            ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testQueryWithCompressContentRequest", submitRequest);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("text/xml");
            eo.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL");
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

            submit(context, eo, idToRepositoryItemMap);

            // Query for the object and indicate compressed content
            HashMap queryParamsMap = new HashMap();
            Collection filterQueryIds = new ArrayList();
            filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
            queryParamsMap.put("$filterQueryIds", filterQueryIds);
            queryParamsMap.put("$query", queryString);
            ServerRequestContext queryContext = new ServerRequestContext("QueryManagerImplTest:testCompressContentQueryPlugin", null);
            queryContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            List res = executeQuery(queryContext, CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery, queryParamsMap);
            assertEquals("testQueryWithCompressContentRequest.", 1, res.size());
        } catch (Exception e) {
            throw e;
        } finally {
            cleanup(id);
        }
    }

    /**
     * This test method requests compressed content of the Test1.wsdl test file
     * that imports Test2.wsdl. The CANONICAL_SEARCH_DEPTH_PARAMETER is set to -1
     * to indicate that the search depth will include all levels.
     * This method verifies that a zip file is returned.
     */
    public void testCompressContentRequestWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            String baseDir = getClass().getResource("/org/freebxml/omar/server/profile/ws/wsdl/data/").toExternalForm();
            String[] relativeFilePaths = {
                    "topDir/test1/Test1.wsdl",
                    "topDir/test2/Test2.wsdl",
                };
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("application/zip");
            eo.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL");
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            File zipFile = File.createTempFile("omar-testCreateZipOutputStream", ".zip");        
            zipFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = null;
            try {
                zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
            } finally {
                zos.close();
            }
            URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());

            DataHandler dh = new DataHandler(wsdlURL);
            RepositoryItem ri = new RepositoryItemImpl(id, dh);
            HashMap idToRepositoryItemMap = new HashMap();
            idToRepositoryItemMap.put(id, ri);

            ServerRequestContext context = new ServerRequestContext("QueryFilterPluginTest:testCompressContentRequestWSDLFileWithImport", submitRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            RegistryResponse resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            // Query for the object and indicate compressed content
            HashMap queryParamsMap = new HashMap();
            Collection filterQueryIds = new ArrayList();
            filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
            queryParamsMap.put("$filterQueryIds", filterQueryIds);
            
            String queryString = "SELECT * FROM ExtrinsicObject WHERE id ='" +
                                  id1 +"'";
            queryParamsMap.put("$query", queryString);
            queryParamsMap.put(CanonicalConstants.CANONICAL_SEARCH_DEPTH_PARAMETER, "-1");
            ServerRequestContext queryContext = new ServerRequestContext("QueryManagerImplTest:testCompressContentQueryPlugin", null);
            queryContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            List res = executeQuery(queryContext, CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery, queryParamsMap);
            assertEquals("testQueryWithCompressContentRequest.", 1, res.size());             
        } catch (Exception e) {
            throw e;
        } finally {
            cleanup(id1);
        }
    }
    
    private void cleanup(String id) throws Exception {
        String cleanupAssocQueryString = "SELECT * FROM Association WHERE sourceObject = "+
                                        "('"+ id +"')";
        RemoveObjectsRequest removeAssocRequest = createRemoveObjectsRequest(cleanupAssocQueryString);
        ServerRequestContext assocContext = new ServerRequestContext("RepositoryTest:testDelete", removeAssocRequest);
        assocContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse assocResponse = lcm.removeObjects(assocContext);
        bu.checkRegistryResponse(assocResponse);
        String cleanupQueryString = "SELECT * FROM ExtrinsicObject WHERE id = "+
                                    "('"+ id +"')";
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(cleanupQueryString);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
}
