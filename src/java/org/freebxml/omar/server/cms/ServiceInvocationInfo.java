/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ServiceInvocationInfo.java,v 1.3 2004/11/12 10:32:16 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import org.oasis.ebxml.registry.bindings.rim.ServiceType;

import java.lang.reflect.Constructor;


/**
 * Information about one combination of service and invocation
 * controller.  Used when invoking a content management service on
 * the service input.
 */
public class ServiceInvocationInfo {
    ServiceType service;
    Constructor constructor;
    InvocationController invocationController;
    CMSTypeManager manager;

    public ServiceInvocationInfo(ServiceType service, Constructor constructor,
        InvocationController invocationController, CMSTypeManager manager) {
        this.service = service;
        this.constructor = constructor;
        this.invocationController = invocationController;
        this.manager = manager;
    }

    /**
     * Get the Service value.
     * @return the Service value.
     */
    public ServiceType getService() {
        return service;
    }

    /**
     * Set the Service value.
     * @param newService The new Service value.
     */
    public void setService(ServiceType newService) {
        this.service = newService;
    }

    /**
     * Get the Constructor value.
     * @return the Constructor value.
     */
    public Constructor getConstructor() {
        return constructor;
    }

    /**
     * Set the Constructor value.
     * @param newConstructor The new Constructor value.
     */
    public void setConstructor(Constructor newConstructor) {
        this.constructor = newConstructor;
    }

    /**
     * Get the InvocationController value.
     * @return the InvocationController value.
     */
    public InvocationController getInvocationController() {
        return invocationController;
    }

    /**
     * Set the InvocationController value.
     * @param newInvocationController The new InvocationController value.
     */
    public void setInvocationController(
        InvocationController newInvocationController) {
        this.invocationController = newInvocationController;
    }

    /**
     * Get the manager value.
     * @return the manager value.
     */
    public CMSTypeManager getManager() {
        return manager;
    }

    /**
     * Set the manager value.
     * @param newManager The new manager value.
     */
    public void setManager(CMSTypeManager newManager) {
        this.manager = newManager;
    }
}
