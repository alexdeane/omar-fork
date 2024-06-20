/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentCatalogingServiceManager.java,v 1.23 2007/01/12 21:34:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.InvalidConfigurationException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;


/**
 * Content Cataloging Service manager
 */
public class ContentCatalogingServiceManager implements CMSTypeManager {
    private static final Log log = LogFactory.getLog(ContentCatalogingServiceManager.class.getName());
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();

    /**
     * Invokes appropriate Content Management Services for the
     * in the <code>RegistryObject</code>.
     *
     * @param ro a <code>RegistryObject</code> value
     * @param ri a <code>RepositoryItem</code> value
     */
    public boolean invokeServiceForObject(ServiceInvocationInfo sii,
        RegistryObjectType ro, RepositoryItem ri, ServerRequestContext context) 
        throws RegistryException {
        
        //Cataloging services only apply to Submit/UpdateObjectsRequests
        RegistryRequestType request = context.getCurrentRegistryRequest();
        if (!((request instanceof SubmitObjectsRequest) ||
              (request instanceof UpdateObjectsRequest))) {
            
            return false;
        }
        
        
        if (log.isTraceEnabled()) {
            log.trace(
                "ContentCatalogingServiceManager.invokeServiceForObject()");
        }

        try {
            ContentManagementService cms = (ContentManagementService) sii.getConstructor()
                                                                         .newInstance((java.lang.Object[]) null);

            System.err.println("cms: " + cms.getClass().getName());

            ServiceOutput so = null;
            
            //Note that ri will be null for ExternalLink ro.
            so = cms.invoke(context, new ServiceInput(ro, ri),
                sii.getService(), sii.getInvocationController(), context.getUser());            
            
            if (!(so.getOutput() instanceof ServerRequestContext)) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.CatalogingServiceInstanceShouldReturnRequestContext", 
                                                    new Object[] {so.getOutput().getClass().getName()}));
            }

            ServerRequestContext outputContext = (ServerRequestContext) so.getOutput();

            if (outputContext != null) {
                ArrayList list = new ArrayList(outputContext.getTopLevelObjectsMap().values());

                if (log.isDebugEnabled()) {
                    Iterator listIter = list.iterator();

                    while (listIter.hasNext()) {
                        RegistryObjectType debugRO = (RegistryObjectType) listIter.next();
                        log.debug(debugRO.getId() + "  " +
                            debugRO.getClass().getName() + "  " +
                            debugRO.getName());
                    }

                    log.debug("Objects found: " + list.size());
                }

                outputContext.checkObjects();

                pm.insert(outputContext, list);
            }
        } catch (RegistryException re) {
            log.error(re, re);
            throw re;
        } catch (Exception e) {
            log.error(e, e);
            throw new RegistryException(e);
        }

        return true;
    }
}
