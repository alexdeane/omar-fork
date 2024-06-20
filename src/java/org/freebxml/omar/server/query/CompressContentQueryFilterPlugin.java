/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2007 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/CompressContentQueryFilterPlugin.java,v 1.4 2007/05/22 06:00:45 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.UUIDFactory;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.RepositoryItemNotFoundException;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.Association;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.Value;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;

/**
 * The QueryPlugin for CompressedContentQueryPlugin special parameterized query.
 * 
 * @author Paul Sterk
 *
 */
public class CompressContentQueryFilterPlugin extends AbstractQueryPlugin {
    
    private Log log = LogFactory.getLog(CompressContentQueryFilterPlugin.class);

    /**
     * This method processes the AdhocQueryRequest in the RequestContext
     *
     * @param context
     * A org.freebxml.omar.common.spi.RequestContext
     */
    public void processRequest(RequestContext context) throws RegistryException {
        try {
            ServerRequestContext serverContext = ServerRequestContext.convert(context);
            Map queryParamsMap = serverContext.getQueryParamsMap();
            int depth = 0;
            String depthString = (String)queryParamsMap.get(CanonicalConstants.CANONICAL_SEARCH_DEPTH_PARAMETER);
            if (depthString != null && depthString.length() > 0) {
                try {
                    depth = Integer.parseInt(depthString);
                } catch (Throwable t) {
                    log.error(t);                    
                }
            }
            Iterator resultsItr = serverContext.getQueryResults().iterator();
            Collection idCollection = new ArrayList();
            Iterator idIter = idCollection.iterator();
            File zipFile = null;
            ZipOutputStream zos = null;
            Collection exportedIds = new ArrayList();
            try {
                while (resultsItr.hasNext()) {
                    RegistryObjectType ro = (RegistryObjectType)resultsItr.next();
                    String id = ro.getId();
                    ExtrinsicObjectType eot = null;
                    RegistryObjectType rot = null;
                    try {
                        rot = QueryManagerFactory.getInstance().getQueryManager()
                                                               .getRegistryObject(serverContext, id, "ExtrinsicObject");
                    } catch (ObjectNotFoundException oex) {
                        String msg = ServerResourceBundle.getInstance()
                                                         .getString("message.registryObjectNotFound",
                                                                     new Object[]{id});
                        log.error(msg, oex);
                    } catch (Throwable t) {
                        log.error(t);
                    }
                    if (rot != null) {
                        if (! (rot instanceof ExtrinsicObjectType)) {
                            String msg = ServerResourceBundle.getInstance()
                                                             .getString("message.expectedExtrinsicObjectNotFound", 
                                                                         new Object[]{rot});
                            log.debug(msg);
                        } else {
                            eot = (ExtrinsicObjectType)rot;
                            RepositoryItem ri = (RepositoryItem)context.getRepositoryItemsMap().get(id);
                            if (ri == null) {                                            
                                // Get each artifact from the Repository
                                try {
                                    ri = RepositoryManagerFactory.getInstance()
                                                                 .getRepositoryManager()
                                                                 .getRepositoryItem(id);
                                } catch (RepositoryItemNotFoundException rex) {
                                    String msg = ServerResourceBundle.getInstance()
                                                                     .getString("message.RepositoryItemDoesNotExist", 
                                                                                new Object[]{rot.getId()});
                                    log.warn(msg);
                                } catch (Throwable t) {
                                    log.warn(t);
                                }
                            }
                            if (ri == null) {
                                // Check if EO is an element contained in a different EO
                                eot = getContainedExtrinsicObject(eot, serverContext);
                                if (eot != null) {
                                    try {
                                        ri = RepositoryManagerFactory.getInstance()
                                                                     .getRepositoryManager()
                                                                     .getRepositoryItem(eot.getId());
                                    } catch (RepositoryItemNotFoundException rex) {
                                        String msg = ServerResourceBundle.getInstance()
                                                                         .getString("message.RepositoryItemDoesNotExist", 
                                                                                    new Object[]{rot.getId()});
                                        log.warn(msg);
                                    } catch (Throwable t) {
                                        log.warn(t);
                                    }
                                }
                            }
                            if (ri != null) {
                                if (zipFile == null) {
                                    zipFile = getZipFile(serverContext);
                                    zos = getZipOutputStream(zipFile);
                                }
                                // Store each RI in the compressed content zip file
                                addRepositoryItemToZipFile(ri, zos, serverContext, eot, exportedIds, depth);                    
                            }                    
                        }
                    }
                }
            } finally {
                if (zos != null) {
                    try {
                        zos.close();
                    } catch (Throwable t) {                    
                    }
                }
            }
            ArrayList results = new ArrayList();
            if (zipFile != null) {
                
                // Create an initialze the compressed content zip file EO
                ExtrinsicObject eo = BindingUtility.getInstance().rimFac.createExtrinsicObject();
                String lid = "urn:uuid:"+UUIDFactory.getInstance().newUUID().toString();
                String zipFileName = zipFile.getName();
                eo.setId(lid);
                eo.setLid(lid);
                eo.setName(bu.createInternationalStringType(zipFileName));
                SlotType1 slot = bu.rimFac.createSlot();
                slot.setName(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT_FILENAME);               
                ValueListType vlt = bu.rimFac.createValueListType();
                Value value = bu.rimFac.createValue(zipFileName);
                vlt.getValue().add(value);
                slot.setValueList(vlt);
                eo.getSlot().add(slot);
                eo.setMimeType("application/zip");
                eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
                eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());
                
                results.add(eo);
            }
            serverContext.setSpecialQueryResults(results);
        } catch (RegistryException re) {
            throw re;
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
    }

