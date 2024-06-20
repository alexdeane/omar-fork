/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/federation/FederatedQueryWorker.java,v 1.9 2007/07/25 23:40:55 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query.federation;

import java.util.HashMap;
import java.util.Map;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.BulkResponseImpl;
import org.freebxml.omar.client.xml.registry.ClientRequestContext;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.QueryImpl;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.IterativeQueryParams;
import org.freebxml.omar.server.common.ConnectionManager;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import EDU.oswego.cs.dl.util.concurrent.BrokenBarrierException;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;


/**
 * Handles dispatching of a federated query to a registry and processes its results.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryWorker implements Runnable {
    
    private static Log log = LogFactory.getLog(FederatedQueryWorker.class);
    BindingUtility bu = BindingUtility.getInstance();
    private final CyclicBarrier barrier;
    private final RegistryType registry;
    private final long timeout;
    private final UserType user;
    private final AdhocQueryRequestType queryRequest;
    
    private AdhocQueryResponseType response = null;    
    private Exception exception = null;
    private Map repositoryItemsMap = null;
    
    protected DeclarativeQueryManagerImpl dqm=null;
    protected Connection connection=null;
    
    
    /**
     * TODO Document me
     */
    FederatedQueryWorker(CyclicBarrier barrier,
    RegistryType destination,
    long timeout,
    UserType user,
    AdhocQueryRequestType partAdhocQueryRequest) {
        this.barrier = barrier;
        this.registry = destination;
        this.timeout = timeout;
        this.user = user;
        this.queryRequest = partAdhocQueryRequest;                
    }
    
        /*
         * @see java.lang.Runnable#run()
         */
    public void run() {
        
        try {
            setUpConnection();
            
            // Send the actual request here
            AdhocQueryResponseType result = sendRequestToDestination(registry, user, queryRequest, timeout);
            this.response = result;
            // And return the result
            // We are not using a timeout here. A timeout should only happen if we have a lingering request
            // and we need to stop such requests before entering the rendezous,
            // otherwise we will accumulate hanging threads.
            barrier.barrier();
            
        } catch (BrokenBarrierException e) {
            
            // Well, I cannot think of anything to do here except log a big, fat error.
            
        } catch (RegistryException re) {
            
            try {
                
                // If the request failed, return the exception instead of a result
                this.exception = re;
                barrier.barrier();
                
            } catch (InterruptedException ie) {
                // log this
            }
            
        } catch (JAXRException re) {
            
            try {
                
                // If the request failed, return the exception instead of a result
                this.exception = re;
                barrier.barrier();
                
            } catch (InterruptedException ie) {
                // log this
            }
            
        } catch (InterruptedException ie) {
            // log this
        }
        
    }
    
    /**
     * @param registry
     * @param user
     * @param queryRequest
     * @param timeout
     * @return
     */
    private AdhocQueryResponseType sendRequestToDestination(RegistryType registry, UserType user, AdhocQueryRequestType queryRequest, long timeout)
    throws RegistryException {
        AdhocQueryResponseType resp = null;
        
        try {
            
            /*
            AdhocQueryType ahq = queryRequest.getAdhocQuery();            
            QueryExpressionType queryExp = ahq.getQueryExpression();
            String queryLang = queryExp.getQueryLanguage();
            String queryStr = (String)queryExp.getContent().get(0);
            Query query = null;
            if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANG_ID_SQL_92)) {
                query = dqm.createQuery(Query.QUERY_TYPE_SQL,  queryStr);
            } else {
                //TODO: filter query 
                throw new JAXRException("No support for Query Language: " + queryLang + " in persistent queries.");
            }
            */
            
            //If registry is local (connection == null) then use local call. 
            //Otherwise use JAXR connection / SOAP
            if (connection == null) {
                try {
                    org.freebxml.omar.common.spi.QueryManager qm = org.freebxml.omar.common.spi.QueryManagerFactory.getInstance().getQueryManager();
                    ServerRequestContext context = new ServerRequestContext("FederatedQueryWorker:sendRequestToDestination", queryRequest);
                    context.setUser(user);
                    resp = qm.submitAdhocQuery(context);
                    repositoryItemsMap = new HashMap();
                } catch (JAXRException e) {
                    String msg = ServerResourceBundle.getInstance().getString("message.error.localQuery", 
                            new Object[]{registry.getId(), bu.getInternationalStringAsString(registry.getName())});
                    throw new RegistryException(msg, e);                    
                }
            } else {            
                //Using impl specific convenient constructor
                try {
                    HashMap queryParams = new HashMap();
                    String returnType = ReturnType._LEAF_CLASS_WITH_REPOSITORY_ITEM;
                    queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);
                    
                    ClientRequestContext context = new ClientRequestContext("org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl:executeQuery", queryRequest);
                    
                    BulkResponseImpl br = (BulkResponseImpl)dqm.executeQuery(context, queryParams, new IterativeQueryParams());
                    log.trace("Registry id: " + registry.getId() + " name: " + bu.getInternationalStringAsString(registry.getName()) + " returned the following objects: " + br.getCollection());
                    resp = (AdhocQueryResponseType)br.getRegistryResponse();
                    
                    bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
                    this.repositoryItemsMap = context.getRepositoryItemsMap();                    
                } catch (Exception e) {
                    String msg = ServerResourceBundle.getInstance().getString("message.error.remoteQuery", 
                            new Object[]{registry.getId(), bu.getInternationalStringAsString(registry.getName())});
                    throw new RegistryException(msg, e);
                }
            }
            
        } catch (JAXRException e) {
            //This exception is thrown potentially (unlikely) by bu.getInternationalStringAsString
            throw new RegistryException(e);
        }
                
        return resp;
    }
        
    /**
     * @return
     */
    public Exception getException() {
        return this.exception;
    }
    
    /**
     * @return
     */
    public AdhocQueryResponseType getResponse() {
        return this.response;
    }
    
    public Map getRepositoryItemsMap() {
        return this.repositoryItemsMap;
    }
    
    /** Setup JAXR Connection for target registry */
    protected void setUpConnection() throws JAXRException {
        
        String home = registry.getHome();
        if ((home != null) && (connection == null)) {
            connection = ConnectionManager.getInstance().getConnection(home);
            ((ConnectionImpl)connection).setLocalCallMode(false);

            RegistryService service = connection.getRegistryService();
            dqm = (DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
        }        
    }
    
    
}
