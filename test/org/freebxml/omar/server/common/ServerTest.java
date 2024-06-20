/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/common/ServerTest.java,v 1.24 2006/08/02 14:01:55 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.common;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.ObjectsNotFoundException;
import org.freebxml.omar.common.spi.LifeCycleManager;
import org.freebxml.omar.common.spi.LifeCycleManagerFactory;
import org.freebxml.omar.common.OMARTest;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.security.authorization.AuthorizationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.User;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;



/**
 * @author Farrukh Najmi
 *
 * Common base class for server-side tests.
 * Server-side tests should extend this class.
 * Any code commonly useful to any server-side test
 * should be added to this class.
 *
 */
public abstract class ServerTest extends OMARTest {
    
    protected static LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    protected RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    protected static AuthorizationServiceImpl az = AuthorizationServiceImpl.getInstance();
    protected static URL cppaURL = ServerTest.class.getResource("/resources/CPP1.xml");
    protected HashMap idToRepositoryItemMap = new HashMap();
    
    /** Creates a new instance of ServerTest */
    public ServerTest(String name) {
        super(name);
        //lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
        //qm = QueryManagerFactory.getInstance().getQueryManager();
	//bu = BindingUtility.getInstance();
    }
    
    public RemoveObjectsRequest createRemoveObjectsRequest(String query) throws Exception {
        AdhocQueryRequestType queryRequest = bu.createAdhocQueryRequest(query);
        ServerRequestContext context = new ServerRequestContext("ServerTest:createRemoveObjectsRequest", queryRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        RemoveObjectsRequest rmRequest = bu.lcmFac.createRemoveObjectsRequest();
        rmRequest.setAdhocQuery(queryRequest.getAdhocQuery());
        
        return rmRequest;
    }
    
    private int contextCounter = 0;
    protected ServerRequestContext getContext(User user) throws Exception {
        String contextId = "Context:" + getClass().getName().replaceAll("\\.", ":")
            + ":" + getName() + ":" + (contextCounter++);
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(user);
        return context;
    }

    protected void closeContext(ServerRequestContext context, boolean commit) throws Exception {
        if (commit) {
            context.commit();
        } else {
            context.rollback();
        }
    }
    
    protected void submit(ServerRequestContext context, Object object) throws Exception {
        submit(context, Collections.singletonList(object));
    }
    
    protected void submit(ServerRequestContext context, Object object, Map idToRepositoryItemMap) throws Exception {
        submit(context, Collections.singletonList(object), idToRepositoryItemMap);
    }
    
    protected void submit(ServerRequestContext context, List objects) throws Exception {
        Map idToRepositoryItemMap = new HashMap();
        this.submit(context, objects, idToRepositoryItemMap);
    }    

    protected void submit(ServerRequestContext context, List objects, Map idToRepositoryItemMap) throws Exception {
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectList roList = bu.rimFac.createRegistryObjectList();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        boolean requestOk = false;
        try {
            context.pushRegistryRequest(submitRequest);
            RegistryResponse resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }    
    
    protected List executeQuery(ServerRequestContext context, String queryId, Map queryParams) throws Exception {
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
            AdhocQueryResponseType resp = qm.submitAdhocQuery(context);
            bu.checkRegistryResponse(resp);
            res = resp.getRegistryObjectList().getIdentifiable();
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
        
        return res;
    }    

    protected void removeIfExist(ServerRequestContext context, String objectId) throws Exception {
        try {
            ServerCache.getInstance().getRegistryObject(context, objectId, "RegistryObject");
            //Turn on forced delete mode.
            HashMap removeRequestSlots = new HashMap();
            removeRequestSlots.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "true");
            remove(context, Collections.singleton(objectId), null, removeRequestSlots);
        } catch (ObjectNotFoundException o) {
            return;
        } catch (ObjectsNotFoundException o) {
            return;
        }       
    }
    
    protected void remove(ServerRequestContext context, String objectId) throws Exception {        
        remove(context, Collections.singleton(objectId), null);
    }
    
    protected void remove(ServerRequestContext context, Set objectIds, String queryString) throws Exception {
        HashMap removeRequestSlots = new HashMap();
        removeRequestSlots.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "false");
        remove(context, objectIds, queryString, removeRequestSlots);
    }
    
