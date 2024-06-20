/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/Request.java,v 1.37 2006/04/07 17:22:17 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.spi.LifeCycleManager;
import org.freebxml.omar.common.spi.LifeCycleManagerFactory;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RegistryResponseHolder;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.UserNotFoundException;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.interfaces.common.SessionManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.security.authentication.UserRegistrar;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequest;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * A Request encapsulates all aspects of an incoming client request to an ebXML registry.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class Request {
    
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private BindingUtility bu = BindingUtility.getInstance();
    private LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    
    private static AuthenticationServiceImpl authc = AuthenticationServiceImpl.getInstance();
    private static Log log = LogFactory.getLog(Request.class);
    
    private CredentialInfo headerCredentialInfo;
    private HttpServletRequest request;
    private ServerRequestContext context = null;
    
    public Request(HttpServletRequest req,
		   CredentialInfo headerCredentialInfo,
		   Object message,
		   HashMap idToRepositoryItemMap)
	throws RegistryException {

	this.request = req;
        this.headerCredentialInfo = headerCredentialInfo;
        
        String contextId = "Request." + message.getClass().getName();
        context = new ServerRequestContext(contextId, (RegistryRequestType)message);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        UserType user = getRequestUser();
        context.setUser(user);        
    }
    
    /**
     * Processes the Request by dispatching it to a service in the registry.
     */
    public Response process() throws RegistryException {
        
        Response response = null;
        RegistryResponseType rr = null;
        
        HashMap responseRepositoryItemMap = new HashMap();

        RegistryRequestType message = context.getCurrentRegistryRequest();
        if (message instanceof AdhocQueryRequestType) {
            AdhocQueryRequestType ahqreq = (AdhocQueryRequestType)message;
            rr = qm.submitAdhocQuery(context);
            
            org.oasis.ebxml.registry.bindings.query.ResponseOptionType responseOption =
                ahqreq.getResponseOption();
            ReturnType returnType = responseOption.getReturnType();
            if (returnType == returnType.LEAF_CLASS_WITH_REPOSITORY_ITEM) {                               
                responseRepositoryItemMap.putAll(context.getRepositoryItemsMap());
            }
        } 
        else if (message instanceof ApproveObjectsRequestType) {
            rr = lcm.approveObjects(context);
        } 
        else if (message instanceof SetStatusOnObjectsRequestType) {
            rr = lcm.setStatusOnObjects(context);
        } 
        else if (message instanceof DeprecateObjectsRequestType) {
            rr = lcm.deprecateObjects(context);
        } 
        else if (message instanceof UndeprecateObjectsRequestType) {
            rr = lcm.unDeprecateObjects(context);
        } 
        else if (message instanceof RemoveObjectsRequestType) {
            rr = lcm.removeObjects(context);
        } 
        else if (message instanceof SubmitObjectsRequestType) {
            rr = lcm.submitObjects(context);
        } 
        else if (message instanceof UpdateObjectsRequestType) {
            rr = lcm.updateObjects(context);
        }
        else if (message instanceof RelocateObjectsRequestType) {
            rr = lcm.relocateObjects(context);
        }
        else if (message instanceof RegistryRequestType) {
            RegistryResponseHolder respHolder = lcm.extensionRequest(context);
            
            //Due to bad design few lines down we are idToRepositoryItemMap for response attachment map
            //Following line is a workaround for that
            responseRepositoryItemMap = respHolder.getAttachmentsMap();
            rr = respHolder.getRegistryResponse();
        }
        else {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unknownRequest") +
                message.getClass().getName());
        }
        
        response = new Response(rr, responseRepositoryItemMap);
        
        return response;
    }
    
    /*
     * Gets the current user associated with request.
     *
     * Get the user from request header if it is signed.
     * Otherwise get it from HttpSession.
     * When all else fails use RegistryGuest as user.
     *
     */
    private UserType getRequestUser() throws RegistryException {
        
        RegistryRequestType message = context.getCurrentRegistryRequest();
        UserType user = null;
        
        if (SessionManager.getInstance().isSessionEstablished(request)) {
            user = SessionManager.getInstance().getUserFromSession(request);
        }
        if (user == null) {
            // User associated with Header signature overrides user associated 
            // with any HttpSession
            // But does not change the user associated with HttpSession.
            if (headerCredentialInfo != null && headerCredentialInfo.cert != null) {
                try {
                    user = authc.getUserFromCertificate(headerCredentialInfo.cert);
                    user = getEffectiveUser(user);

                } 
                catch (UserNotFoundException e) {
                    if (message instanceof SubmitObjectsRequest) {
                        user = UserRegistrar.getInstance()
                                            .registerUser(headerCredentialInfo.cert,
                                                         (SubmitObjectsRequest)message);
                    } 
                    else {
                        user = authc.registryGuest;
                    }
                }
            } else {
                user = authc.registryGuest;
            }     
            SessionManager.getInstance().establishSession(request,
                                                          user,
                                                          (RegistryRequestType)message);
        }
        
        //Map registryGuest user to registryOperator user if noUserRegistrationRequired is true
        if (user == authc.registryGuest) {
            boolean noUserRegRequired = Boolean.valueOf(CommonProperties.getInstance().getProperty("omar.common.noUserRegistrationRequired", "false")).booleanValue();
            if (noUserRegRequired) {
                user = authc.registryOperator;
            }
        }
                
        return user;
    }
    
    /*
     * If requestor user has role of Intermediary then gets the actual User
     * on whose behalf the requestor sent the request. 
     *
     * @returns the requestor if requestor does not have role of Intermediary or the user
     * identified by special request slot if requestor does have role of Intermediary
     *
     */
    private UserType getEffectiveUser(UserType requestor) throws RegistryException {
        RegistryRequestType message = context.getCurrentRegistryRequest();
        UserType user = requestor;
        
        try {
            HashMap requestSlots = bu.getSlotsFromRequest((RegistryRequestType)message);
            String userId = (String)requestSlots.get(BindingUtility.CANONICAL_URI_EFFECTIVE_REQUESTOR);
            
            if (userId != null) {
                boolean isIntermediary = (authc.hasIntermediaryRole(requestor) || authc.hasRegistryAdministratorRole(requestor));

                if (isIntermediary) {
		    try {
			UserType u = (UserType)qm.getRegistryObject(context,
								    userId,
								    "User");
			if (u != null) {
			    user = u;
			}
		    } catch (ObjectNotFoundException e) {
			// Missing effective user, fall back to requestor
		    }
                }
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return user;
    }
}
