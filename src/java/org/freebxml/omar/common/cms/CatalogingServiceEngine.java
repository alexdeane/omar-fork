/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/CatalogingServiceEngine.java,v 1.1 2005/06/22 00:22:35 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.common.cms;

import org.freebxml.omar.common.exceptions.CatalogingException;

/**
 * Interface for Catalogin Service Engine Classes.
 *
 * @author Farrukh.Najmi@sun.com
 */
public interface CatalogingServiceEngine {
    public CatalogingServiceOutput catalogContent(CatalogingServiceInput input) throws CatalogingException;
}
