/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/validationTest/cppaValidation/CPPAValidation.java,v 1.4 2004/12/15 15:24:29 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.validationTest.cppaValidation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.jaxrpc.cms.validation.server.ContentValidationServicePortType;
import org.freebxml.omar.service.validationTest.AbstractValidationTestService;

import org.oasis.ebxml.registry.bindings.cms.ValidateContentRequestType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;

import java.io.StringWriter;

import java.rmi.RemoteException;

import java.util.HashMap;

import javax.activation.DataHandler;

import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CPPAValidation extends AbstractValidationTestService
    implements ContentValidationServicePortType {
    private static final Log log = LogFactory.getLog(CPPAValidation.class.getName());
    static final String errorCodeContext = "CPPAValidation.validateContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        try {
            if (log.isDebugEnabled()) {
                printNodeToConsole(partValidateContentRequest);
            }

            HashMap repositoryItemDHMap = getRepositoryItemDHMap();

            if (log.isDebugEnabled()) {
                log.debug("Attachments: " + repositoryItemDHMap.size());
            }

            Object requestObj = getBindingObjectFromNode(partValidateContentRequest);

            if (!(requestObj instanceof ValidateContentRequestType)) {
                throw new Exception(
                    "Wrong response received from validation service.  Expected ValidationContentRequest, got: " +
                    partValidateContentRequest.getElementName()
                                              .getQualifiedName());
            }

            vcReq = (ValidateContentRequestType) requestObj;

            IdentifiableType originalContentIT = (IdentifiableType) vcReq.getOriginalContent()
                                                                         .getIdentifiable()
                                                                         .get(0);
            IdentifiableType invocationControlIT = (IdentifiableType) vcReq.getInvocationControlFile()
                                                                           .get(0);

            DataHandler originalContentDH = (DataHandler) repositoryItemDHMap.get(originalContentIT.getId());
            DataHandler invocationControlDH = (DataHandler) repositoryItemDHMap.get(invocationControlIT.getId());

            if (log.isDebugEnabled()) {
                log.debug("originalContentIT id: " + originalContentIT.getId());
                log.debug("invocationControlIT id: " +
                    invocationControlIT.getId());
            }

            StreamSource invocationControlSrc = new StreamSource(invocationControlDH.getInputStream());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(invocationControlSrc);

            StringWriter sw = new StringWriter();
            transformer.transform(new StreamSource(
                    originalContentDH.getInputStream()), new StreamResult(sw));

            vcResp = cmsFac.createValidateContentResponse();

            boolean success = Boolean.valueOf(sw.toString()).booleanValue();

            if (success) {
                vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            } else {
                RegistryError re = rsFac.createRegistryError();
                re.setValue(sw.toString());
                re.setCodeContext(errorCodeContext);
                re.setErrorCode("InvalidContentException");

                RegistryErrorList el = rsFac.createRegistryErrorList();
                el.getRegistryError().add(re);
                el.setHighestSeverity("Failure");

                vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
                vcResp.setRegistryErrorList(el);
            }

            vcRespElement = getSOAPElementFromBindingObject(vcResp);
        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return vcRespElement;
    }
}
