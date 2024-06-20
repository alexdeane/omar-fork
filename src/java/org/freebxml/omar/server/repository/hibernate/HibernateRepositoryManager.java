/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/HibernateRepositoryManager.java,v 1.30 2006/12/20 02:19:45 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.RepositoryItemNotFoundException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.repository.AbstractRepositoryManager;
import org.freebxml.omar.server.repository.RepositoryItemKey;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class HibernateRepositoryManager extends AbstractRepositoryManager {    
    /** Log */
    private Log log = LogFactory.getLog(HibernateRepositoryManager.class.getName());
    
    /** Hibernatet util, initialized by constructor. */
    private RepositoryHibernateUtil hu;

    /** Flag for BLOB supported, required for using Oracle. Postgres requires byte
     *  arrays (binary type). HSQLDB works with both. */
    private boolean isBlobSupported;

    /** Flag to signal need for Oracle hack. */
    private boolean needsOracleHack;
    
    private boolean reclaimUsedDBConnections = false;
    
    /** Singleton instance */
    protected static RepositoryManager instance;        
    
    // ---------------------------------------------------------------------- //
    // Constructor
    // ---------------------------------------------------------------------- //
    
    /** Creates a new instance of HibernateRepositoryManager */
    protected HibernateRepositoryManager() {
        hu = RepositoryHibernateUtil.getInstance();
        // initialize isBlobSupported property, default to true.
        isBlobSupported = "BLOB".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("omar.persistence.rdb.largeBinaryType", "BLOB"));
        // checks if Oracle hack will be needed.. does not consider version!
        needsOracleHack = "oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(
            RegistryProperties.getInstance().getProperty(
            "omar.persistence.rdb.databaseDriver", ""));
        reclaimUsedDBConnections = "true".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("omar.repository.hibernate.reclaimUsedDBConnections", "false"));
    }
    
    // ---------------------------------------------------------------------- //
    // Singleton pattern
    // ---------------------------------------------------------------------- //
    
    /**
     * Singleton instance accessor.
     */
    public synchronized static RepositoryManager getInstance() {
        if (instance == null) {
            instance = new HibernateRepositoryManager();
        }
        
        return instance;
    }
    
    // ---------------------------------------------------------------------- //
    // RepositoryManager interface implementation
    // ---------------------------------------------------------------------- //
    
    /**
     * Insert the repository item.
     * @param item The repository item.
     */
    public void insert(ServerRequestContext context, RepositoryItem item) throws RegistryException {
        Transaction tx = null;
        
        ExtrinsicObjectType eo = (ExtrinsicObjectType)context.getRegistryObject(item.getId(), "ExtrinsicObject");
        String lid = eo.getLid();
        String versionName = null;
                    
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
                                    
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo == null) {
                contentVersionInfo = bu.rimFac.createVersionInfoType();
                eo.setContentVersionInfo(contentVersionInfo);
            }
            versionName = eo.getContentVersionInfo().getVersionName();
        
            // if item already exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
            
            List results = s.find(findByID, params, types);
            if (!results.isEmpty()) {
                String errmsg = ServerResourceBundle.getInstance().getString("message.RepositoryItemWithIdAndVersionAlreadyExist",
				                                                    new Object[]{lid, versionName});
                log.error(errmsg);
                throw new RegistryException(errmsg);
            }
            
            // Writing out the RepositoryItem itself
            byte contentBytes[] = readBytes(item.getDataHandler().getInputStream());
            
            RepositoryItemBean bean = new RepositoryItemBean();
            RepositoryItemKey key = new RepositoryItemKey(lid, versionName);
            bean.setKey(key);

            if (needsOracleHack) {
                doOracleHackForInsert(s, bean, contentBytes);
                // do not call save after this
            } else {
                if (isBlobSupported) {
                    bean.setBlobContent(Hibernate.createBlob(contentBytes));
                } else {
                    bean.setBinaryContent(contentBytes);
                }
            }
            
            if (log.isDebugEnabled()) {
                String message = "Inserting repository item:"
                + "lid='" + key.getLid() + "', "
                + "versionName='" + key.getVersionName() + "', ";
                if (isBlobSupported) {
                    message += "content size=" + bean.getBlobContent().length();
                } else {
                    message += "content size=" + bean.getBinaryContent().length;
                }
                log.debug(message);
            }

            if (!needsOracleHack) {
                s.save(bean);
            }
            
            tx.commit();
            s.refresh(bean);
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToInsertRepositoryItem",
			                                                new Object[]{item.getId()});
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg}));
        } finally {
            tryClose();
        }
    }
    
    /**
    * Returns the RepositoryItem associated with the ExtrinsicObject specified by id.
    *
    * @param id Unique id for ExtrinsicObject whose repository item is desired.
    * @return RepositoryItem instance
    * @exception RegistryException
    */
    public RepositoryItem getRepositoryItem(String id)
        throws RegistryException {
        RepositoryItem repositoryItem = null;

	/*
	** Following code must duplicate that in getRepositoryItemKey()
	** because we need the eo variable later.  Keep the two in sync
	** manually.
	*/
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("HibernateRepositoryManager:getRepositoryItem", null);
            
            //Access control is check in qm.getRepositoryItem using actual request context
            //This internal request context has total access.
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            RegistryObjectType ro = qm.getRegistryObject(context, id,  "ExtrinsicObject");
            if (!(ro instanceof ExtrinsicObjectType)) {
                throw new ObjectNotFoundException(id);
            }

            ExtrinsicObjectType eo = (ExtrinsicObjectType)ro;
            if (eo == null) {
                throw new ObjectNotFoundException(id);
            }
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo == null) {
                // no Repository Item to find for this EO
                throw new RepositoryItemNotFoundException(id,
                                             eo.getVersionInfo().getVersionName());
            }

            RepositoryItemKey key = new RepositoryItemKey(eo.getLid(), contentVersionInfo.getVersionName());

            Transaction tx = null;
            String lid = key.getLid();
            String versionName = key.getVersionName();

            try {
                SessionContext sc = hu.getSessionContext();
                Session s = sc.getSession();
                
                //Need to call clear otherwise we get a cached ReposiytoryItemBean where the txn has committed 
                //and Blob cannot be read any more. This will be better fixed when we pass ServerRequestContext
                //to each rm interface method and leverage leave txn management to ServerRequestContext.
                //See patch submitted for Sun Bug 6444810 on 6/28/2006
                s.clear();
                tx = s.beginTransaction();

                // if item does not exist, error
                String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
                Object[] params = {
                    lid, versionName
                };

                Type[] types = {
                    Hibernate.STRING, Hibernate.STRING
                };

                List results = s.find(findByID, params, types);
                if (results.isEmpty()) {
                    String errmsg = ServerResourceBundle.getInstance().getString("message.RepositoryItemWithIdAndVersionDoesNotExist",
                                                                                       new Object[]{lid, versionName});
                    log.error(errmsg);
                    throw new RepositoryItemNotFoundException(lid, versionName);
                }

                RepositoryItemBean bean = (RepositoryItemBean)results.get(0);

                if (log.isDebugEnabled()) {
                    String message = "Getting repository item:"
                    + "lid='" + lid + "', "
                    + "versionName='" + versionName + "', ";
                    if (isBlobSupported) {
                        message += "content size=" + bean.getBlobContent().length();
                    } else {
                        message += "content size=" + bean.getBinaryContent().length;
                    }
                    log.debug(message);
                }

                String contentType = eo.getMimeType();

                DataHandler contentDataHandler;
                if (isBlobSupported) {
                    contentDataHandler = new DataHandler(new ByteArrayDataSource(
                        readBytes(bean.getBlobContent().getBinaryStream()), contentType));
                } else {
                    contentDataHandler = new DataHandler(new ByteArrayDataSource(
                        bean.getBinaryContent(), contentType));
                }

                repositoryItem = new RepositoryItemImpl(id, contentDataHandler);

                tx.commit();
            } catch (RegistryException e) {
                tryRollback(tx);
                throw e;
            } catch (Exception e) {
                String msg = ServerResourceBundle.getInstance().getString("message.FailedToGetRepositoryItem",
                                                                             new Object[]{lid, versionName});
                log.error(e, e);
                tryRollback(tx);
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
            } finally {
                tryClose();
            }
        } finally {
            context.rollback();
        }
        
        return repositoryItem;
    }
    
    /**
     * Delete the repository item.
     * @param key Unique key for repository item
     * @throws RegistryException if the item does not exist
     */
    public void delete(RepositoryItemKey key) throws RegistryException {
        Transaction tx = null;
        String lid = key.getLid();
        String versionName = key.getVersionName();
        
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
            
            if (log.isDebugEnabled()) {
                String message = "Deleting repository item: lid='" + lid + "' versionName='" + versionName + "'";
                log.debug(message);
            }
            
                        
            // if item does not exist, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            int deleted = s.delete(findByID, params, types);
            if (deleted == 0) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RepositoryItemDoesNotExist", new Object[]{lid,versionName}));
            }
            tx.commit();
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToDeleteRepositoryItem",
			                                                 new Object[]{lid, versionName});
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
     * Since ids could contain duplicates returns a Set.
     */
    private Set getKeysFromIds(List ids) throws RegistryException {
        HashSet keys = new HashSet();
        
        Iterator iter = ids.iterator();
        while (iter.hasNext()) {
            String id = (String)iter.next();
            RepositoryItemKey key = getRepositoryItemKey(id);
            keys.add(key);
        }
        
        return keys;
    }
        
    
    /**
     * Delete multiple repository items.
     * @param ids List of ids of ExtrinsicObjects whose repositoryItems are desired to be deleted.
     * @throws RegistryException if any of the item do not exist
     */
    public void delete(List ids) throws RegistryException {
        Transaction tx = null;
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
            
            Set keys = getKeysFromIds(ids);
            Iterator iter = keys.iterator();
            
            if (log.isDebugEnabled()) {
                StringBuffer message = new StringBuffer("Deleting repository items: ");
                for (Iterator it = keys.iterator(); it.hasNext(); ) {
                    message.append(((RepositoryItemKey)it.next()).toString());
                    if (it.hasNext()) {
                        message.append(", \n");
                    } else {
                        message.append(".\n");
                    }
                }
                log.debug(message);
            }
            
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                RepositoryItemKey key = (RepositoryItemKey)it.next();
                
                String lid = key.getLid();
                String versionName = key.getVersionName();
                
                Object[] params = {
                    lid, versionName
                };

                Type[] types = {
                    Hibernate.STRING, Hibernate.STRING
                };

                int deleted = s.delete(findByID, params, types);
                
                if (deleted == 0) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RepositoryItemDoesNotExist", new Object[]{lid,versionName}));
                }
            }
            
            tx.commit();
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToDeleteRepositoryItems");
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
     * Updates a RepositoryItem.
     */
    public void update(ServerRequestContext context, RepositoryItem item) throws RegistryException {
        Transaction tx = null;
        
        ExtrinsicObjectType eo = (ExtrinsicObjectType)context.getRegistryObject(item.getId(), "ExtrinsicObject");
        String lid = eo.getLid();
        String versionName = null;
            
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
                        
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo == null) {
                contentVersionInfo = bu.rimFac.createVersionInfoType();
                eo.setContentVersionInfo(contentVersionInfo);
            }
            versionName = eo.getContentVersionInfo().getVersionName();
            
            // if item does not exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            List results = s.find(findByID, params, types);
            if (results.isEmpty()) {
                throw new RepositoryItemNotFoundException(lid, versionName);
            }
            
            // Writing out the RepositoryItem itself
            byte contentBytes[] = readBytes(item.getDataHandler().getInputStream());
            
            RepositoryItemBean bean = (RepositoryItemBean)results.get(0);
            RepositoryItemKey key = new RepositoryItemKey(lid, versionName);
            bean.setKey(key);

            if (needsOracleHack) {
                doOracleHackForUpdate(s, bean, contentBytes);
                // do not call save after this
            } else {
                if (isBlobSupported) {
                    bean.setBlobContent(Hibernate.createBlob(contentBytes));
                } else {
                    bean.setBinaryContent(contentBytes);
                }
            }
            
            if (log.isDebugEnabled()) {
                String message = "Updating repository item:"
                + "lid='" + lid + "', "
                + "versionName='" + versionName + "', ";
                if (isBlobSupported) {
                    message += "content size=" + bean.getBlobContent().length();
                } else {
                    message += "content size=" + bean.getBinaryContent().length;
                }
                log.debug(message);
            }
            
            if (!needsOracleHack) {
                s.update(bean);
            }
            
            tx.commit();
            s.refresh(bean);
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToUpdateRepositoryItem",
			                                                new Object[]{lid, versionName});

            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
    * Determines if RepositoryItem exists for specified key. 
    *
    * @return true if a RepositoryItem exists for specified key, false otherwise 
    * @param key The RepositoryItemKey.
    **/
    public boolean itemExists(RepositoryItemKey key) throws RegistryException {
        boolean found = false;
        Transaction tx = null;
        
        String lid = key.getLid();
        String versionName = key.getVersionName();
            
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            
            //Need to call clear otherwise we get a cached ReposiytoryItemBean where the txn has committed 
            //and Blob cannot be read any more. This will be better fixed when we pass ServerRequestContext
            //to each rm interface method and leverage leave txn management to ServerRequestContext.
            //See patch submitted for Sun Bug 6444810 on 6/28/2006
            s.clear();
            tx = s.beginTransaction();
            // if item does not exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            List results = s.find(findByID, params, types);
            if (!results.isEmpty()) {
                found = true;
            }
            
            tx.commit();                 
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToSearchRepositoryItem",
			                                                 new Object[] {lid, versionName});

            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
        
        return found;
    }
            
    // ---------------------------------------------------------------------- //
    // private/util methods
    // ---------------------------------------------------------------------- //
    
    /**
     * Reads bytes from InputStream untill the end of the stream.
     *
     * @param in The InputStream to be read.
     * @return the read bytes
     * @thows Exception (IOEXception...)
     */
    private byte[] readBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamReader inr = new InputStreamReader(in);
        byte bbuf[] = new byte[1024];
        int read;
        while ((read = in.read(bbuf)) > 0) {
            baos.write(bbuf, 0, read);
        }
        return baos.toByteArray();
    }
    
    protected void tryClose() throws RegistryException {
        try {
            SessionContext sc = hu.getSessionContext();
            if (reclaimUsedDBConnections) {
                try {
                    sc.getSession().disconnect();
                } catch (Throwable t) {
                    String msg = ServerResourceBundle.getInstance().getString("message.FailedCloseDatabaseSession");
                    log.error(msg);
                    log.error(t, t);
                }
            }
            sc.close();
        } catch (HibernateException e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedCloseDatabaseSession");
            log.error(e, e);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        }
    }
    
    protected void tryRollback(Transaction tx) throws RegistryException {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (Exception e) {
                String msg = ServerResourceBundle.getInstance().getString("message.failedToRollbackTransaction");
                log.error(e, e);
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
            }
        }
    }            

    private void doOracleHackForInsert(Session s, RepositoryItemBean bean, byte[] contentBytes)
    throws Exception {
        // the code below is used to for oracle only, since it requires 
        // special handling for BLOBs. First save dummy small cotent to BLOBs
        bean.setBlobContent(Hibernate.createBlob("dummy-content".getBytes("utf-8")));
        s.save(bean);
        s.flush();
        doOracleHackForUpdate(s, bean, contentBytes);
    }

    private void doOracleHackForUpdate(Session s, RepositoryItemBean bean, byte[] contentBytes)
    throws Exception {
        // grabs an Oracle LOB
        s.refresh(bean, net.sf.hibernate.LockMode.UPGRADE); 
        Object contentBlob = bean.getBlobContent();
        Class clazz = Class.forName("oracle.sql.BLOB");
        java.lang.reflect.Method method = clazz.getMethod("getBinaryOutputStream", new Class[] {});
        java.io.OutputStream contentOS = (java.io.OutputStream)
            method.invoke(contentBlob, new Object[] {});
            
        // Write directly to the OutputStreams
        contentOS.write(contentBytes);
        contentOS.flush();
        contentOS.close();
    }
    
}
