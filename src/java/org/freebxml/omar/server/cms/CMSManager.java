/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/CMSManager.java,v 1.4 2005/11/21 04:27:33 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;


/**
 * Concrete Content Management Service implementation.  For each
 * registry object in a <code>RequestContext</code>, the
 * <code>CMSManager</code> determines the applicable content
 * management service or services to use and invokes them.
 */
public interface CMSManager {
    /**
     * Invokes appropriate Content Management Services for the content
     * in the <code>RequestContext</code>.
     *
     * @param context a <code>RequestContext</code> value
     */
    public void invokeServices(ServerRequestContext context) throws RegistryException;
}
