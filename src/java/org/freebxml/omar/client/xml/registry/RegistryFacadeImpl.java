/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/RegistryFacadeImpl.java,v 1.10 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectRef;
import org.freebxml.omar.client.xml.registry.util.CertificateUtil;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.util.SecurityUtil;
import org.freebxml.omar.client.xml.registry.util.UserRegistrationInfo;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.common.profile.ws.wsdl.CanonicalConstants;

/**
 * Implementation of provider specific interface named RegistryFacade.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryFacadeImpl implements RegistryFacade {
    private static final Log log = LogFactory.getLog(RegistryFacadeImpl.class);
    protected SecurityUtil securityUtil = SecurityUtil.getInstance();
    protected BindingUtility bu = BindingUtility.getInstance();
    protected HashMap dontVersionSlotsMap = new HashMap();

    
    private RegistryService service;
    private BusinessQueryManagerImpl bqm;
    private BusinessLifeCycleManagerImpl lcm;
    private DeclarativeQueryManagerImpl dqm;
    private Connection connection;
    
    /** Creates a new instance of RegistryFacadeImpl */
    public RegistryFacadeImpl() {
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");        
    }

    public void setEndpoints(String qmEndpoint, String lcmEndpoint) throws JAXRException {
        createConnection(qmEndpoint, lcmEndpoint);
    }

    /*
     * Register a new user with registry server.
     * Creates a new registry issued digital certificate and stores
     * it into the client keystore using specified alias and password.
     * Also stores the public key in server keystore and stores the User instance
     * in the registry server.
     *
     * @param user the metadata desribing the new users's name and other information
     * @param alias the alias to be used for the user's new registry issued certificate
     * @param keypass the key password for user's new private key in the client keystore
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     *
     */
    public void registerUser(User user, String alias, String keypass) throws JAXRException {
        ConnectionImpl c = ((ConnectionImpl)getConnection());
        if (c == null) {
            throw new JAXRException("setEndpoint MUST be called before registerUser is called.");
        }
        UserRegistrationInfo userRegInfo = new UserRegistrationInfo(user);
        
        try {
            //Just in case previous logon session existed.
            logoff();
                        
            userRegInfo.setAlias(alias);
            userRegInfo.setKeyPassword(keypass.toCharArray());
            char[] storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass").toCharArray();
            if (!CertificateUtil.certificateExists(alias, storepass)) {
                CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
            }

            logon(alias, keypass);
            
            // Now save the User
            ArrayList objects = new ArrayList();
            objects.add(userRegInfo.getUser());
            getLifeCycleManager().saveObjects(objects, dontVersionSlotsMap);
        } 
        catch (Exception e) {
            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (alias != null) {
                    CertificateUtil.removeCertificate(alias,
                        userRegInfo.getStorePassword());
                }
            } catch (Exception removeCertException) {
                log.warn(removeCertException);
            }

            if (e instanceof JAXRException) {
                throw (JAXRException)e;
            } else {
                throw new JAXRException(e);
            }
        }
        finally {
            File tmpFile = new File(userRegInfo.getP12File());
            if (tmpFile.exists()) {
                tmpFile.delete();
            }             
        }
    }

    /**
     * Logs off the current user from this RegistryFacade.
     *  
     */
    public void logoff() throws JAXRException {
        if (getConnection() == null) {
            throw new JAXRException("setEndpoint MUST be called before logoff is called.");
        }
        (( org.freebxml.omar.client.xml.registry.ConnectionImpl)getConnection()).logoff();
    }
    
    /**
     * Logs on with a new user context using the credentials specified. The
     * credentials are used to look up the user's public/private keys in teh client keystore
     * and use then to sign client requests to the server.
     *
     * @param alias the alias for user in the client keystore
     * @param keypass the key password for user's private key in the client keystore
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     *
     */
    public void logon(String alias, String keypass) throws JAXRException {
        if (getConnection() == null) {
            throw new JAXRException("setEndpoint MUST be called before logon is called.");
        }
        // Set credentials
        HashSet creds = new HashSet();
        creds.add(securityUtil.aliasToX500PrivateCredential(alias, keypass));
        getConnection().setCredentials(creds);
    }

    /*
     * Publishes a set of related files as a single zip file to the registry.
     * At present this method is intended to support publishing of a set
     * of related WSDL and XML Schema documents as a single zip file to the registry.
     *
     *
     * @param baseDirectory the base directory for files being published.
     * @param relativeFilePaths the paths to files being published where each path is relative to baseDirectory.
     * @param id the identifier for the zip file being published. Specify null if no id is specified. If null, the registry will assign a unique id.
     * @param name the name for the zip file being published. Specify null if no name is specified.
     * @param description the description for the zip file being published. Specify null if no description is specified.
     * 
     * @return the ExtrinsicObject published as metadata.
     * @throws JAXRException an exception thrown by JAXR Provider.
     */
    public ExtrinsicObject publishFilesAsZip(
            String baseDirectory, 
            String[] relativeFilePaths,
            Map metadata
            ) throws JAXRException {
        
        ExtrinsicObjectImpl eo = null;
        if (getLifeCycleManager() == null) {
            throw new JAXRException("setEndpoint MUST be called before setCredentials is called.");
        }
        
        try {
            //Create the zip file
            File zipFile = File.createTempFile("omar-RegistryFacadeImpl", ".zip");
            zipFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = Utility.createZipOutputStream(baseDirectory, relativeFilePaths, fos);
            zos.close();
                        
            javax.activation.DataSource 
                     ds = new javax.activation.FileDataSource(zipFile);        
            javax.activation.DataHandler 
                      dh = new javax.activation.DataHandler(ds);

            if (dh == null) {
                throw new JAXRException("Error processing zip file" + zipFile.getAbsolutePath());
            }
            
            eo  = (ExtrinsicObjectImpl)getLifeCycleManager().createExtrinsicObject(dh);
            
            String id = (String)metadata.remove(bu.CANONICAL_SLOT_IDENTIFIABLE_ID);
            String name = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_NAME);
            String description = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_DESCRIPTION);
            String objectType = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_OBJECTTYPE);

            //If id is specified then use it.
            if (id != null) {
                eo.setKey(lcm.createKey(id));
                eo.setLid(id);
            }

            String eoId = eo.getKey().getId();

            //If name is specified then use it.
            if (name != null) {
                eo.setName( lcm.createInternationalString(name));
            }

            //If description is specified then use it.
            if (description != null) {
                eo.setDescription( lcm.createInternationalString(description));
            }

            if (objectType == null) {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.missingMetadata",new Object[] {bu.CANONICAL_SLOT_REGISTRY_OBJECT_OBJECTTYPE}));
            }
                                                
            eo.setMimeType("application/zip");
            eo.setObjectTypeRef(new RegistryObjectRef(getLifeCycleManager(), objectType));
            
            
            ArrayList objects = new ArrayList();
            objects.add(eo);

            //??Turn of versioning on save as it creates problems
            
            BulkResponse br = getLifeCycleManager().saveObjects(objects, dontVersionSlotsMap);
            if (br.getExceptions() != null) {
                throw new JAXRException("Error publishing to registry", (Exception)(br.getExceptions().toArray()[0]));
            }
            
            if (br.getStatus() != BulkResponse.STATUS_SUCCESS) {
                throw new JAXRException("Error publishing to registry. See server logs for detils.");
            }
        } catch (IOException e) {
            throw new JAXRException(e);
        }
        
        return eo;
    }
    
    /**
     * Publishes a repositoryItem and the metadata that describes it.    
     *
     * @param repositoryItem the URL to the repositoryItem being published.
     * @param metadata describing the repositoryItem
     *
     * @return the ExtrinsicObject published as metadata.
     * @see org.freebxml.omar.common.CanonicalConstants for constants that may be used to identify keys in metadaat HashMap 
     */
    public ExtrinsicObject publish(URL repositoryItem, Map metadata) throws JAXRException {
        ExtrinsicObjectImpl eo = null;
        
        if (lcm == null) {
            throw new JAXRException("setEndpoint MUST be called before setCredentials is called.");
        }
        
        javax.activation.DataSource 
                 ds = new javax.activation.URLDataSource(repositoryItem);        
        javax.activation.DataHandler 
                  dh = new javax.activation.DataHandler(ds);

        eo  = (ExtrinsicObjectImpl)lcm.createExtrinsicObject(dh);

        String id = (String)metadata.remove(bu.CANONICAL_SLOT_IDENTIFIABLE_ID);
        String name = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_NAME);
        String description = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_DESCRIPTION);
        String objectType = (String)metadata.remove(bu.CANONICAL_SLOT_REGISTRY_OBJECT_OBJECTTYPE);
        String mimeType = (String)metadata.remove(bu.CANONICAL_SLOT_EXTRINSIC_OBJECT_MIMETYPE);

        //If id is specified then use it.
        if (id != null) {
            eo.setKey(lcm.createKey(id));
            eo.setLid(id);
        }

        String eoId = eo.getKey().getId();

        //If name is specified then use it.
        if (name != null) {
            eo.setName( lcm.createInternationalString(name));
        }

        //If description is specified then use it.
        if (description != null) {
            eo.setDescription( lcm.createInternationalString(description));
        }
        
        if (objectType == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.missingMetadata",new Object[] {bu.CANONICAL_SLOT_REGISTRY_OBJECT_OBJECTTYPE}));
        }

        if (mimeType == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.missingMetadata",new Object[] {bu.CANONICAL_SLOT_EXTRINSIC_OBJECT_MIMETYPE}));
        }

        eo.setMimeType(mimeType);
        eo.setObjectTypeRef(new RegistryObjectRef(lcm, objectType));

        //Set any remaining metadata properties as Slots
        JAXRUtility.addSlotsToRegistryObject(eo, metadata);

        ArrayList objects = new ArrayList();
        objects.add(eo);

        BulkResponse br = lcm.saveObjects(objects, dontVersionSlotsMap);
        if (br.getExceptions() != null) {
            throw new JAXRException("Error publishing to registry", (Exception)(br.getExceptions().toArray()[0]));
        }

        if (br.getStatus() != BulkResponse.STATUS_SUCCESS) {
            throw new JAXRException("Error publishing to registry. See server logs for detils.");
        }
        
        return eo;
    }    

    /**
     * Get the specified artifacts from Service Registry.
     * 
     * @see [WSPROF] ebXML Registry Profile for Web Services: http://www.oasis-open.org/committees/document.php?document_id=14756
     * @param queryId Identifies the discovery query that is preconfigured registry
     * @param queryParams key is a parameter name String (e.g. $service.name), value is a parameter value String as described by [WSPROF]
     * @return Set of javax.xml.registry.infomodel.RegistryObject.
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     */
    public Collection executeQuery(String queryId, Map queryParams) throws JAXRException {
        if (getDeclarativeQueryManager() == null) {
            throw new JAXRException("setEndpoint MUST be called before setCredentials is called.");
        }
        
        Collection registryObjects = null;

        queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, queryId);
        Query query = ((DeclarativeQueryManagerImpl)getDeclarativeQueryManager())
            .createQuery(Query.QUERY_TYPE_SQL);
        BulkResponse bResponse = getDeclarativeQueryManager().executeQuery(query, queryParams);
        registryObjects = bResponse.getCollection();        
        
        return registryObjects;
    }

    private void createConnection(String qmEndpoint, String lcmEndpoint) throws JAXRException {
        ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();

        Properties  props = new Properties();
        
        if (qmEndpoint == null) {
            throw new JAXRException("QueryManager endpoint MUST not be null");
        }
        props.put("javax.xml.registry.queryManagerURL", qmEndpoint);
        if (lcmEndpoint != null) {
            props.put("javax.xml.registry.lifeCycleManagerURL", qmEndpoint);
        }
        connFactory.setProperties(props);
        connection = connFactory.createConnection();

        service = getConnection().getRegistryService();
        bqm = (BusinessQueryManagerImpl) getService().getBusinessQueryManager();
        lcm = (BusinessLifeCycleManagerImpl) getService().getBusinessLifeCycleManager();
        dqm = (DeclarativeQueryManagerImpl) getService().getDeclarativeQueryManager();        
    }        

    public RegistryService getService() {
        return service;
    }

    public BusinessQueryManagerImpl getBusinessQueryManager() {
        return bqm;
    }

    public BusinessLifeCycleManagerImpl getLifeCycleManager() {
        return lcm;
    }

    public DeclarativeQueryManagerImpl getDeclarativeQueryManager() {
        return dqm;
    }

    public Connection getConnection() {
        return connection;
    }

}
