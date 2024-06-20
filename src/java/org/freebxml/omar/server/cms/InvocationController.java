/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/InvocationController.java,v 1.4 2004/11/19 17:54:33 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import javax.activation.DataHandler;

import javax.xml.transform.stream.StreamSource;


/**
 * Information to control an invocation of a Content Management
 * Service.  Also includes sufficient information to be able to
 * meaningfully sort instances of <code>InvocationController</code> so
 * services can be invoked in consistent order.
 *
 * @author  tkg
 */
public class InvocationController {
    /** Id of the 'InvocationControlFileFor' association subtype of
     * this invocation controller. */
    String controlFileForAssocId;

    /** Id of the corresponding ExtrinsicObject (and RepositoryItem)
     * in the registry/repository. */
    String eoId;

    /** Creates a new instance of InvocationController */
    public InvocationController(String controlFileForAssocId, String eoId) {
        this.controlFileForAssocId = controlFileForAssocId;
        this.eoId = eoId;
    }

    /**
     * Get the ControlFileForAssocId value.
     * @return the ControlFileForAssocId value.
     */
    public String getControlFileForAssocId() {
        return controlFileForAssocId;
    }

    /**
     * Set the ControlFileForAssocId value.
     * @param newControlFileForAssocId The new ControlFileForAssocId value.
     */
    public void setControlFileForAssocId(String newControlFileForAssocId) {
        this.controlFileForAssocId = newControlFileForAssocId;
    }

    /**
     * Gets the Id of the ExtrinsicObject for the InvocationControlFile.
     * @return the eoId value.
     */
    public String getEoId() {
        return eoId;
    }

    /**
     * Sets eoId value.  This is the Id of the ExtrinsicObject for the
     * InvocationControlFile.
     * @param newEoId The new eoId value.
     */
    public void setEoId(String newEoId) {
        this.eoId = newEoId;
    }
}
