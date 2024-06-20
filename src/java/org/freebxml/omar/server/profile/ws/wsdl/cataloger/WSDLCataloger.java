/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/profile/ws/wsdl/cataloger/WSDLCataloger.java,v 1.4 2005/11/21 04:27:54 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.profile.ws.wsdl.cataloger;

import java.util.HashMap;
import java.util.Map;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.cms.CatalogingServiceEngine;
import org.freebxml.omar.common.cms.CatalogingServiceInput;
import org.freebxml.omar.common.cms.CatalogingServiceOutput;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.MissingRepositoryItemException;
import org.freebxml.omar.server.cms.ContentCatalogingServiceImpl;
import org.freebxml.omar.server.cms.InvocationController;
import org.freebxml.omar.server.cms.ServiceInput;
import org.freebxml.omar.server.cms.ServiceOutput;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

import javax.activation.DataHandler;

import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;



/**
 * Extracts information from an image RepositoryItem and adds it to the
 * OriginalContent as named slots.
 *
 * @author Tony.Graham@sun.com
 */
public class WSDLCataloger extends ContentCatalogingServiceImpl {
    private static final Log log = LogFactory.getLog(WSDLCataloger.class.getName());
    static final String errorCodeContext = "WSDLCataloger.catalogContent";
    private static BindingUtility bu = BindingUtility.getInstance();

    /**
     * Catalogs WSDL files.
     *
     * @param partCatalogContentRequest CatalogContentRequest containing
     * the ExtrinisicObject representing the WSDL file.
     *
     * @throws RemoteException if an error occurs
     * @return SOAPElement containing an updated ExtrinsicObject
     */
    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        
        if (log.isTraceEnabled()) {
            log.trace("WSDLCataloger.invoke()");
        }

        RegistryObjectType registryObject = input.getRegistryObject();
        RepositoryItem repositoryItem = input.getRepositoryItem();
        DataHandler dh = null;
        if (repositoryItem != null) {
            dh = repositoryItem.getDataHandler();
        }

        if ((registryObject instanceof ExtrinsicObjectType) && (repositoryItem == null)) {
            throw new MissingRepositoryItemException(input.getRegistryObject()
                                                          .getId());
        }

        ServerRequestContext outputContext = null;

        try {
            outputContext = context; //new RequestContext(null);

            CatalogingServiceEngine engine = new WSDLCatalogerEngine();
            CatalogingServiceInput input1 = new CatalogingServiceInput((DataHandler)null, dh, registryObject);
            CatalogingServiceOutput output1 = engine.catalogContent(input1);
            

            RegistryObjectListType catalogedMetadata = bu.rimFac.createRegistryObjectListType();
            
            // FIXME: Setting catalogedMetadata as CatalogedContent results in incorrect serialization.
            catalogedMetadata.getIdentifiable().addAll(output1.getRegistryObjects());
            
            //Add cataloged repository items to outputContext
            Map idToRepositoryItemMap = output1.getRepositoryItemMap();            
            outputContext.getRepositoryItemsMap().putAll(idToRepositoryItemMap);
            
            // TODO: User should refer to "Service object for the
            // Content Management Service that generated the
            // Cataloged Content."
            outputContext.setUser(user);

            bu.getObjectRefsAndRegistryObjects(catalogedMetadata, outputContext.getTopLevelObjectsMap(), outputContext.getObjectRefsMap());
        } catch (Exception e) {
            if (outputContext != context) {
                outputContext.rollback();
            }
            throw new RegistryException(e);
        }

        ServiceOutput so = new ServiceOutput();
        so.setOutput(outputContext);

        // Setting this error list is redundant, but Content Validation Services
        // currently output a Boolean and a RegistryErrorList, so using
        // same mechanism to report errors from Content Cataloging Services.
        so.setErrorList(outputContext.getErrorList());

        if (outputContext != context) {
            outputContext.commit();
        }
        return so;        
    }
    
}
