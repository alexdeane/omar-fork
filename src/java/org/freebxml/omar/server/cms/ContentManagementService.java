/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentManagementService.java,v 1.7 2005/11/21 04:27:34 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Content management service
 * @see
 * @author Tony Graham
 *
 */
public interface ContentManagementService {
    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException;
}
