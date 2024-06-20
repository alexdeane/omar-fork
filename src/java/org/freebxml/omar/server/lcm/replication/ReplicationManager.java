/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/lcm/replication/ReplicationManager.java,v 1.9 2006/06/23 21:44:19 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm.replication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.server.common.ConnectionManager;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Manages replication of objects from remote registris to local registry
 * 
 * @author Farrukh S. Najmi
 * 
 */
public class ReplicationManager {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private ReplicationManager _objectManagerImpl; */
    private static ReplicationManager instance = null;
    private static BindingUtility bu = BindingUtility.getInstance();
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    
    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.PersistenceManagerImpl}>
     */
    org.freebxml.omar.server.persistence.PersistenceManager pm = org.freebxml.omar.server.persistence.PersistenceManagerFactory.getInstance()
                                                                                                                               .getPersistenceManager();
    /** Creates a new instance of ReplicationManager */
    protected ReplicationManager() {
    }
    
    public synchronized static ReplicationManager getInstance() {
        if (instance == null) {
            instance = new ReplicationManager();
        }

        return instance;
    }
    
    public RegistryObjectType createReplica(ServerRequestContext context, ObjectRefType oref) throws RegistryException {
        RegistryObjectType replica = null;
        
        //Only create replica if one does not exists already
        RegistryObjectType ro = pm.getRegistryObject(context, oref.getId(), "RegistryObject");
        
        if (ro == null) {
            String home = oref.getHome();
            if (home == null) {
                throw new RegistryException(new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.cannotCreateReplica")));
            }
            
            ro = getRemoteRegistryObjectUsingJAXR(oref);
            
            if (ro != null) {
                //Make sure ro has home set. TODO: Add to spec
                if (ro.getHome() == null) {
                    ro.setHome(oref.getHome());
                }
                
                //Store the remote object replica and its ObjectRef 
                //locally using user associated with requestContext
                List roList = new ArrayList();
                roList.add(ro);
                roList.add(oref);
                pm.insert(context, roList);                            
            } else {
                //What to do if remote ObjectRef unresolved
                int i=0;
            }
            
        }
        
        return replica;
    }
        
    public boolean isRemoteObjectRef(ObjectRefType oref) throws RegistryException {
        boolean isRemoteRef = false;
        String refHome = oref.getHome();
        if (refHome != null) {
            //TODO: Need extra check in case home is to local registry
            isRemoteRef = true;
        }
        
        return isRemoteRef;
    }
    

    private RegistryObjectType getRemoteRegistryObjectUsingJAXR(ObjectRefType remoteRef) throws RegistryException {
        RegistryObjectType ebRO = null;
        
        String home = remoteRef.getHome();       
        try {
            Connection connection = ConnectionManager.getInstance().getConnection(home);        
            RegistryService service = connection.getRegistryService();
            DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            RegistryObjectImpl ro = (RegistryObjectImpl)dqm.getRegistryObject(remoteRef.getId());
            if (ro != null) {
                ebRO = (RegistryObjectType)ro.toBindingObject();
            } else {
                String msg = ServerResourceBundle.getInstance().getString("message.error.RemoteObjectNotFound", new Object[]{home, remoteRef.getId()});
                throw new RegistryException(msg);
            }
        } catch (JAXRException e) {
            String msg = ServerResourceBundle.getInstance().getString("message.error.ErrorGettingRemoteObject", new Object[]{home, remoteRef.getId()});
            throw new RegistryException(msg, e);
        }
        return ebRO;
    }
    
    private RegistryObjectType getRemoteRegistryObjectUsingREST(ObjectRefType remoteObj) throws RegistryException {
        RegistryObjectType ro = null;
        String remoteURLString = remoteObj.getHome() +
        "/http?interface=QueryManager&method=getRegistryObject&param-id=" + remoteObj.getId();
                
        InputStream is = null;
        try {
            URL url = new URL(remoteURLString);
            
            HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if ((responseCode < 200) || ((responseCode > 300) && (responseCode < 500))) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.HTTPError",
                        new Object[]{new Integer(responseCode)}));
            }
            
            conn.connect();
            is = conn.getInputStream();
            ro = (RegistryObjectType)BindingUtility.getInstance().rimFac.createUnmarshaller().unmarshal(is);          
        }
        catch (MalformedURLException e) {
            throw new RegistryException(e);
        }
        catch (UnknownHostException e) {
            throw new RegistryException(e);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (IOException e) {
            throw new RegistryException(e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return ro;
    }
    
    private RepositoryItem getRemoteRepositoryItem(ObjectRefType remoteObj) {
        RepositoryItem ri = null;
        
        return ri;
    }
    
    
    
}
