/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JAXRClient.java,v 1.12 2006/08/24 20:41:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.RegistryObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.jaas.LoginModuleManager;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;

/**
 * Contains the JAXR client code used by the browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JAXRClient {
    /** DOCUMENT ME! */
    ConnectionImpl connection;
    
    /*
     * This class handles all JAAS authentication tasks
     */
    LoginModuleManager lmm;
    
    /** DOCUMENT ME! */
    BusinessQueryManager bqm;
    private DeclarativeQueryManagerImpl dqm;
    
    /** DOCUMENT ME! */
    org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl lcm;
    
    /** DOCUMENT ME! */
    private static final Log log = LogFactory.getLog(JAXRClient.class);

    /** Flag for this client being connected or not. */
    private boolean connected = false;

    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     * @return boolean true if connected, false otherwise.
     */
    public synchronized boolean createConnection(String url) {
        try {
            if (!RegistryBrowser.localCall) {
                (new URL(url)).openStream().read();
            }
            
            Thread.currentThread().setContextClassLoader(RegistryBrowser.getInstance().classLoader);
            
            ProviderProperties.getInstance().put("javax.xml.registry.queryManagerURL",
            url);
            
            ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();

            connection = (org.freebxml.omar.client.xml.registry.ConnectionImpl) connFactory.createConnection();
            RegistryService service = connection.getRegistryService();
            bqm = service.getBusinessQueryManager();
            dqm = (org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            lcm = (org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl)service.getBusinessLifeCycleManager();
            
            lmm = connection.getLoginModuleManager();
            lmm.setParentFrame(RegistryBrowser.getInstance());
            connected = true;
        } catch (final JAXRException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        } catch (MalformedURLException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        } catch (IOException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        return connected;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param t DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getStackTraceFromThrowable(Throwable t) {
        String trace = null;
        
        if (t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            trace = sw.toString();
        }
        
        return trace;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public Connection getConnection() throws JAXRException {
        isConnected();
        return connection;
    }
    
    /**
     * returns the business life cycle query manager. This should go
     * away once the client code has all been moved here.
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BusinessLifeCycleManager getBusinessLifeCycleManager()
    throws JAXRException {
        isConnected();
        return lcm;
    }
    
    /**
     * returns the business life cycle query manager. This should go
     * away once the client code has all been moved here.
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BusinessQueryManager getBusinessQueryManager()
        throws JAXRException {
        isConnected();
        return bqm;
    }
    
    /**
     * Find classification schemes
     *
     * @return DOCUMENT ME!
     */
    Collection getClassificationSchemes() {
        Collection schemes = null;
        
        String errMsg = "Error getting ClassificationSchemes";
        
        try {
            Map queryParams = new HashMap();
            queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, BindingUtility.getInstance().CANONICAL_QUERY_GetClassificationSchemesById);
            Query query = getDeclarativeQueryManager().createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse response = getDeclarativeQueryManager().executeQuery(query, queryParams);
            checkBulkResponse(response);
            schemes = response.getCollection();
        } catch (JAXRException e) {
            RegistryBrowser.displayError(errMsg, e);
            schemes = new ArrayList();
        }
        
        return schemes;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    CapabilityProfile getCapabilityProfile() {
        CapabilityProfile profile = null;
        
        try {
            profile = connection.getRegistryService().getCapabilityProfile();
        } catch (JAXRException e) {
            e.printStackTrace();
            RegistryBrowser.displayError(e);
        }
        
        return profile;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param objectsToSave DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BulkResponse saveObjects(Collection objectsToSave, boolean versionMetadata, boolean versionContent)
    throws JAXRException {
        BulkResponse resp = null;
        
        try {
            HashMap slotsMap = new HashMap();
            if (!versionMetadata) {
                slotsMap.put(BindingUtility.CANONICAL_SLOT_LCM_DONT_VERSION, "true");
            }
            if (!versionContent) {
                slotsMap.put(BindingUtility.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
            }
            resp = lcm.saveObjects(objectsToSave, slotsMap);
            checkBulkResponse(resp);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
            e.fillInStackTrace();
            throw e;
        }
        
        return resp;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param objectsToExport DOCUMENT ME!
     */
    public void exportObjects(Collection objectsToExport) {
        try {
            Iterator iter = objectsToExport.iterator();
            
            while (iter.hasNext()) {
                RegistryObject ro = (RegistryObject) iter.next();
                System.err.println(ro.toXML());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param resp DOCUMENT ME!
     */
    public void checkBulkResponse(BulkResponse resp) {
        try {
            if ((resp != null) &&
            (!(resp.getStatus() == JAXRResponse.STATUS_SUCCESS))) {
                Collection exceptions = resp.getExceptions();
                
                if (exceptions != null) {
                    Iterator iter = exceptions.iterator();
                    
                    while (iter.hasNext()) {
                        Exception e = (Exception) iter.next();
                        RegistryBrowser.displayError(e);
                    }
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void isConnected() throws JAXRException {
        if (!connected) {
            // try again, now synchro. will wait if in startup.
            synchronized (this) {
                if (!connected) {
                    throw new JAXRException(JavaUIResourceBundle.getInstance()
                        .getString("message.error.noConnection"));
                }
            }
        }
    }
    
    public DeclarativeQueryManagerImpl getDeclarativeQueryManager() {
        return dqm;
    }
    
}
