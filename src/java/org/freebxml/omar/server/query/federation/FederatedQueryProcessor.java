/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/federation/FederatedQueryProcessor.java,v 1.6 2007/07/25 23:40:55 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query.federation;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.IdentifiableComparator;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

/**
 * Handles dispatching of a federated query to all federation member Registries
 * and processes individual responses into a single unified response.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryProcessor {
    
    private static Log log = LogFactory.getLog(FederatedQueryProcessor.class);
    BindingUtility bu = BindingUtility.getInstance();
    AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();    
    
    private long TIMEOUT_CONSTANT = 50000L;
    private List workers = new LinkedList();
    private List threads = new LinkedList();
    
    // This will contain the list of responses, once the threads have run through
    private List responses = new LinkedList();
    // This will contain the list of exceptions from the threads that have not received a response
    private List exceptions = new LinkedList();
    
    private HashSet members = null;
    private AdhocQueryRequestType adhocQueryRequest = null;
        
    private FederatedQueryProcessor() {        
    }
    
    FederatedQueryProcessor(HashSet members) {
        this.members = members;
    }
    
    /**
     * Submits an AdhocQueryRequest to all Registries thare are members of specified federation.
     *
     * @param user
     * @param adhocQueryRequest the request sent to every registry ias a parrallel distrubuted query.
     */
    public AdhocQueryResponse submitAdhocQuery(final ServerRequestContext context)
    throws RegistryException {

        this.adhocQueryRequest = (AdhocQueryRequestType)context.getCurrentRegistryRequest();
        try {
            //Reset federated flag before doing federated query to avoid infinite loop
            adhocQueryRequest.setFederated(false);

            //Reset federation flag before doing federated query to avoid potential 
            //for implementations to interpret non-null values as implying federated query
            adhocQueryRequest.setFederation(null);

            // Create a barrier for all worker threads
            log.trace("Dispatching federated query to " + members.size() + " member registries.");
            CyclicBarrier barrier = new CyclicBarrier(members.size(),
            new Runnable() {
                public void run() {
                    retrieveResults(context);
                }
            });

            ThreadGroup threadGroup = new ThreadGroup("federatedQuery");
            // Send a request to all destinations
            Iterator i = members.iterator();
            while (i.hasNext()) {
                RegistryType registry = (RegistryType) i.next();
                FederatedQueryWorker worker = new FederatedQueryWorker(barrier,
                registry,
                TIMEOUT_CONSTANT,
                context.getUser(),
                adhocQueryRequest);
                workers.add(worker);
                Thread thread = new Thread(threadGroup, worker);
                threads.add(thread);
                log.trace("Dispatching query to registry with id: " + registry.getId() + " name: " + bu.getInternationalStringAsString(registry.getName()));
                thread.start();
            }

            i = threads.iterator();
            while (i.hasNext()) {
                Thread thread = (Thread) i.next();
                // Wait until all threads have finished.
                // CAVEAT: The timeouts will add up, max. waiting time is (number of threads) * (timeout)
                try {
                    thread.join(TIMEOUT_CONSTANT);
                } catch (InterruptedException e) {
                    //TODO: Try to kill the thread somehow
                }
            }
        } catch (JAXRException e) {
            //This exception is thrown potentially (unlikely) by bu.getInternationalStringAsString
            throw new RegistryException(e);
        }
                
        return getUnifiedResponse();        
    }
    
    private AdhocQueryResponse getUnifiedResponse() throws RegistryException {
        AdhocQueryResponse response = null;
        try {
            response = bu.queryFac.createAdhocQueryResponse();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectList rol = bu.rimFac.createRegistryObjectList();
            response.setRegistryObjectList(rol);
            int maxTotalResultCount = -1;
            org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el = null;
            
            Iterator i = responses.iterator();
            while (i.hasNext()) {
                AdhocQueryResponseType resp = (AdhocQueryResponseType)i.next();
                int totalResultCount = resp.getTotalResultCount().intValue();
                if (totalResultCount > maxTotalResultCount) {
                    maxTotalResultCount = totalResultCount;
                }
                
                if ((resp.getRegistryErrorList() != null) && (resp.getRegistryErrorList().getRegistryError().size() > 0)) {
                    if (el == null) {
                        el = bu.rsFac.createRegistryErrorList();
                        response.setRegistryErrorList(el);                        
                    }
                    response.getRegistryErrorList().getRegistryError().addAll(resp.getRegistryErrorList().getRegistryError());
                }
                
                //Spec Issue: How to handle duplicate id across registries?? 
                //Need to return one. Which one? The latest? Probably the one from current registry.
                //May require always querying current registry last since code below keeps replacing existing objects 
                //with new ones that have same id.
                if (resp.getRegistryObjectList() != null) {
                    IdentifiableComparator comparator = new IdentifiableComparator();                    
                    List unifiedObjects = response.getRegistryObjectList().getIdentifiable();
                    List currentObjects = resp.getRegistryObjectList().getIdentifiable();
                    Collections.sort(unifiedObjects, comparator);
                    
                    //Remove duplicates. 
                    //unifiedObjects.removeAll(currentObjects) will not work as there is no comparator implemented for JAXB objects
                    Iterator currentObjectsIter = currentObjects.iterator();
                    while (currentObjectsIter.hasNext()) {
                        RegistryObjectType currentObject = (RegistryObjectType)currentObjectsIter.next();
                        int index = Collections.binarySearch(unifiedObjects, currentObject, comparator);
                        if (index >= 0) {
                            unifiedObjects.remove(index);
                            log.trace("Removing duplicate object returned by a previous registry: id=" + currentObject.getId() + " name=" + bu.getInternationalStringAsString(currentObject.getName()));
                        }
                        log.trace("Adding object returned by registry: id=" + currentObject.getId() + " name=" + bu.getInternationalStringAsString(currentObject.getName()));
                    }
                    
                    //Add the currentObjects to unified objects
                    unifiedObjects.addAll(currentObjects);                    
                }
            }
            
            if ((response.getRegistryErrorList() != null) && (response.getRegistryErrorList().getRegistryError().size() > 0)) {
                response.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
            } else {
                response.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            }
            
            //Set iterative query related attributes
            response.setStartIndex(adhocQueryRequest.getStartIndex());
            response.setTotalResultCount(BigInteger.valueOf(maxTotalResultCount));
            
        } catch (JAXBException e) {
            throw new RegistryException(e);
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
                
        return response;
    }
    
    /**
     * This method is run by the barrier once all workers have entered the barrier
     */
    private void retrieveResults(ServerRequestContext context) {
        
        Iterator i = workers.iterator();
        while (i.hasNext()) {
            
            FederatedQueryWorker worker = (FederatedQueryWorker) i.next();
            if (worker.getResponse() != null) {
                responses.add(worker.getResponse());
                
                //Add repositoryItems to context's repositoryItemMap
                context.getRepositoryItemsMap().putAll(worker.getRepositoryItemsMap());
            }
            else if (worker.getException() != null) {
                exceptions.add(worker.getException());
            }
        }
        
    }        
}
