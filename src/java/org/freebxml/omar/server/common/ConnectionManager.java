/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/common/ConnectionManager.java,v 1.4 2006/06/23 21:44:17 farrukh_najmi Exp $
 *
 * ====================================================================
 */

package org.freebxml.omar.server.common;

import java.util.Properties;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;

import org.freebxml.omar.client.xml.registry.util.JAXRUtility;

/**
 * Manages outbound JAXR Connections to other registries.
 * For now does very little other than create a new Connection each time.
 * In future it may be optimized to cache Connections.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConnectionManager {
    
    //maps String home to JAXR Connection
    //private HashMap connectionMap = new HashMap();
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private ConnectionManager _connectionManagerImpl; */
    private static ConnectionManager instance = null;
    
    /** Creates a new instance of ConnectionManager */
    protected ConnectionManager() {
    }
    
    public synchronized static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }

        return instance;
    }        
    
    public Connection getConnection(String home) throws JAXRException {
        return createConnection(home);
    }
    
    /** Setup JAXR Connection for target registry */
    private Connection createConnection(String home) throws JAXRException {
        
        //TODO: Need to use SAML SSO here
        String queryManagerURL = home + "/soap";

        ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();
        Properties props = new Properties();
        props.put("javax.xml.registry.queryManagerURL", queryManagerURL);
        props.put("javax.xml.registry.lifeCycleManagerURL", queryManagerURL);
        connFactory.setProperties(props);

        Connection connection = connFactory.createConnection();
        ((ConnectionImpl)connection).setLocalCallMode(false);
        return connection;
    }
    
}
