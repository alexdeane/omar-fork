/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ServiceInput.java,v 1.2 2004/11/12 10:32:16 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import org.freebxml.omar.common.RepositoryItem;

import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 * Input to a content management service invocation.
 */
public class ServiceInput {
    RegistryObjectType registryObject;
    RepositoryItem repositoryItem;

    public ServiceInput(RegistryObjectType registryObject,
        RepositoryItem repositoryItem) {
        this.registryObject = registryObject;
        this.repositoryItem = repositoryItem;
    }

    /**
     * Get the RegistryObject value.
     * @return the RegistryObject value.
     */
    public RegistryObjectType getRegistryObject() {
        return registryObject;
    }

    /**
     * Set the RegistryObject value.
     * @param newRegistryObject The new RegistryObject value.
     */
    public void setRegistryObject(RegistryObjectType newRegistryObject) {
        this.registryObject = newRegistryObject;
    }

    /**
     * Get the RepositoryItem value.
     * @return the RepositoryItem value.
     */
    public RepositoryItem getRepositoryItem() {
        return repositoryItem;
    }

    /**
     * Set the RepositoryItem value.
     * @param newRepositoryItem The new RepositoryItem value.
     */
    public void setRepositoryItem(RepositoryItem newRepositoryItem) {
        this.repositoryItem = newRepositoryItem;
    }
}
