/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/QueryImpl.java,v 1.15 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.List;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.bind.JAXBException;
import javax.xml.registry.UnsupportedCapabilityException;

import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQuery;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named Query.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class QueryImpl implements Query {
    
    private AdhocQueryRequestType req = null;
    private static BindingUtility bu = BindingUtility.getInstance();        
    
    /**
     * This constant is added to support new stored query. This queryType
     * involves the use of the XPath query language.
     *
     * TODO JAXR 2.0: this constant will be added to javax.xml.registry.Query for 
     * JAXR 2.0. After that, remove from this constant from the class.
     */
    private static int qtFilterQuery = QUERY_TYPE_EBXML_FILTER_QUERY;
    public static final int QUERY_TYPE_XPATH = (qtFilterQuery + 1);
    
    public QueryImpl(int queryType) 
        throws JAXRException {
        
        try {
            this.req = bu.queryFac.createAdhocQueryRequest();
            req.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            AdhocQuery adhocQuery = bu.rimFac.createAdhocQuery();
            adhocQuery.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
            adhocQuery.setQueryExpression(queryExp);
            queryExp.setQueryLanguage(mapQueryTypeFromJAXR2ebXML(queryType));
            //queryExp.getContent().add(queryStr);

            req.setAdhocQuery(adhocQuery);

            ResponseOption ro = bu.queryFac.createResponseOption();
            ro.setReturnComposedObjects(true);
            
            //Do not specify LEAF_CLASS_WITH_REPOSITORY ITEM by default as repositoryitems are fetched lazily.
            ro.setReturnType(ReturnType.LEAF_CLASS);
            req.setResponseOption(ro);
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
     
    public QueryImpl(int queryType, String queryString) 
        throws JAXRException {
        // The queryString is null, set to empty string. Reason: 
        // AdhocQueryRequestType cannot have a null QueryExpression
        //if (queryString == null) {
        //    queryString = "";
        //}
          
        try {    
            this.req = bu.queryFac.createAdhocQueryRequest();
            req.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            AdhocQuery adhocQuery = bu.rimFac.createAdhocQuery();
            adhocQuery.setId(org.freebxml.omar.common.Utility.getInstance().createId());

            QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
            adhocQuery.setQueryExpression(queryExp);
            queryExp.setQueryLanguage(mapQueryTypeFromJAXR2ebXML(queryType));

            if (queryString != null) {
                queryExp.getContent().add(queryString);
            }

            req.setAdhocQuery(adhocQuery);

            ResponseOption ro = bu.queryFac.createResponseOption();
            ro.setReturnComposedObjects(true);

            //Do not specify LEAF_CLASS_WITH_REPOSITORY ITEM by default as repositoryitems are fetched lazily.
            ro.setReturnType(ReturnType.LEAF_CLASS);
            req.setResponseOption(ro);        
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    public QueryImpl(AdhocQueryRequestType req) 
        throws JAXRException {

        this.req = req;
    }
    
    /*
     * Sets whether this is a federated query or not.
     * TODO: Add to JAXR 2.0
     */
    public void setFederated(boolean federated) throws JAXRException {
        req.setFederated(federated);
    }
    
    /*
     * Determines whether this is a federated query or not.
     * TODO: Add to JAXR 2.0
     */
    public boolean isFederated() throws JAXRException {
        return req.isFederated();
    }
    
    /*
     * Sets the federation id for a federated.
     * TODO: Add to JAXR 2.0
     */
    public void setFederation(String federationId) throws JAXRException  {
        req.setFederation(federationId);
        if ((federationId != null) && (!isFederated())) {
            setFederated(true);
        }
    }
    
    public String getFederation() {
        return req.getFederation();
    }
    
    /**
     * Gets the type of Query (e.g. QUERY_TYPE_SQL)
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     * @return the type of query
     */
    public int getType() throws JAXRException {
        String queryLanguageConceptId = req.getAdhocQuery().getQueryExpression().getQueryLanguage();        
        return mapQueryTypeFromebXML2JAXR(queryLanguageConceptId);
    }

    /**
     * Must print the String representing the query. For example
     * in case of SQL query prints the SQL query as a string.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     */
    public String toString() {
        String queryString = null;
        List content = req.getAdhocQuery().getQueryExpression().getContent();  
        if (content.size() >= 1) {
            Object obj = content.get(0);
            if (obj instanceof String) {
                queryString = (String)obj;
            }
        }
        return queryString;
    }
    

    /**
     * This method is used to return a AdhocQueryRequestType object that is
     * bound to this class.
     *
     * @return
     *   An AdhocQueryRequestType instance
     * @throws JAXBException
     * @throws JAXRException
     */
    public AdhocQueryRequestType toBindingObject() throws JAXBException, JAXRException {           
        return req;
    }
    
    private static int mapQueryTypeFromebXML2JAXR(String queryLanguageConceptId) throws JAXRException {
        if (queryLanguageConceptId.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery)) {
            return Query.QUERY_TYPE_EBXML_FILTER_QUERY;
        } else if (queryLanguageConceptId.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
            return Query.QUERY_TYPE_SQL;
        } else {
            throw new UnsupportedCapabilityException(JAXRResourceBundle.getInstance().getString("message.error.query.type",new Object[] {queryLanguageConceptId}));
        }
    }
    
    private static String mapQueryTypeFromJAXR2ebXML(int jaxrQueryType) throws JAXRException {
        if (jaxrQueryType == Query.QUERY_TYPE_EBXML_FILTER_QUERY) {
            return BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery;
        } else if (jaxrQueryType == Query.QUERY_TYPE_SQL) {
            return BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92;
        } else {
            throw new UnsupportedCapabilityException(JAXRResourceBundle.getInstance().getString("message.error.query.type",new Object[] {new Integer(jaxrQueryType)}));
        }
    }
    
}
