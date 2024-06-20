/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/query/ReferenceResolverTest.java,v 1.1 2007/04/19 16:46:49 psterk Exp $
 * ====================================================================
 */

package org.freebxml.omar.server.query;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/**
 *
 * @author psterk
 */
public class ReferenceResolverTest extends ServerTest {
    
    /**
     * Creates a new instance of QueryFilterPluginTest
     */
    public ReferenceResolverTest(String name) {
        super(name);
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references only for the target object.
     */
    public void testResolveAllReferencesForStandaloneWSDLFile() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:StandaloneQueryFilterTest.wsdl";                
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
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection objRefs = refResolver.getReferencedObjects(context, eo);
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 2, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(null, id);
            } catch (Throwable t) {
                //ignore
            }
        }
    }

    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object.
     */
    public void testResolveReferencesAllForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // Get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 29, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references to one depth level for the target Test1.wsdl object.
     */
    public void testResolveReferencesDepth1ForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection objRefs = refResolver.getReferencedObjects(context, ro, 1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 9, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }

    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object with an
     * includes assocation filter set to AssociationType:Imports
     */
    public void testResolveReferencesAssocIncludeFilterForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            RegistryProperties.getInstance().put(
                "omar.server.referenceResolver.associations.includeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Imports");
            
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 3, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object with an
     * excludes assocation filter set to AssociationType:Contains
     */
    public void testResolveReferencesAssocExcludeFilterForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            RegistryProperties.getInstance().put(
                "omar.server.referenceResolver.associations.excludeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains");
            
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 3, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test verifies that the Reference Resolver can handle circular references
     * such as those that exist with the urn:freebxml:registry:demoDB:acp:folderACP1
     * object. This object has an AccessControlPolicyFor for the folder1 RP.
     * The folder1 RP has a hasMember association with the folderACP1 EO.
     * Thus, circular references exist in this case.
     */
    public void testResolveAllReferencesForXACMLPolicyFile() throws Exception {
        // Upload test WSDL file to compress
        String id1 = "urn:freebxml:registry:demoDB:acp:folderACP1";
        try {
            RegistryProperties.getInstance().put(
                "omar.server.referenceResolver.associations.excludeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains");
            
            // Create the ServerRequestContext
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);
            ServerRequestContext context = new ServerRequestContext("ReferenceResolverTest:testResolveAllReferencesForXACMLPolicyFile", submitRequest);
            
            // Get folderACP1 EO
            RegistryObjectType ro = QueryManagerFactory.getInstance()
                                                       .getQueryManager().getRegistryObject(context, id1);
            if (ro == null) {
                this.assertNull("This test method requires that the demoDB is installed", ro);
            } else {
                // Use ReferenceResolver to resolve all references
                ReferenceResolver refResolver = new ReferenceResolverImpl();
                Collection objRefs = refResolver.getReferencedObjects(context, ro, -1);

                // Verify you got all references
                assertEquals("testQueryWithCompressContentRequest.", 4, objRefs.size());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void cleanupFiles(String zipFileId, String id1) throws Exception {
        String cleanupAssocQueryString = "SELECT * FROM Association WHERE sourceObject = "+
                                    "('"+ id1 +"')";
        RemoveObjectsRequest removeAssocRequest = createRemoveObjectsRequest(cleanupAssocQueryString);
        ServerRequestContext assocContext = new ServerRequestContext("RepositoryTest:testDelete", removeAssocRequest);
        assocContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse assocResponse = lcm.removeObjects(assocContext);
        bu.checkRegistryResponse(assocResponse);
        String cleanupQueryString = "SELECT * FROM ExtrinsicObject WHERE id = "+
                                    "('"+ id1 +"')";
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(cleanupQueryString);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponse response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
    
    private ServerRequestContext submitTestZipFile(String id) throws Exception {
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
        submit(context, eo, idToRepositoryItemMap);
        
        return context;
    }
}
