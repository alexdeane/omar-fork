/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/catalogingTest/cppaCataloging/CPPACataloging.java,v 1.1 2004/12/15 15:22:59 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.catalogingTest.cppaCataloging;

import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.jaxrpc.cms.cataloging.server.ContentCatalogingServicePortType;
import org.freebxml.omar.service.catalogingTest.AbstractCatalogingTestService;

import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequestType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;

import java.io.StringWriter;

import java.rmi.RemoteException;
import java.util.Collection;

import java.util.HashMap;

import javax.activation.DataHandler;
import javax.xml.bind.util.JAXBSource;
import javax.xml.rpc.handler.MessageContext;

import javax.xml.soap.SOAPElement;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CPPACataloging extends AbstractCatalogingTestService
    implements ContentCatalogingServicePortType {
    private static final Log log = LogFactory.getLog(CPPACataloging.class.getName());
    static final String errorCodeContext = "CPPACataloging.catalogContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement catalogContent(SOAPElement partCatalogContentRequest)
        throws RemoteException {
        try {
            if (log.isDebugEnabled()) {
                printNodeToConsole(partCatalogContentRequest);
            }

            final HashMap repositoryItemDHMap = getRepositoryItemDHMap();

            if (log.isDebugEnabled()) {
                log.debug("Attachments: " + repositoryItemDHMap.size());
            }

            Object requestObj = getBindingObjectFromNode(partCatalogContentRequest);

            if (!(requestObj instanceof CatalogContentRequestType)) {
                throw new Exception(
                    "Wrong response received from validation service.  Expected CatalogContentRequest, got: " +
                    partCatalogContentRequest.getElementName().getQualifiedName());
            }

            ccReq = (CatalogContentRequestType) requestObj;

            IdentifiableType originalContentIT = (IdentifiableType) ccReq.getOriginalContent()
                                                                         .getIdentifiable()
                                                                         .get(0);
            IdentifiableType invocationControlIT = (IdentifiableType) ccReq.getInvocationControlFile()
                                                                           .get(0);

            DataHandler invocationControlDH = (DataHandler) repositoryItemDHMap.get(invocationControlIT.getId());

            if (log.isDebugEnabled()) {
                log.debug("originalContentIT id: " + originalContentIT.getId());
                log.debug("invocationControlIT id: " +
                    invocationControlIT.getId());
            }

            StreamSource invocationControlSrc = new StreamSource(invocationControlDH.getInputStream());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(invocationControlSrc);

            transformer.setURIResolver(new URIResolver() {
                public Source resolve(String href,
                    String base)
                throws TransformerException {
                    Source source = null;
                    try {
                        // Should this check that href is UUID URN first?
                        source = new StreamSource(((DataHandler) repositoryItemDHMap.get(href)).getInputStream());
                    } catch (Exception e) {
                        source = null;
                    }
            
                    return source;
                }
            });
            transformer.setErrorListener(new ErrorListener() {
                    public void error(TransformerException exception)
                        throws TransformerException {
                        log.info(exception);
                    }

                    public void fatalError(TransformerException exception)
                        throws TransformerException {
                        log.error(exception);
                        throw exception;
                    }

                    public void warning(TransformerException exception)
                        throws TransformerException {
                        log.info(exception);
                    }
                });

            //Set respository item as parameter
            transformer.setParameter("repositoryItem",
                originalContentIT.getId());

            StringWriter sw = new StringWriter();
            transformer.transform(new JAXBSource(jaxbContext, originalContentIT), new StreamResult(sw));

            ccResp = cmsFac.createCatalogContentResponse();

            RegistryObjectListType catalogedMetadata = (RegistryObjectListType) getUnmarshaller()
                                                                                  .unmarshal(new StreamSource(new StringReader(sw.toString())));
            RegistryObjectListType roList = rimFac.createRegistryObjectListType();
            ccResp.setCatalogedContent(roList);
            // FIXME: Setting catalogedMetadata as CatalogedContent results in incorrect serialization.
            roList.getIdentifiable().addAll(catalogedMetadata.getIdentifiable());
            
            ccResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            
            ccRespElement = getSOAPElementFromBindingObject(ccResp);
            
            // Copy request's attachments to response to exercise attachment-processing code on client.
            MessageContext mc = servletEndpointContext.getMessageContext();
            mc.setProperty(com.sun.xml.rpc.server.ServerPropertyConstants.SET_ATTACHMENT_PROPERTY, (Collection) mc.getProperty(com.sun.xml.rpc.server.ServerPropertyConstants.GET_ATTACHMENT_PROPERTY));

        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return ccRespElement;
    }
}
