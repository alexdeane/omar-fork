/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/AbstractCMSManager.java,v 1.2 2004/11/12 10:32:16 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;


/**
 * Abstract superclass of cataloging and validation (and other)
 * services managers.
 * @see
 * @author Tony Graham
 *
 */
public abstract class AbstractCMSManager implements CMSManager {
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
}