    /**
     * This method gets the id of this QueryPlugin
     *
     * @return java.lang.String
     * The String containing the id
     */
    public String getId() {
        return BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT;
    }

    /**
     * This method gets the name of this QueryPlugin
     *
     * @return org.oasis.ebxml.registry.bindings.rim.InternationalStringType
     * The String containing the name
     */
    public InternationalStringType getName() {
        InternationalStringType is = null;
        try {
            //??I18N: Need a ResourceBundle class for minDB
            is = bu.createInternationalStringType("CompressedContentQueryPlugin");
        } catch (JAXRException e) {
            //can't happen
            log.error(e);
        }
        
        return is;
    }

    /**
     * This method gets the description of this QueryPlugin
     *
     * @return org.oasis.ebxml.registry.bindings.rim.InternationalStringType
     * The String containing the description
     */
    public InternationalStringType getDescription() {
        InternationalStringType is = null;
        try {
            //??I18N: Need a ResourceBundle class for minDB
            is = bu.createInternationalStringType("CompressedContentQueryPlugin");
        } catch (JAXRException e) {
            //can't happen
            log.error(e);
        }
        
        return is;
    }
    
    /*
     * This method takes a File and returns a ZipOutputStream used to store the
     * compressed content.
     */
    private ZipOutputStream getZipOutputStream(File file) throws FileNotFoundException {
        //ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        //zos.setMethod(ZipOutputStream.STORED);
        FileOutputStream dest = new FileOutputStream(file);
        CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(checksum));
        return zos;
    }
    
    /*
     * This method creates a compressed content zip file in the java.io.tmpdir
     */
    private File getZipFile(ServerRequestContext context) {
        StringBuffer sb = new StringBuffer();        
        String userName = getLocalizedString(context, context.getUser().getName().getLocalizedString());
        if (userName == null) {
            userName = "RegistryGuest";
        }
        sb.append(userName);
        sb.append("-freebxml-registry-");
        sb.append(getDateTime());
        sb.append(".zip");
        String zipFileName = sb.toString();
        return new File(System.getProperty("java.io.tmpdir"), zipFileName);
    }
    
    /*
     * Gets current datetime as String
     * TODO: place in Utility class
     */
    private String getDateTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return sdf.format(date);
    }
    
    /*
     * This method takes each RI and adds it to the compressed content zip file.
     */    
    protected void addRepositoryItemToZipFile(RepositoryItem ri,
                                              ZipOutputStream zos,
                                              ServerRequestContext context,
                                              ExtrinsicObjectType eo,
                                              Collection exportedIds, int depth)
        throws RegistryException {
        try {
            String objectType = eo.getObjectType();
            if (! exportedIds.contains(eo.getId())) {
                String zipEntryValue = getZipEntryValue(eo, context);
                internalAddRepositoryItemToZipFile(ri, zos, zipEntryValue);
                exportedIds.add(eo.getId());
                // Resolve all dependent imported files using imports assoc
                resolveImportedExtrinsicObjects(zos, context, eo, exportedIds, depth);
                // Check if EO is an element contained in a different EO
                ExtrinsicObjectType eot = getContainedExtrinsicObject(eo, context);
                if (eot != null) {
                    resolveImportedExtrinsicObjects(zos, context, eot, exportedIds, depth);
                }
            }
        } catch (RegistryException re) {
            throw re;
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
    }

    /*
     * This method takes a RepositoryItem and adds it to the ZipOutputStream
     */
    private void internalAddRepositoryItemToZipFile(RepositoryItem ri,
                                                    ZipOutputStream zos,
                                                    String zipEntryValue)
        throws IOException {
        if (ri != null) {
            int BUFFER = 2048;
            byte data[] = new byte[BUFFER];
            BufferedInputStream origin = null;
            try {
                InputStream is = ri.getDataHandler().getInputStream();
                origin = new BufferedInputStream(is, BUFFER);
                ZipEntry entry = new ZipEntry(zipEntryValue);
                zos.putNextEntry(entry);
                int count;
                while((count = origin.read(data, 0, BUFFER)) != -1) {
                   zos.write(data, 0, count);
                }
            } finally {
                if (origin != null) {
                    try {
                        origin.close();
                    } catch (Throwable t) {
                    }
                }
            }
        }
    }
    
    /*
     * This method creates the String that will be used for the ZipEntry
     */
    protected String getZipEntryValue(ExtrinsicObjectType eo, ServerRequestContext serverContext)
        throws RegistryException {
        StringBuffer zipEntryValue = new StringBuffer();
        try {
            String id = eo.getId();
            id = Utility.makeValidFileName(id);
            zipEntryValue.append(id);
            String queryId = CanonicalConstants.CANONICAL_QUERY_GetRegistryPackagesByMemberId;
            resolveHasMemberAssociations(serverContext, queryId, id, zipEntryValue);
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        return zipEntryValue.toString();
    }
    
    /*
     * This method is used to recurse up all upstream hasMember associations.
     */
    protected void resolveHasMemberAssociations(ServerRequestContext serverContext,
                                          String queryId,
                                          String id,
                                          StringBuffer zipEntryValue)
        throws RegistryException, Exception {
        ServerRequestContext context = null;
        try {
            Map queryParamsMap = new HashMap();
            queryParamsMap.put("$memberId", id);
            context = new ServerRequestContext("CompressContentQueryFilterPlugin:internalGetZipEntryValue", null);
            context.setUser(serverContext.getUser());
            List res = executeQuery(context, queryId, queryParamsMap);
            Iterator roItr = res.iterator();
            if (roItr.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)roItr.next();
                id = ro.getId();
                if (! id.equalsIgnoreCase(CanonicalConstants.CANONICAL_USERDATA_FOLDER_ID)) {
                    String packageName = getLocalizedString(serverContext, ro.getName().getLocalizedString());
                    zipEntryValue.insert(0, '/');
                    zipEntryValue.insert(0, packageName);
                    resolveHasMemberAssociations(serverContext, queryId, id, zipEntryValue);
                }
            }
        } finally {
            if (context != null) {
                context.rollback();
            }
        }
    }
    
    /*
     * This is a convenience method for executing queries.
     * TODO: factor out into a utility class
     */
    protected List executeQuery(ServerRequestContext context, 
                               String queryId, 
                               Map queryParams) 
        throws Exception {
        List res = null;
        AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
        int startIndex = 0;
        int maxResults = -1;
        req.setStartIndex(BigInteger.valueOf(startIndex));
        req.setMaxResults(BigInteger.valueOf(maxResults));

        Map slotsMap = new HashMap();
        slotsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, queryId);
        if ((queryParams != null) && (queryParams.size() > 0)) {
            slotsMap.putAll(queryParams);
        }
        BindingUtility.getInstance().addSlotsToRequest(req, slotsMap);

        //Now execute the query
        Map idToRepositoryItemMap = new HashMap();
        context.setRepositoryItemsMap(idToRepositoryItemMap);

        boolean requestOk = false;
        try {
            context.pushRegistryRequest(req);
            AdhocQueryResponseType resp = QueryManagerFactory.getInstance()
                                                             .getQueryManager()
                                                             .submitAdhocQuery(context);
            bu.checkRegistryResponse(resp);
            res = resp.getRegistryObjectList().getIdentifiable();
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
        return res;
    }    

    /*
     * Utility method for closing the ServerRequestContext
     */
    protected void closeContext(ServerRequestContext context, boolean commit) throws Exception {
        if (commit) {
            context.commit();
        } else {
            context.rollback();
        }
    }
    
    /*
     * This method takes an EO and resolves all downstream import associations
     */
    protected Collection getAllImportedEOs(ExtrinsicObjectType targetEO, 
                                         ServerRequestContext serverContext, 
                                         int depth)
        throws RegistryException {
        Collection eoList = new ArrayList();
        try {
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            // Support this parameter: urn:oasis:names:tc:ebxml-regrep:rs:depthParameter
            Collection objRefs = refResolver.getReferencedObjects(serverContext,
                                                                  targetEO,
                                                                  depth);
            if (objRefs != null) {
                Iterator objRefItr = objRefs.iterator();
                while (objRefItr.hasNext()) {
                    Object obj = objRefItr.next();
                    if (obj instanceof ExtrinsicObjectType) {
                        ExtrinsicObjectType dependentEO = (ExtrinsicObjectType)obj;
                        if (! eoList.contains(dependentEO)) {
                            eoList.add(dependentEO);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        return eoList;
    }
                        
    /*
     * This method resolves all dependent EOs of the target EO
     */
    protected void resolveImportedExtrinsicObjects(ZipOutputStream zos,
                                                 ServerRequestContext context,
                                                 ExtrinsicObjectType targetEO,
                                                 Collection exportedIds, int depth)
        throws RegistryException {
        try {
            Collection eos = getAllImportedEOs(targetEO, context, depth);
            Iterator eoItr = eos.iterator();
            while (eoItr.hasNext()) {
                ExtrinsicObjectType importedEO = (ExtrinsicObjectType)eoItr.next();
                String importedEOId = importedEO.getId();
                // Check for duplicate entries
                if (! exportedIds.contains(importedEOId)) {
                    // Second: resolve RP hasMember associations.
                    String zipEntryValue = getZipEntryValue(importedEO, context);
                    RepositoryItem importedRI = null;
                    try {
                        importedRI = RepositoryManagerFactory.getInstance()
                                                             .getRepositoryManager()
                                                             .getRepositoryItem(importedEOId);
                    } catch (RepositoryItemNotFoundException ex) {
                        // This is possible. The targetEO may have references to other EOs
                        // with no RI. Continue processing.
                    }
                    if (importedRI != null) {
                        internalAddRepositoryItemToZipFile(importedRI, zos, zipEntryValue);
                        exportedIds.add(importedEO.getId());
                        // Resolve imports of child EOs
                        resolveImportedExtrinsicObjects(zos, context, importedEO, exportedIds, depth);
                    }
                }
            }
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
    }
     
    /*
     * This utilty method retrieves the LocalizedString from the EO according
     * to the user's locale
     * TODO: place in Utilty class
     */
    private String getLocalizedString(ServerRequestContext context, List lsList) {
        Locale locale = context.getLocale();
        Iterator lsItr = lsList.iterator();
        String value = null;
        while(lsItr.hasNext()) {
            LocalizedStringType ls = (LocalizedStringType)lsItr.next();
            String lang = ls.getLang();
            String userLang = locale.getLanguage();
            if (lang.equalsIgnoreCase(userLang)) {
                value = ls.getValue();
                break;
            } else if (lang.indexOf(userLang) != -1 || userLang.indexOf(lang) != -1) {
                //TODO: make the locale match more robust
                value = ls.getValue();
                break;
            }
        }
        if (value == null) {
            if (lsItr.hasNext()) {
                LocalizedStringType ls = (LocalizedStringType)lsList.get(0);
                value = ls.getValue();
            }
        }
        return value;
    }
    
    /*
     * This method is used to determine if the target EO is contained in a 
     * different EO. For example: a SimpleType EO is contained in an xsd file
     * modeled by a different EO.
     */
    protected ExtrinsicObjectType getContainedExtrinsicObject(ExtrinsicObjectType targetEO, 
                                                              ServerRequestContext serverContext) 
        throws RegistryException {
        ExtrinsicObjectType containedEO = null;
        Collection eoList = new ArrayList();
        ServerRequestContext context = null;
        try {
            String queryId = CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery;
            String assocType = "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains";
            String id = targetEO.getId();
            String queryString = "SELECT ass.* FROM Association ass WHERE targetObject = '" +
                id + "'" + " AND associationType = '"+assocType 
                + "' ORDER BY sourceObject, targetObject, associationType";
            
            Map queryParamsMap = new HashMap();
            queryParamsMap.put("$query", queryString);

            context = new ServerRequestContext("CompressContentQueryFilterPlugin:getContainedExtrinsicObject", null);
            context.setUser(serverContext.getUser());
            List res = executeQuery(context, queryId, queryParamsMap);
            Iterator containsItr = res.iterator();
            // Check for most recent version
            while (containsItr.hasNext()) {
                Association assoc = (Association)containsItr.next();
                String sourceId = assoc.getSourceObject();
                ExtrinsicObjectType nextEOT = null;
                try {
                    containedEO = (ExtrinsicObjectType)ServerCache.getInstance()
                                                              .getRegistryObject(serverContext, 
                                                                                 sourceId, 
                                                                                 "ExtrinsicObject");
                } catch (ObjectNotFoundException oex) {
                    String msg = ServerResourceBundle.getInstance()
                                                     .getString("message.registryObjectNotFound",
                                                                 new Object[]{id});
                    log.error(msg);
                } catch (Throwable t) {
                    log.error(t);
                }
            }
        } catch (Throwable t) {
            throw new RegistryException(t);
        } finally {
            if (context != null) {
                context.rollback();
            }
        }
        return containedEO;
    }    
}
