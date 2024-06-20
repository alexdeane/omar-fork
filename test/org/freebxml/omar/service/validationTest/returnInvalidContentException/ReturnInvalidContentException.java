/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/validationTest/returnInvalidContentException/ReturnInvalidContentException.java,v 1.2 2004/12/15 15:26:24 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.validationTest.returnInvalidContentException;

import org.freebxml.omar.common.jaxrpc.cms.validation.server.ContentValidationServicePortType;
import org.freebxml.omar.service.validationTest.AbstractValidationTestService;

import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;

import java.rmi.RemoteException;

import javax.xml.soap.SOAPElement;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReturnInvalidContentException extends AbstractValidationTestService
    implements ContentValidationServicePortType {
    static final String errorCodeContext = "ReturnInvalidContentException.validateContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        try {
            RegistryError re = rsFac.createRegistryError();
            re.setValue("InvalidContentException");
            re.setCodeContext(errorCodeContext);
            re.setErrorCode("InvalidContentException");

            RegistryErrorList el = rsFac.createRegistryErrorList();
            el.getRegistryError().add(re);
            el.setHighestSeverity("Failure");

            vcResp = cmsFac.createValidateContentResponse();
            vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
            vcResp.setRegistryErrorList(el);

            vcRespElement = getSOAPElementFromBindingObject(vcResp);
        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return vcRespElement;
    }
}
