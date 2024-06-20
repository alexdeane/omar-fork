/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/CatalogingServiceOutput.java,v 1.2 2005/10/13 23:53:34 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.common.cms;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import javax.activation.DataHandler;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Simple wrapper class for all input parameters to a Cataloging Service
 *
 * @author Farrukh.Najmi@sun.com
 */
public class CatalogingServiceOutput extends CMSOutput {

    private DataHandler repositoryItem = null;
    private Set registryObjects = new HashSet();
    private Map idToRepositoryItemMap = null;
    
    private CatalogingServiceOutput() {}
    
    public CatalogingServiceOutput(Set registryObjects, Map idToRepositoryItemMap) {
        this.registryObjects = registryObjects;
        this.idToRepositoryItemMap = idToRepositoryItemMap;
    }
    
    public Map getRepositoryItemMap() {
        return idToRepositoryItemMap;
    }

    public Set getRegistryObjects() {
        return registryObjects;
    }

}
