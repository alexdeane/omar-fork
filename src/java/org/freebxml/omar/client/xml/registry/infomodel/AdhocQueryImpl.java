/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implementation of the AdhocQueryType.
 * @author <a href="mailto:Paul.Sterk@Sun.COM">Paul Sterk</a>
 */
public class AdhocQueryImpl extends RegistryObjectImpl implements Query {

    private int queryType;
    private String queryString="";
    
    public AdhocQueryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public AdhocQueryImpl(LifeCycleManagerImpl lcm,
        AdhocQueryType adhocQueryObj) throws JAXRException {
        super(lcm, adhocQueryObj);

        QueryExpressionType queryExp = adhocQueryObj.getQueryExpression();
        String queryLang = queryExp.getQueryLanguage();
        if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
            queryString = (String)queryExp.getContent().get(0);
        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unsupported.querylanguage", new Object[] {queryLang}));
        }
    }
    
    public int getType() {
        return queryType;
    }
    
    public void setType(int queryType) {
        this.queryType = queryType;
    }
    
    public String toString() {
        return queryString ;
    }
    
    public void setString(String queryString) {
        this.queryString = queryString;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.AdhocQuery ebBinding = 
                factory.createAdhocQuery();
            
            setBindingObject(ebBinding);
            
            QueryExpressionType qeType = factory.createQueryExpression();
            qeType.setQueryLanguage(getQueryTypeId());
            qeType.getContent().add(queryString);
            ebBinding.setQueryExpression(qeType);

            return ebBinding;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    private String getQueryTypeId() {
        String queryTypeBinding = null;
        
        switch (queryType) {
            case Query.QUERY_TYPE_SQL:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92;
                break;
            
            case Query.QUERY_TYPE_EBXML_FILTER_QUERY:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery;
                break;
            
            case Query.QUERY_TYPE_XQUERY:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_XQuery;
                break;
        }
        return queryTypeBinding;
    }
    
}