    protected void remove(ServerRequestContext context, Set objectIds, String queryString, HashMap removeRequestSlots) throws Exception {
        ArrayList objectRefs = new ArrayList();
        
        if ((objectIds != null) && (objectIds.size() > 0)) {
            Iterator iter = objectIds.iterator();
            while (iter.hasNext()) {
                String objectId = (String)iter.next();
                ObjectRef objectRef = bu.rimFac.createObjectRef();
                objectRef.setId(objectId);        
                objectRefs.add(objectRef);                
            }
        }
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        if (removeRequestSlots != null) {
            bu.addSlotsToRequest(removeRequest, removeRequestSlots);
        }
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        if ((queryString != null) && (queryString.length() > 0)) {
            AdhocQueryType adhocQuery = bu.rimFac.createAdhocQuery();
            adhocQuery.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
            adhocQuery.setQueryExpression(queryExp);
            queryExp.setQueryLanguage(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);

            queryExp.getContent().add(queryString);
            removeRequest.setAdhocQuery(adhocQuery);
        }
                
        boolean requestOk = false;
        try {
            context.pushRegistryRequest(removeRequest);
            RegistryResponse resp = lcm.removeObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
    
    protected void approve(ServerRequestContext context, String objectId) throws Exception {
        ArrayList objectRefs = new ArrayList();
        ObjectRef objectRef = bu.rimFac.createObjectRef();
        objectRef.setId(objectId);        
        objectRefs.add(objectRef);
        
        ApproveObjectsRequest approveRequest = bu.lcmFac.createApproveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        approveRequest.setObjectRefList(orList);

        boolean requestOk = false;
        try {
            context.pushRegistryRequest(approveRequest);
            RegistryResponse resp = lcm.approveObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
        
    protected void setStatus(ServerRequestContext context, String objectId, String statusId) throws Exception {
        ArrayList objectRefs = new ArrayList();
        ObjectRef objectRef = bu.rimFac.createObjectRef();
        objectRef.setId(objectId);        
        objectRefs.add(objectRef);
        
        SetStatusOnObjectsRequest setStatusRequest = bu.lcmFac.createSetStatusOnObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
        orList.getObjectRef().addAll(objectRefs);
        setStatusRequest.setObjectRefList(orList);
        setStatusRequest.setStatus(statusId);

        boolean requestOk = false;
        try {
            context.pushRegistryRequest(setStatusRequest);
            RegistryResponse resp = lcm.setStatusOnObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
    
    public CredentialInfo getCredentialInfo(String alias, String password) 
        throws Exception 
    {
        X509Certificate cert = ac.getCertificate(alias);
        if (cert == null) {
            throw new RegistryException("X509Certificate not found for alias:" + alias);
        }

        java.security.PrivateKey privateKey = ac.getPrivateKey(alias, password);
        if (privateKey == null) {
            throw new RegistryException("PrivateKey not found for alias:" + alias);
        }

        java.security.cert.Certificate[] certChain = ac.getCertificateChain(alias);
        
        return new CredentialInfo(alias, cert, certChain, privateKey);
    }
    
    /**
     * Create a RepositoryItem containing provided content.
     *
     * @param id id to use when signing the RepositoryItem
     * @param content contents of the created RepositoryItem
     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
     * @param deleteOnExit whether to delete the temporary file containing <CODE>content</CODE> after the test
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    public RepositoryItem createRepositoryItem(String id, String content, String alias, String password, boolean deleteOnExit) throws Exception {
        File file = createTempFile(deleteOnExit, content);
        
        DataHandler dh = new DataHandler(new FileDataSource(file));        
        return createRepositoryItem(dh, id);
    }
    
    /**
     * Creates a signed CPP RepositoryItem.
     * 
     * @param id id to use when signing the RepositoryItem
     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    public RepositoryItem createCPPRepositoryItem(String id)
        throws Exception {
        DataHandler dh = new javax.activation.DataHandler(cppaURL);

        return new RepositoryItemImpl(id, dh);
    }
    
//    /**
//     * Creates a signed CPP RepositoryItem.
//     * 
//     * @param id id to use when signing the RepositoryItem
//     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
//     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
//     * @return a <code>RepositoryItem</code> value
//     * @exception Exception if an error occurs
//     */
//    public RepositoryItem createCPPRepositoryItem(String id, String alias, String password)
//        throws Exception {
//        DataHandler dh = new javax.activation.DataHandler(cppaURL);
//
//        return createSignedRepositoryItem(dh, id, alias, password);
//    }
    
//    /**
//     * Creates a signed RepositoryItem for a DataHandler.
//     * @param dh <CODE>DataHandler</CODE> representing RepositoryItem content.
//     * @param id id to use when signing the RepositoryItem
//     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
//     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
//     * @return a <code>RepositoryItem</code> value
//     * @exception Exception if an error occurs
//     */
//    RepositoryItem createSignedRepositoryItem(DataHandler dh, String id, String alias, String password)
//        throws Exception {
//        CredentialInfo credentialInfo = getCredentialInfo(alias, password);
//        RepositoryItem ri = su.signPayload(dh, id, credentialInfo);
//        
//        return ri;
//    }
    
    /**
     * Creates a {@link RepositoryItem}.
     *
     * @param dh the {@link DataHandler} representing the payload
     * @param id the ID to use for the {@link RepositoryItem}
     * @exception Exception if an error occurs
     */
    RepositoryItem createRepositoryItem(DataHandler dh, String id)
        throws Exception {
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        return ri;
    }
    
}
