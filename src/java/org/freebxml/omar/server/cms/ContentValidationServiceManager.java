/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentValidationServiceManager.java,v 1.12 2007/01/12 21:34:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.lang.reflect.InvocationTargetException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.Utility;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;


/**
 * Content Validation Service manager
 */
public class ContentValidationServiceManager implements CMSTypeManager {
    private static final Log log = LogFactory.getLog(ContentValidationServiceManager.class.getName());
    private Utility util = Utility.getInstance();

    /**
     * Invokes appropriate Content Management Services for the
     * in the <code>RegistryObject</code>.
     *
     * @param ro a <code>RegistryObject</code> value
     * @param ri a <code>RepositoryItem</code> value
     */
    public boolean invokeServiceForObject(ServiceInvocationInfo sii,
        RegistryObjectType ro, RepositoryItem ri, ServerRequestContext context) throws RegistryException {
        System.err.println(
            "ContentValidationServiceManager.invokeServiceForObject()");

        //Validation services only apply to Submit/UpdateObjectsRequests
        RegistryRequestType request = context.getCurrentRegistryRequest();
        if (!((request instanceof SubmitObjectsRequest) ||
              (request instanceof UpdateObjectsRequest))) {
            
            return false;
        }
        
        try {
            ContentManagementService cms = (ContentManagementService) sii.getConstructor()
                                                                         .newInstance((java.lang.Object[]) null);
            ServiceOutput so = null;
            //Note that ri will be null for ExternalLink ro.
            so = cms.invoke(context, new ServiceInput(ro, ri),
                    sii.getService(), sii.getInvocationController(), context.getUser());            
        } catch (InstantiationException e) {
            throw new RegistryException(e);
        } catch (IllegalAccessException e) {
            throw new RegistryException(e);
        } catch (InvocationTargetException e) {
            throw new RegistryException(e);
        }


        return true;
    }
}
