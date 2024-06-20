/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/ConnectionFactoryImpl.java,v 1.6 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;


/**
 * A provider specific extension of the JAXR API class named  ConnectionFactory.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ConnectionFactoryImpl extends ConnectionFactory {
    private static final Log log = LogFactory.getLog(ConnectionFactoryImpl.class);
    private ProviderProperties providerProps = ProviderProperties.getInstance();

    public ConnectionFactoryImpl() throws JAXRException {
        initLocale(this.providerProps.getProperties());
    }

    /**
     * Sets the Properties used during createConnection
     * and createFederatedConnection calls.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param properties configuration properties that are either
     * specified by JAXR or provider specific.
     */
    public void setProperties(Properties properties) throws JAXRException {
        this.providerProps.mergeProperties(properties);
    }

    /**
     * Gets the Properties used during createConnection
     * and createFederatedConnection calls.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Properties getProperties() throws JAXRException {
        return this.providerProps.getProperties();
    }

    /**
     * Create a named connection. Such a connection can be used to
     * communicate with a JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @link dependency
     * @label creates
     * @associates <{Connection}>
     */
    public Connection createConnection() throws JAXRException {
        Connection connection = new ConnectionImpl(this);

        return connection;
    }

    /**
     * Create a FederatedConnection.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param connections Is a Collection of Connection objects. Note that
     * Connection objects may also be FederatedConnection objects.
     *
     * @link dependency
     * @label creates
     * @associates <{FederatedConnection}>
     */
    public FederatedConnection createFederatedConnection(Collection connections)
        throws JAXRException {
        // Write your code here
        return null;
    }

    /**
     * Sets initial Locale.
     *
     * The Locale is defined by 3 properties:
     * <ul>
     * <li>jaxr-ebxml.initial-locale.language</li>
     * <li>jaxr-ebxml.initial-locale.country</li>
     * <li>jaxr-ebxml.initial-locale.variant</li>
     * </ul>
     * Locale.setDefault will be called with a new Locale created using language,
     * country and variant (if defined, up to the first missing of the 3
     * properties). If language property is not defined, Locale WILL NOT be
     * changed. See http://java.sun.com/j2se/1.4.1/docs/api/java/util/Locale.html
     * for possible values for language, country and variant.
     *
     * @param props The source for the properties.
     */
    private void initLocale(Properties props) {
        String language = props.getProperty(
                "jaxr-ebxml.initial-locale.language");
        String country = props.getProperty("jaxr-ebxml.initial-locale.country");
        String variant = props.getProperty("jaxr-ebxml.initial-locale.variant");

        if ((language == null) || (language.length() == 0)) {
            return;
        } else if ((country == null) || (country.length() == 0)) {
            Locale.setDefault(new Locale(language));
        } else if ((variant == null) || (variant.length() == 0)) {
            Locale.setDefault(new Locale(language, country));
        } else {
            Locale.setDefault(new Locale(language, country, variant));
        }
    }
}
