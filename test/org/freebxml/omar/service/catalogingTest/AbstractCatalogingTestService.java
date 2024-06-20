/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/service/catalogingTest/AbstractCatalogingTestService.java,v 1.2 2005/06/22 01:11:25 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.service.catalogingTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.cms.AbstractService;

import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequestType;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentResponseType;

import javax.xml.soap.SOAPElement;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractCatalogingTestService extends AbstractService {
    private static final Log log = LogFactory.getLog(AbstractCatalogingTestService.class.getName());
    protected static org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
    protected CatalogContentRequestType ccReq = null;
    protected CatalogContentResponseType ccResp = null;
    protected SOAPElement ccRespElement = null;
}
