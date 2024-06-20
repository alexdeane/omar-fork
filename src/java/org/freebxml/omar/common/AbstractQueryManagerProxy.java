/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/AbstractQueryManagerProxy.java,v 1.1 2006/08/25 15:35:41 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.RequestContext;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import java.security.cert.X509Certificate;

import javax.xml.registry.RegistryException;


/**
 * Code common to all QueryManagerProxy implementations
 */
public abstract class AbstractQueryManagerProxy implements QueryManager {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected String registryURL;
    protected CredentialInfo credentialInfo;
    protected SOAPMessenger msgr;

    /**
     * Implements methods and contains data common to all QueryManagerProxy implementations.
     * @param registryURL URL of registry to which to connect.
     * @param credentialInfo Credentials to use when connecting to @registryUrl
     */
    public AbstractQueryManagerProxy(String registryURL,
        CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }

    /**
     * Submit the ad hoc query contained in @context.
     * @param context Context containing the ad hoc query.
     * @return an AdhocQueryResponse.
     * @throws javax.xml.registry.RegistryException if an error occurs.
     */
    public abstract AdhocQueryResponseType submitAdhocQuery(
        RequestContext context) throws RegistryException;

    /**
     * Gets the registry object with the specified id, if the object exists.
     * @param context Context through which to make the request.
     * @param id Id of the RegistryObject to get.
     * @return the RegistryObject with Id matching @id.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public RegistryObjectType getRegistryObject(RequestContext context,
        String id) throws RegistryException {
        return getRegistryObject(context, id, "RegistryObject");
    }

    /**
     * Gets the RegistryObject with the specified Id and specified object type.
     * @param context The context within which to make the request to the server.
     * @param id Id of the RegistryObject to get.
     * @param typeName Type of the RegistryObject to get.
     * @return the RegistryObject with the specified Id and object type.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public abstract RegistryObjectType getRegistryObject(
        RequestContext context, String id, String typeName)
        throws RegistryException;

    /**
     * Gets the RepositoryItem, if any, corresponding to the RegistryObject with the specified Id.
     * @param context Context within which to make the request to the server.
     * @param id Id of the RegistryObject for which the corresponding RepositoryItem is to be returned.
     * @return the RepositoryItem, if it exists.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public abstract RepositoryItem getRepositoryItem(RequestContext context,
        String id) throws RegistryException;

    /**
     * Gets the User associated with the specified certificate.
     * @param cert Certificate from which to get the User.
     * @return the User for the certificate.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public UserType getUser(X509Certificate cert) throws RegistryException {
        throw new RegistryException(resourceBundle.getString(
                "message.unimplemented"));
    }
}
