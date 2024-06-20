/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/rest/UserDefinedURLHandler.java,v 1.7 2006/04/21 21:59:06 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces.rest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.registry.InvalidRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.UnimplementedException;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class UserDefinedURLHandler extends URLHandler {
    
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();


    protected UserDefinedURLHandler() {
    }
    
    UserDefinedURLHandler(HttpServletRequest request,
        HttpServletResponse response) throws RegistryException {
            
        super(request, response);
    }    
    
    
    /**
     * Processes a Get Request
     */
    void processGetRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {       
        try {
            //First see if URL matches a repositoryItem
            getRepositoryItemByURI();
        }
        catch (ObjectNotFoundException e) {
            //No match on repository item so see if it matches a RegistryObject
            getRegistryObjectByURI();
        }        
    }
    
    
    /**
     * Attempt to get a RepositoryItem by its URI.
     *
     */
    private List getRepositoryItemByURI() 
        throws IOException, RegistryException, ObjectNotFoundException 
    {
        List results = null;
        String pathInfo = request.getPathInfo();
        
        //If path begins with a '/' then we also need to check for same patch but without leading '/'
        //because zip files with relative entry paths when cataloged do not have the leading '/'
        String pathInfo2 = new String(pathInfo);
        if (pathInfo2.startsWith("/")) {
            pathInfo2 = pathInfo2.substring(1);
        }

        try {
            ExtrinsicObjectType ro = null;
            
            String queryString = 
                "SELECT eo.* FROM ExtrinsicObject eo, Slot s " +
                "WHERE (s.value='" + pathInfo + "' OR s.value='" + pathInfo2 + "') AND " +
                    "s.name_='" + BindingUtility.CANONICAL_SLOT_CONTENT_LOCATOR + "' AND s.parent=eo.id";
            
            results = submitQueryAs(queryString, currentUser);
            
            if (results.size() == 0) {
                throw new ObjectNotFoundException(ServerResourceBundle.getInstance().getString("message.noRepositoryItemFound",
                        new Object[]{pathInfo})); 
            } else if (results.size() == 1) {
                ro = (ExtrinsicObjectType) results.get(0);
                writeRepositoryItem(ro);                
            } else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.duplicateRegistryObjects",
                        new Object[]{pathInfo})); 
            }
        } 
        catch (NullPointerException e) {
            e.printStackTrace();
            throw new RegistryException(org.freebxml.omar.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
        }

        return results;
    }

    /**
     * Attempt to get a RegistryObject by its URI.
     *
     */
    private List getRegistryObjectByURI() 
        throws IOException, RegistryException, ObjectNotFoundException  
    {
        List results = null;
        Locale locale = request.getLocale();
        String pathInfo = request.getPathInfo();

        try {
            RegistryObjectType ro = null;
            
            String queryString = 
                "SELECT ro.* FROM RegistryObject ro, Slot s " +
                "WHERE s.value='" + pathInfo + "' AND " +
                    "s.name_='" + BindingUtility.CANONICAL_SLOT_LOCATOR + "' AND s.parent=ro.id";
            
            results = submitQueryAs(queryString, currentUser);

            if (results.size() == 0) {                
                throw new ObjectNotFoundException(
                    ServerResourceBundle.getInstance().getString("message.noRegistryObjectFound",
                        new Object[]{pathInfo})); 
            } else if (results.size() == 1) {
                ro = (RegistryObjectType) results.get(0);
                writeRegistryObject(ro);
            } else {
                throw new RegistryException(
                    ServerResourceBundle.getInstance().getString("message.duplicateRegistryObjects",
                        new Object[]{pathInfo})); 
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new RegistryException(org.freebxml.omar.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
        }

        if ((results == null) || (results.size() == 0)) {
            throw new ObjectNotFoundException(ServerResourceBundle.getInstance().getString("message.noRegistryObjectFound",
                    new Object[]{pathInfo})); 
        }
        
        return results;
    }
    
}
