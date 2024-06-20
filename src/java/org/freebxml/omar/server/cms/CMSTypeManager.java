/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/CMSTypeManager.java,v 1.6 2005/11/21 04:27:33 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.server.common.ServerRequestContext;

import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;

import java.lang.reflect.Constructor;


/**
 * Content Management Service manager
 */
public interface CMSTypeManager {
    /**
     * Invokes appropriate Content Management Services for the
     * in the <code>RegistryObject</code>.
     *
     * @param ro a <code>RegistryObject</code> value
     * @param ri a <code>RepositoryItem</code> value
     */
    public boolean invokeServiceForObject(ServiceInvocationInfo sii,
        RegistryObjectType ro, RepositoryItem ri, ServerRequestContext context) throws RegistryException;
}
