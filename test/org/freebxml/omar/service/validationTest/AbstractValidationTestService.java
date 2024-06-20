/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/validationTest/AbstractValidationTestService.java,v 1.5 2005/06/22 00:44:34 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.validationTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.cms.AbstractService;

import org.oasis.ebxml.registry.bindings.cms.ValidateContentRequestType;
import org.oasis.ebxml.registry.bindings.cms.ValidateContentResponseType;

import javax.xml.soap.SOAPElement;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractValidationTestService extends AbstractService {
    private static final Log log = LogFactory.getLog(AbstractValidationTestService.class.getName());
    protected static org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
    protected ValidateContentRequestType vcReq = null;
    protected ValidateContentResponseType vcResp = null;
    protected SOAPElement vcRespElement = null;
}
