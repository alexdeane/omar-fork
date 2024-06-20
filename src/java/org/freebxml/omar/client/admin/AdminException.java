/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminException.java,v 1.2 2004/11/19 18:35:34 tonygraham Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.admin;

import javax.xml.registry.JAXRException;

/**
 * 
 *
 * @author     Tony Graham
 *
 */
public class AdminException extends JAXRException {

    /**
     * Construct an AdminException with no reason.
     *
     *
     * @see javax.xml.registry.JAXRException
     */
    public AdminException() {
        super();
    }

    /**
     * Construct an AdminException with a reason .
     *
     *
     * @param reason
     *
     * @see javax.xml.registry.JAXRException
     */
    public AdminException(String reason) {
        super(reason);
    }

    /**
     * Construct an AdminException with the embedded exception and the
     * reason for it.
     *
     *
     * @param reason
     * @param exception
     *
     * @see javax.xml.registry.JAXRException
     */
    public AdminException(String reason, Exception exception) {
        super(reason, exception);
    }

    /**
     * Construct an AdminException with the embedded exception.
     *
     *
     * @param exception
     *
     * @see javax.xml.registry.JAXRException
     */
    public AdminException(Exception exception) {
	super(exception);
    }
}
