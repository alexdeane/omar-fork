/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/QueryManagerSOAPProxy.java,v 1.19 2006/04/07 17:22:17 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.RequestContext;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class QueryManagerSOAPProxy implements QueryManager {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    private static BindingUtility bu = BindingUtility.getInstance();
    private String registryURL ;
    private CredentialInfo credentialInfo ;
    private SOAPMessenger msgr ;
    
    public QueryManagerSOAPProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public AdhocQueryResponseType submitAdhocQuery(RequestContext context) throws 
        RegistryException {
                  
        try {
            RegistryResponseHolder resp = submitAdhocQueryInternal(context);
            RegistryResponseType ebResp = resp.getRegistryResponse();

            return (AdhocQueryResponseType)ebResp;
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }        
    }
    
    private RegistryResponseHolder submitAdhocQueryInternal(RequestContext context) throws 
        RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        RegistryResponseHolder resp = null;
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = bu.queryFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            resp = msgr.sendSoapRequest(sw.toString());
            
            RegistryResponseType ebResp = resp.getRegistryResponse();
            bu.checkRegistryResponse(ebResp);            
            context.setRepositoryItemsMap(resp.getAttachmentsMap());
                        
            return resp;
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
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
            RegistryResponseHolder respHolder = submitAdhocQueryInternal(context);

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
        org.freebxml.omar.common.RepositoryItem repositoryItem = null;
                
        try {
            String queryStr = "SELECT * from ExtrinsicObject WHERE id='" + id + "'";
            AdhocQueryRequestType req = bu.createAdhocQueryRequest(queryStr);
            
            ResponseOption respOption = bu.queryFac.createResponseOption();
            respOption.setReturnComposedObjects(true);
            respOption.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
            req.setResponseOption(respOption);

            context.pushRegistryRequest(req);
            RegistryResponseHolder respHolder = submitAdhocQueryInternal(context);

            HashMap attachmentsMap = respHolder.getAttachmentsMap();
            if ((attachmentsMap != null) && attachmentsMap.containsKey(id)) {
                repositoryItem = new RepositoryItemImpl(id, (DataHandler) attachmentsMap.get(id));
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
        return repositoryItem;
    }
    
    public UserType getUser(X509Certificate cert) throws RegistryException {
        throw new RegistryException(resourceBundle.getString("message.unimplemented"));
    }
    
}
