/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentCatalogingServiceImpl.java,v 1.14 2007/01/12 21:34:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.rpc.Stub;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.jaxrpc.cms.cataloging.client.ContentCatalogingServicePortType_Stub;
import org.freebxml.omar.common.jaxrpc.cms.cataloging.client.ContentCatalogingServiceSOAPService;
import org.freebxml.omar.common.jaxrpc.cms.cataloging.client.ContentCatalogingServiceSOAPService_Impl;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequestType;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentResponseType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * A proxy Content cataloging service that invokes the actual
 * Content cataloging service that is a SOAP endpoint.
 *
 * TODO: This should be really be renamed to SOAPContentCatalogingService??
 *
 * @author Tony Graham
 *
 */
public class ContentCatalogingServiceImpl
    extends AbstractContentCatalogingService {
    private static final Log log = LogFactory.getLog(ContentCatalogingServiceImpl.class.getName());

    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("ContentCatalogingServiceImpl.invoke():: input: " +
                input + "; Service: " + service + "; invocationController: " +
                invocationController + "; user: " + user);
        }

        String accessURI = selectAccessURI(service);

        SOAPElement responseElement;
        Object responseObj;
        ServiceOutput output = new ServiceOutput();
        ServerRequestContext outputContext = null;
        
        try {
            outputContext = context; //new RequestContext(null);
            CatalogContentRequestType request = bu.cmsFac.createCatalogContentRequest();
            RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
            roList.getIdentifiable().add(input.getRegistryObject());
            request.setOriginalContent(roList);
            System.err.println("InvocationControlFile class: " +
                qm.getRegistryObject(outputContext, invocationController.getEoId()).getClass()
                  .getName());

            ExtrinsicObjectType icfEOT = bu.rimFac.createExtrinsicObjectType();
            icfEOT.setId(invocationController.getEoId());
            request.getInvocationControlFile().add(icfEOT);

            if (log.isDebugEnabled()) {
                log.debug("\n\nOriginalContent:");
                printNodeToConsole(bu.getSOAPElementFromBindingObject(
                        input.getRegistryObject()));
            }

            Collection attachments = new ArrayList();

            // RepositoryItem for input to be cataloged.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    input.getRegistryObject().getId()));

            // RepositoryItem for InvocationControlFile.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    invocationController.getEoId()));

            ContentCatalogingServiceSOAPService soapService = (ContentCatalogingServiceSOAPService) new ContentCatalogingServiceSOAPService_Impl();
            ContentCatalogingServicePortType_Stub stub = (ContentCatalogingServicePortType_Stub) soapService.getContentCatalogingServicePort();
            stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            stub._setProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
                attachments);

            responseElement = stub.catalogContent(bu.getSOAPElementFromBindingObject(
                        request));

            responseObj = bu.getBindingObjectFromSOAPElement(responseElement);

            if (!(responseObj instanceof CatalogContentResponseType)) {
                throw new RegistryException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.WrongResponseReceivedFromCatalogingService", 
                                                    new Object[] {responseElement.getElementName().getQualifiedName()}));
            }

            CatalogContentResponseType response = (CatalogContentResponseType) responseObj;

            String status = response.getStatus();

            if (log.isDebugEnabled()) {
                log.debug("Status: " + status);
            }

            RegistryObjectListType catalogedMetadata = response.getCatalogedContent();

            // TODO: User should refer to "Service object for the
            // Content Management Service that generated the
            // Cataloged Content."
            outputContext.setUser(user);

            bu.getObjectRefsAndRegistryObjects(catalogedMetadata, outputContext.getTopLevelObjectsMap(), outputContext.getObjectRefsMap());
            outputContext.getRepositoryItemsMap().putAll(getRepositoryItems(stub));

            output.setOutput(outputContext);
            output.setErrorList(response.getRegistryErrorList());
        } catch (Exception e) {
            e.printStackTrace();
            if (outputContext != context) { 
                outputContext.rollback();
            }
            throw new RegistryException(e);
        }

        if (outputContext != context) {
            outputContext.commit();
        }
        return output;
    }

    protected static void printNodeToConsole(Node n) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // also print to console
            transformer.transform(new DOMSource(n), new StreamResult(System.out));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    protected HashMap getRepositoryItems(com.sun.xml.rpc.client.StubBase stub)
        throws Exception {
        HashMap repositoryItems = new HashMap();
        
        Collection attachments = (Collection) stub._getProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY);

        if (attachments != null) {
            Iterator attachmentsIter = attachments.iterator();

            while (attachmentsIter.hasNext()) {
                Object obj = attachmentsIter.next();

                System.err.println("getRepositoryItems:: Attachment: " +
                    obj.getClass().getName());

                if (obj instanceof AttachmentPart) {
                    AttachmentPart ap = (AttachmentPart) obj;

                    String contentId = ap.getContentId();
                    String contentType = ap.getContentType();

                    System.err.println("getRepositoryItems:: contentId: " +
                        contentId + "; contentType: " + contentType);


                    repositoryItems.put(contentId, processIncomingAttachment(ap));
                }
            }
        }

        return repositoryItems;
    }

    private RepositoryItem processIncomingAttachment(AttachmentPart ap)
        throws Exception {
        DataHandler dh = null;

        //ContentId is the id of the repositoryItem (CID UIR
        String id = SoapSecurityUtil.convertContentIdToUUID(ap.getContentId());

        if (log.isInfoEnabled()) {
            log.info(ServerResourceBundle.getInstance().getString("message.ProcessingAttachmentWithContentId", new Object[]{id}));
        }
        
        if (log.isDebugEnabled()) {
            log.debug(
                "Processing attachment (RepositoryItem):\n" +
                ap.getContent().toString());
        }
        
        dh = ap.getDataHandler();
        return new RepositoryItemImpl(id, dh);
    }
}
