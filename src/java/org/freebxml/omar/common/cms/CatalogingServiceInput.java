/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/CatalogingServiceInput.java,v 1.1 2005/06/22 00:22:35 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.common.cms;

import javax.activation.DataHandler;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Simple wrapper class for all input parameters to a Cataloging Service
 *
 * @author Farrukh.Najmi@sun.com
 */
public class CatalogingServiceInput extends CMSInput {

    public CatalogingServiceInput(DataHandler invocationControlFile, DataHandler repositoryItem, RegistryObjectType registryObject) {
        super(invocationControlFile, repositoryItem, registryObject);
    }
    
}
