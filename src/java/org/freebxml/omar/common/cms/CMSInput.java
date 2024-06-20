/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/CMSInput.java,v 1.1 2005/06/22 00:22:35 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.common.cms;

import javax.activation.DataHandler;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Simple wrapper class for all input parameters to a Content Management Service
 *
 * @author Farrukh.Najmi@sun.com
 */
public abstract class CMSInput {
    private DataHandler invocationControlFile = null;
    private DataHandler repositoryItem = null;
    private RegistryObjectType registryObject = null;    

    public CMSInput(DataHandler invocationControlFile, DataHandler repositoryItem, RegistryObjectType registryObject) {
        this.invocationControlFile = invocationControlFile;
        this.repositoryItem = repositoryItem;
        this.setRegistryObject(registryObject);            
    }
    
    public DataHandler getInvocationControlFile() {
        return invocationControlFile;
    }

    public void setInvocationControlFile(DataHandler invocationControlFile) {
        this.invocationControlFile = invocationControlFile;
    }

    public DataHandler getRepositoryItem() {
        return repositoryItem;
    }

    public void setRepositoryItem(DataHandler repositoryItem) {
        this.repositoryItem = repositoryItem;
    }

    public RegistryObjectType getRegistryObject() {
        return registryObject;
    }

    public void setRegistryObject(RegistryObjectType registryObject) {
        this.registryObject = registryObject;
    }
}
