/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/validationTest/throwRemoteException/ThrowRemoteException.java,v 1.2 2004/12/15 15:28:24 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.validationTest.throwRemoteException;

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
public class ThrowRemoteException implements ContentValidationServicePortType {
    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        throw new RemoteException(
            "This Service always throws a RemoteException.");
    }
}
