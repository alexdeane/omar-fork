/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/AbstractCatalogingService.java,v 1.1 2005/06/22 00:22:35 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.cms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequestType;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentResponseType;

import javax.xml.soap.SOAPElement;


/**
 * @author Tony Graham
 *
 */
public abstract class AbstractCatalogingService extends AbstractService {
    private static final Log log = LogFactory.getLog(AbstractCatalogingService.class.getName());
    protected static org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
    protected CatalogContentRequestType ccReq = null;
    protected CatalogContentResponseType ccResp = null;
    protected SOAPElement ccRespElement = null;
}
