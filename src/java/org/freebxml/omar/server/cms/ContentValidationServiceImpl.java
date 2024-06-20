/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentValidationServiceImpl.java,v 1.12 2005/11/21 04:27:34 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.Stub;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.jaxrpc.cms.validation.client.ContentValidationServicePortType_Stub;
import org.freebxml.omar.common.jaxrpc.cms.validation.client.ContentValidationServiceSOAPService;
import org.freebxml.omar.common.jaxrpc.cms.validation.client.ContentValidationServiceSOAPService_Impl;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.cms.ValidateContentRequestType;
import org.oasis.ebxml.registry.bindings.cms.ValidateContentResponseType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Content validation service
 * @see
 * @author Tony Graham
 *
 */
public class ContentValidationServiceImpl
    extends AbstractContentValidationService {
    private static final Log log = LogFactory.getLog(ContentValidationServiceImpl.class.getName());

    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        System.err.println("ContentValidationServiceImpl.invoke():: input: " +
            input + "; Service: " + service + "; invocationController: " +
            invocationController + "; user: " + user);

        List serviceBindings = service.getServiceBinding();
        Iterator debugIter = serviceBindings.iterator();

        while (debugIter.hasNext()) {
            ServiceBindingType binding = (ServiceBindingType) debugIter.next();
            System.err.println("URL: " + binding.getAccessURI());
        }

        String accessURI = selectAccessURI(service);

        SOAPElement responseElement;
        Object responseObj;

        try {
            ValidateContentRequestType request = bu.cmsFac.createValidateContentRequest();
            RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
            roList.getIdentifiable().add(input.getRegistryObject());
            request.setOriginalContent(roList);
            System.err.println("InvocationControlFile class: " +
                qm.getRegistryObject(context, invocationController.getEoId()).getClass()
                  .getName());

            // FIXME: Adding existing ExtrinsicObjects with 'request.getInvocationControlFile().add()' gives incorrect serialization.
            ExtrinsicObjectType icfEOT = bu.rimFac.createExtrinsicObjectType();
            icfEOT.setId(invocationController.getEoId());
            request.getInvocationControlFile().add(icfEOT);

            System.out.println("\n\nOriginalContent:");
            printNodeToConsole(bu.getSOAPElementFromBindingObject(
                    input.getRegistryObject()));

            Collection attachments = new ArrayList();

            // RepositoryItem for input to be validated.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    input.getRegistryObject().getId()));

            // RepositoryItem for InvocationControlFile.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    invocationController.getEoId()));

            ContentValidationServiceSOAPService soapService = (ContentValidationServiceSOAPService) new ContentValidationServiceSOAPService_Impl();
            ContentValidationServicePortType_Stub stub = (ContentValidationServicePortType_Stub) soapService.getContentValidationServicePort();
            stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            stub._setProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
                attachments);

            responseElement = stub.validateContent(bu.getSOAPElementFromBindingObject(
                        request));

            responseObj = bu.getBindingObjectFromSOAPElement(responseElement);
        } catch (Exception e) {
            throw new RegistryException(e);
        }

        if (!(responseObj instanceof ValidateContentResponseType)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ValidationContentResponseExpected", 
                    new Object[]{responseElement.getElementName().getQualifiedName()}));
        }

        ValidateContentResponseType response = (ValidateContentResponseType) responseObj;

        String status = response.getStatus();

        System.err.println("Status: " + status);

        if (log.isDebugEnabled()) {
            log.debug("Status: " + status);
        }

        ServiceOutput output = new ServiceOutput();
        output.setErrorList(response.getRegistryErrorList());

        if (status.equals(bu.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success)) {
            output.setOutput(Boolean.TRUE);
        } else if (status.equals(bu.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure)) {
            output.setOutput(Boolean.FALSE);
        } else {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ResponseStatusTypeExpected", new Object[]{status}));
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
}
