/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/DeclarativeQueryManagerImpl.java,v 1.24 2007/08/08 21:09:44 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.Map;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Connection;
import javax.xml.registry.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.IterativeQueryParams;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.spi.RequestContext;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * Implements JAXR API interface named DeclarativeQueryManager.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class DeclarativeQueryManagerImpl extends QueryManagerImpl
    implements DeclarativeQueryManager {
    private static final Log log = LogFactory.getLog(DeclarativeQueryManagerImpl.class);
    private org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    private org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    private org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    private BindingUtility bu = BindingUtility.getInstance();

    public final static String CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS =
        "urn:javax:xml:registry:DeclarativeQueryManager:responseOption:returnComposedObjects:transientslot";        
    
    public final static String CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE =
        "urn:javax:xml:registry:DeclarativeQueryManager:responseOption:returnType:transientslot";        
    
    DeclarativeQueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm) throws JAXRException {
        super(regService, lcm, null);

        lcmFac = bu.lcmFac;
        rimFac = bu.rimFac;
        queryFac = bu.queryFac;
    }

    /**
     * Creates a Query object given a queryType (e.g. SQL)
     * that represents a query in the syntax appropriate for queryType.
     * No query string is passed to this method.  So, user must call
     * DeclarativeQueryManager.executeQuery(query, queryParams)
     * Must throw an InvalidRequestException if the sqlQuery is not valid.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     * @see DeclarativeQueryManager#executeQuery(Query query, Map queryParams)
     */
    public Query createQuery(int queryType)
        throws InvalidRequestException, JAXRException {
        if (queryType != Query.QUERY_TYPE_SQL) {
            throw new InvalidRequestException(
                "Type must be Query.QUERY_TYPE_SQL");
        }

        return new QueryImpl(queryType);
    }
    
    /**
     * Creates a Query object given a queryType (e.g. SQL) and a String
     * that represents a query in the syntax appropriate for queryType.
     * Must throw and InvalidRequestException if the sqlQuery is not valid.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     */
    public Query createQuery(int queryType, String queryString)
        throws InvalidRequestException, JAXRException {
        if ((queryType != Query.QUERY_TYPE_SQL) && (queryType != Query.QUERY_TYPE_EBXML_FILTER_QUERY))  {
            throw new InvalidRequestException(
                "Type must be Query.QUERY_TYPE_SQL or QUERY_TYPE_EBXML_FILTER_QUERY");
        }

        // TODO: check queryString syntax
        return new QueryImpl(queryType, queryString);
    }
    
    /**
     * Execute a query as specified by query paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the queryParams or query parameter is null
     */
    public BulkResponse executeQuery(Query query) throws JAXRException {
        BulkResponse br = executeQuery(query, null);           
        return br;
    }
    
    /**
     * Execute a query as specified by query and parameters paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query. This parameter
     *   is required.
     * @param queryParams
     *   A java.util.Map of query parameters. This parameter is optional.
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the query parameter is null
     */
    public BulkResponse executeQuery(Query query, Map queryParams) throws JAXRException {
        return executeQuery(query, queryParams, new IterativeQueryParams());
    }
    
    //TODO: Add this as interface to JAXR 2.0??   
    public BulkResponse executeQuery(Query query, Map queryParams, 
                                     IterativeQueryParams iterativeParams) 
        throws JAXRException {
        BulkResponse bresp = null;
        
        try {
            if (query == null) {
                throw new NullPointerException("query is null");
            }
            
            AdhocQueryRequestType req = ((QueryImpl) query).toBindingObject();
            ClientRequestContext context = new ClientRequestContext("org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl:executeQuery", req);
            bresp = executeQuery(context, queryParams, iterativeParams);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        return bresp;
    }
    
    //TODO: Add this as interface to JAXR 2.0??   
    /**
     * Execute a query as specified by query and parameters paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query. This parameter
     *   is required.
     * @param queryParams
     *   A java.util.Map of query parameters. This parameter is optional.
     * @param iterativeParams
     *   A IterativeQueryParams instance that holds parameters used in making
     *   iterative queriesClientContext
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the query parameter is null
     */
    public BulkResponse executeQuery(ClientRequestContext context, Map queryParams, 
                                     IterativeQueryParams iterativeParams) 
        throws JAXRException {
        try {
            AdhocQueryRequestType req = (AdhocQueryRequestType) context.getCurrentRegistryRequest();            
            Connection connection = ((RegistryServiceImpl)getRegistryService()).getConnection();
            JAXRUtility.addCreateSessionSlot(req, connection);
            
            // If parameter Map is not null, set parameters on the AdhocQuery object as a
            // slot list
            if (queryParams != null) {                               
                ResponseOptionType responseOption = req.getResponseOption();
                
                //Exract (remove) transient Slots from slotsMap if specified
                String responseOptionReturnComposedObjectSlotValue = (String)queryParams.remove(this.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS);
                String responseOptionReturnTypeSlotValue = (String)queryParams.remove(this.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE);
                
                if (responseOptionReturnTypeSlotValue != null) {
                    ReturnType returnType = ReturnType.fromString(responseOptionReturnTypeSlotValue);
                    responseOption.setReturnType(returnType);
                }
                if (responseOptionReturnComposedObjectSlotValue != null) {
                    boolean returnComposedObjects = Boolean.valueOf(responseOptionReturnComposedObjectSlotValue).booleanValue();
                    responseOption.setReturnComposedObjects(returnComposedObjects);
                }
                
                bu.addSlotsToRequest((RegistryRequestType)req, queryParams);
            }
            // Add iterative query parameters to request
            req.setStartIndex(new BigInteger(String.valueOf(iterativeParams.startIndex)));
            req.setMaxResults(new BigInteger(String.valueOf(iterativeParams.maxResults)));
            
            AdhocQueryResponseType resp = serverQMProxy.submitAdhocQuery(context);            
            BulkResponse br = new BulkResponseImpl(lcm, resp, context.getRepositoryItemsMap());           
            return br;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }


}
