/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/rest/LifeCycleManagerURLHandler.java,v 1.4 2005/02/23 22:57:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.RegistryException;

/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class LifeCycleManagerURLHandler extends URLHandler {
    
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();


    protected LifeCycleManagerURLHandler() {
    }
    
    LifeCycleManagerURLHandler(HttpServletRequest request, HttpServletResponse response) throws RegistryException {
            
        super(request, response);
    }       
    
}
