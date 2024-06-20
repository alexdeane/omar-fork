/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/federation/FederatedQueryManager.java,v 1.13 2007/07/25 23:40:55 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query.federation;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Manages all Federations that this Registry is a member of.
 * This is a long-lived class. It creates a new instance of a FederatedQueryProcessor
 * to process each federated query.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryManager {
    private static FederatedQueryManager instance = null;

    BindingUtility bu = BindingUtility.getInstance();
    AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();    
        
    HashMap federationCache = null;

    //A context only to be used for internal usage by FederatedQuerymanager
    ServerRequestContext internalContext = null;
    
    protected FederatedQueryManager() {
        try {
            internalContext = new ServerRequestContext("FederatedQueryManager:internalContext", null);
            internalContext.setUser(ac.registryOperator);        
        } catch (RegistryException ex) {
            throw new UndeclaredThrowableException(ex);
        }        
    }
    
    public synchronized static FederatedQueryManager getInstance() {
        if (instance == null) {
            instance = new FederatedQueryManager();
        }

        return instance;
    }
    
    /**
     * Submits an AdhocQueryRequest to all Registries thare are members of specified federation.
     *
     * @param user
     * @param adhocQueryRequest the request sent to every registry ias a parrallel distrubuted query.
     */
    public AdhocQueryResponse submitAdhocQuery(ServerRequestContext context)
    throws RegistryException {
        AdhocQueryRequestType adhocQueryRequest = (AdhocQueryRequestType)context.getCurrentRegistryRequest();
        UserType user = context.getUser();
        getFederationCache();
        
        //This optional parameter specifies the id of the target Federation for a
        //federated query in case the registry is a member of multiple federations. 
        //In the absence of this parameter a registry must route the federated query to all 
        //federations of which it is a member.
        String federationId = adhocQueryRequest.getFederation();        
        HashSet federationMembers = null;
        try {
            // Get members for federation(s) specified in request
            federationMembers = getFederationMembers(federationId);
        } catch (InvalidRequestException e) {
            //It is possible that the federation was added since cache was initialized.
            //Try updating cache and retrying once
            federationCache = null;
            getFederationCache();
            try {
                federationMembers = getFederationMembers(federationId);
            } catch (InvalidRequestException e1) {
            }
        }
        
        FederatedQueryProcessor fqp = new FederatedQueryProcessor(federationMembers);
        
        return fqp.submitAdhocQuery(context);        
    }
    
    /*
     * Initializes the Federation configuration cache
     */
    private HashMap getFederationCache() throws RegistryException {
        
        try {
            if (federationCache == null) {
                federationCache = new HashMap();

                //TODO: Make this a FederationCache call
                String query = "SELECT f.* FROM Federation f";

                AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest(query);
                internalContext.pushRegistryRequest(queryRequest);
                AdhocQueryResponseType queryResp = null;
                try {
                    queryResp = qm.submitAdhocQuery(internalContext);
                } finally {
                    internalContext.popRegistryRequest();
                }
                List federations = queryResp.getRegistryObjectList().getIdentifiable();

                Iterator iter = federations.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof FederationType) {
                        FederationType federation = (FederationType)obj;
                        
                        //TODO: Make this a FederationCache call or at least a PreparedStatement
                        query = "SELECT r.* FROM Registry r, Federation f, Association a WHERE a.associationType = '" +
                            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember +
                            "' AND a.targetObject = r.id AND a.sourceObject = '" + federation.getId() +
                            "'";
                        queryRequest = bu.createAdhocQueryRequest(query);
                        internalContext.pushRegistryRequest(queryRequest);
                        try {
                            queryResp = qm.submitAdhocQuery(internalContext);
                        } finally {
                            internalContext.popRegistryRequest();
                        }
                        List _members = queryResp.getRegistryObjectList().getIdentifiable();;
                        HashSet members = new HashSet();
                        members.addAll(_members);
                        
                        //If a Federation has no members then add this registry to the Federation
                        if (members.size() == 0) {
                            members.add(ServerCache.getInstance().getRegistry(internalContext));
                        }
                        federationCache.put(federation.getId(), members);
                    }
                }
            }                            
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return federationCache;
    }
    
    /**
     * Gets the Registrys that are members of specified. 
     *
     * @param federation The specified Federation. A null value implies all Federations this registry is a member of.
     */
    private HashSet getFederationMembers(String federationId) throws RegistryException, InvalidRequestException {
        HashSet members = new HashSet();
        
        if ((federationId != null) && (federationId.length() > 0)) {
            //federation specified so only get members for specified federation
            HashSet _members = (HashSet)federationCache.get(federationId);
            if (_members == null) {
                throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.noFederationConfigured",
                        new Object[]{federationId}));
            } else {
                members.addAll(_members);
            }
        } else {
            //federation not specified so get members for all federations. Bug: Allows duplicate members
            Iterator iter = federationCache.values().iterator();
            while (iter.hasNext()) {
                HashSet _members = (HashSet)iter.next();
                members.addAll(_members);
            }
        }
        
        return members;
    }
    
}
