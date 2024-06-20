/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/QueryManagerLocalProxy.java,v 1.16 2006/04/07 17:22:17 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.RepositoryItemNotFoundException;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.common.spi.RequestContext;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


public class QueryManagerLocalProxy implements QueryManager {
    
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private static BindingUtility bu = BindingUtility.getInstance();
    private UserType callersUser = null;
    
    
    private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    private SOAPMessenger msgr = null;
    
    public QueryManagerLocalProxy(String registryURL, CredentialInfo credentialInfo) {
        // DBH 4/8/04 - Seems like this was needed, otherwise getCallersUser()
        // always returned null.
        this.credentialInfo = credentialInfo;
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public AdhocQueryResponseType submitAdhocQuery(RequestContext context) throws 
         RegistryException {
        AdhocQueryResponseType resp = null;
         context.setUser(getCallersUser());
         resp = qm.submitAdhocQuery(context);
         bu.convertRepositoryItemMapForClient(context.getRepositoryItemsMap());
         
         return resp;
    }
    
    public RegistryObjectType getRegistryObject(RequestContext context, String id) throws RegistryException {
        return getRegistryObject(context, id, "RegistryObject");
    }
    
    public RegistryObjectType getRegistryObject(RequestContext context, String id, String typeName) throws RegistryException {
        RegistryObjectType ro = null;
        try {
            typeName = org.freebxml.omar.common.Utility.getInstance().mapTableName(typeName);
            
            HashMap queryParams = new HashMap();
            queryParams.put("$id", id);
            queryParams.put("$tableName", typeName);
            AdhocQueryRequestType req = bu.createAdhocQueryRequest("urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType", queryParams);

            context.pushRegistryRequest(req);
            AdhocQueryResponseType resp = submitAdhocQuery(context);
            
            RegistryResponseHolder respHolder = new RegistryResponseHolder(resp, null);
            List results = respHolder.getCollection();
            if (results.size() == 1) {
                ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) results.get(0);
            }
        }
        catch (RegistryException e) {
            throw e;
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        } finally {
            context.popRegistryRequest();
        }

        return ro;
    }
    
    public RepositoryItem getRepositoryItem(RequestContext context, String id) throws RegistryException {
        RepositoryItem ri = null;

	try {
	    ri = qm.getRepositoryItem(context, id);
	} catch (RepositoryItemNotFoundException ex) {
	    // Ignore exception: Unexpected for JAXR 1.0, fine in JAXR 2.0
	    // Universally, client code expects null return in this case.
	}

        return ri;
    }
    
    private UserType getCallersUser() throws RegistryException {
        X509Certificate cert = null;
        if (credentialInfo != null) {
            cert = credentialInfo.cert;
        }
        return getUser(cert);
    }
            
    /**
     * Looks up the server side User object based upon specified public key certificate.
     */
    public UserType getUser(X509Certificate cert) throws RegistryException {
        
        callersUser = qm.getUser(cert);
        
        return callersUser;
    }
        
    
}
