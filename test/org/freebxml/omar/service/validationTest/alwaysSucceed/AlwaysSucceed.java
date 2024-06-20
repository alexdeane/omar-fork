/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/validationTest/alwaysSucceed/AlwaysSucceed.java,v 1.2 2004/12/15 15:26:25 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.validationTest.alwaysSucceed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.jaxrpc.cms.validation.server.ContentValidationServicePortType;
import org.freebxml.omar.service.validationTest.AbstractValidationTestService;

import java.io.IOException;
import java.io.PrintWriter;

import java.rmi.RemoteException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.soap.SOAPElement;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AlwaysSucceed extends AbstractValidationTestService
    implements ContentValidationServicePortType {
    private static final Log log = LogFactory.getLog(AlwaysSucceed.class.getName());

    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        try {
            vcResp = cmsFac.createValidateContentResponse();
            vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            vcRespElement = getSOAPElementFromBindingObject(vcResp);
        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return vcRespElement;
    }
}
