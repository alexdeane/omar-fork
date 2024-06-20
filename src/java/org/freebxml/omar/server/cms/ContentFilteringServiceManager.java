/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/ContentFilteringServiceManager.java,v 1.1 2007/01/12 21:34:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;


import java.util.HashSet;
import java.util.Set;
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
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;


/**
 * Content Filtering Service manager
 */
public class ContentFilteringServiceManager implements CMSTypeManager {
    private static final Log log = LogFactory.getLog(ContentFilteringServiceManager.class.getName());
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
        
        RegistryRequestType request = context.getCurrentRegistryRequest();
        
        //Filter services only apply to AdhocQueryRequests
        if (!(request instanceof AdhocQueryRequest)) {            
            return false;
        }        
        
        try {
            ContentManagementService cms = (ContentManagementService) sii.getConstructor()
                                                                         .newInstance((java.lang.Object[]) null);
            ServiceOutput so = null;
            
            //Note that ri will be null for ExternalLink ro.
            so = cms.invoke(context, new ServiceInput(ro, ri),
                sii.getService(), sii.getInvocationController(), context.getUser());            
            
            if (!(so.getOutput() instanceof ServerRequestContext)) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.FilteringServiceInstanceShouldReturnRequestContext", 
                                                    new Object[] {so.getOutput().getClass().getName()}));
            }

            ServerRequestContext outputContext = (ServerRequestContext) so.getOutput();
           
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
