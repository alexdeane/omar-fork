/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/LifeCycleManagerLocalProxy.java,v 1.16 2006/04/07 17:22:17 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.lang.reflect.Method;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.exceptions.UserNotFoundException;

import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequest;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.common.spi.LifeCycleManager;
import org.freebxml.omar.common.spi.LifeCycleManagerFactory;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;

/**
 *
 * @author  najmi
 */
public class LifeCycleManagerLocalProxy implements LifeCycleManager {
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();
    private LifeCycleManager serverLCM = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    private QueryManager serverQM = QueryManagerFactory.getInstance().getQueryManager();
    
    private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    private SOAPMessenger msgr = null;        
    
    /** Creates a new instance of LifeCycleManagerLocalImpl */
    public LifeCycleManagerLocalProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
        this.credentialInfo = credentialInfo;
    }
    
    public RegistryResponse approveObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.approveObjects(context);
    }
    
    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponse setStatusOnObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.setStatusOnObjects(context);        
    }
    
    public RegistryResponse deprecateObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.deprecateObjects(context);
    }

    public RegistryResponse unDeprecateObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.unDeprecateObjects(context);
    }
    
    public RegistryResponse removeObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.removeObjects(context);
    }
        
    public RegistryResponse submitObjects(RequestContext context) throws RegistryException {
        UserType user =null;
        
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        // handle self registration
        user = checkRegisterUser((SubmitObjectsRequest)context.getCurrentRegistryRequest());
        // null means no registration. Proceed the normal way
        if (user == null) {
            user = getCallersUser();
        }
        context.setUser(user);
        return serverLCM.submitObjects(context);
    }
    
    public RegistryResponse updateObjects(RequestContext context) throws RegistryException {
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        context.setUser(getCallersUser());
        return serverLCM.updateObjects(context);
    }
    
    private UserType getCallersUser() throws RegistryException {
        X509Certificate cert = null;
        if (credentialInfo != null) {
            cert = credentialInfo.cert;
        }
        return serverQM.getUser(cert);
    }
    
    protected UserType checkRegisterUser(SubmitObjectsRequest req)
    throws RegistryException {
        // is it a user registration?
        // are we in local mode?
        // do we have an x509??
        if (credentialInfo != null && credentialInfo.cert != null) {
            try {
                UserType user = getCallersUser();
                // sanity check
                if (user == null) {
                    throw new UserNotFoundException("catch me");
                }
            } catch (UserNotFoundException e) {
                // UserRegistrar checks for only one user in the request
                // Call user registrar by reflection, mask the dependency of
                // common on server.. Not a best practice, but used by this
                // proxy already. What we want is:
                // UserRegistrar.getInstance().registerUser(
                //            credentialInfo.cert,req);
                try {
                    Class clazz = Class.forName(
                            "org.freebxml.omar.server.security.authentication.UserRegistrar");
                    Method getInstance = clazz.getMethod("getInstance", new Class[] {});
                    Object userRegistrar = getInstance.invoke(null, new Object[] {});
                    Method registerUser = clazz.getMethod("registerUser", new Class []
                        {X509Certificate.class, SubmitObjectsRequest.class});
                    Object user = registerUser.invoke(userRegistrar, new Object[]
                        {credentialInfo.cert, req});
                    return (UserType)user;
                } catch (Exception re) {
                    // Log error, return null (proceed with normal way)
                    log.error(CommonResourceBundle.getInstance().getString("message.ExceptionWhenCallingUserRegistrarRegisterUser"), re);
                    //TODO throw exception with "internal server error"?!
                }
            }
        }
        return null;
    }
    
    public RegistryResponse relocateObjects(RequestContext context) throws RegistryException {
        return serverLCM.relocateObjects(context);
    }
    
    /** Sends an impl specific protocol extension request. */
    public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException {
        
        RegistryResponseHolder respHolder = null;
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        
        context.setUser(getCallersUser());
        respHolder = serverLCM.extensionRequest(context);
        
        bu.convertRepositoryItemMapForClient(respHolder.getAttachmentsMap());
        
        return respHolder;
    }
}
