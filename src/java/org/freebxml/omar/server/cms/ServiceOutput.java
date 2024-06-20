/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ServiceOutput.java,v 1.4 2004/11/30 21:55:30 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;


/**
 * Output of a content management service invocation.
 * <p>
 * The type of the result depends on the type of the content
 * management service.  For example, the output from invoking a
 * content validation service is an indication of success or failure,
 * but the output from invoking a content cataloging service is
 * created and/or updated RegistryObject metadata.
 */
public class ServiceOutput {
    private Object output;
    private RegistryErrorListType errorList;

    public ServiceOutput() {
        //
    }

    public ServiceOutput(Object output, RegistryErrorListType errorList) {
        this.setOutput(output);
        this.setErrorList(errorList);
    }

    /**
     * Get the output value.
     * @return the output value.
     */
    public Object getOutput() {
        return output;
    }

    /**
     * Set the output value.
     * @param newOutput The new output value.
     */
    public void setOutput(Object newOutput) {
        this.output = newOutput;
    }

    public RegistryErrorListType getErrorList() {
        return errorList;
    }

    public void setErrorList(RegistryErrorListType errorList) {
        this.errorList = errorList;
    }
}
