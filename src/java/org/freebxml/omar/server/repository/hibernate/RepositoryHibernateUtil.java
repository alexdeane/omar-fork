/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/RepositoryHibernateUtil.java,v 1.16 2006/09/28 02:33:44 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.util.ServerResourceBundle;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.server.common.RegistryProperties;

/**
 * A class to hold util methods for Hibernate, like session handling.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class RepositoryHibernateUtil extends AbstractHibernateUtil {
    private static final Log log = LogFactory.getLog(RepositoryHibernateUtil.class);
    
    private static RepositoryHibernateUtil instance;
    private static SessionFactory sessionFactory;
    private static Configuration configuration;
    private boolean reclaimUsedDBConnections = false;
    public static final ThreadLocal threadLocalSession = new ThreadLocal();
    
    protected RepositoryHibernateUtil() {
        reclaimUsedDBConnections = "true".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("omar.repository.hibernate.reclaimUsedDBConnections", "false"));
        getConfiguration();
    }
    
    /**
     * Singleton instance accessor.
     */
    public synchronized static RepositoryHibernateUtil getInstance() {
        if (instance == null) {
            instance = new RepositoryHibernateUtil();
        }

        return instance;
    }
    
    public synchronized SessionContext getSessionContext()
	throws HibernateException {
        SessionContext sessionContext = super.getSessionContext();
        if (reclaimUsedDBConnections) {
            Session s = sessionContext.getSession();
            if (!s.isConnected()) {
                s.reconnect();
            }
        }
        return sessionContext;
    }
    
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
 
    protected Configuration getConfiguration() {
        if (configuration == null)  {
            synchronized (RepositoryHibernateUtil.class) {
                if (configuration == null)  {
                    try {
			String cfgResource;
                        DataSource ds = null;

			boolean useConnectionPool = Boolean.
			    valueOf(RegistryProperties.getInstance().
				    getProperty("omar.persistence.rdb.useConnectionPooling",
						"true")).
			    booleanValue();
			boolean debugConnectionPool = Boolean.
			    valueOf(RegistryProperties.getInstance().
				    getProperty("omar.persistence.rdb.pool.debug",
						"false")).
			    booleanValue();

			// Try DataSource first, if configured
			if (useConnectionPool && !debugConnectionPool) {
			    cfgResource = "/repository.datasource.cfg.xml";
			    configuration =
				new Configuration().configure(cfgResource);

			    String dataSourceName = configuration
				.getProperty("connection.datasource");
			    if (dataSourceName != null &&
				!"".equals(dataSourceName)) {
				try {
				    Context ctx = new InitialContext();
				    if (ctx != null ) {
					ds = (DataSource)
					    ctx.lookup(dataSourceName);
					if (ds != null) {
					    // create a test connection to
					    // make sure all is well with
					    // DataSource
					    Connection connection = null;
					    try {
						connection =
						    ds.getConnection();
					    } catch (Exception e) {
						ds = null;
						log.info(ServerResourceBundle.
							 getInstance().
		 getString("message.UnableToCreateTestConnectionForDataSource",
			   new Object[]{dataSourceName}), e);
					    } finally {
						if (connection != null) {
						    try {
							connection.close();
						    } catch (Exception e1) {
							//Do nothing.
						    }
						}
					    }
					}
				    } else {
					log.info(ServerResourceBundle.
						 getInstance().
			       getString("message.UnableToGetInitialContext"));
				    }
				} catch (NamingException e) {
				    log.info(ServerResourceBundle.
					     getInstance().
		       getString("message.UnableToGetJNDIContextForDataSource",
				 new Object[]{dataSourceName}));
				}
			    }
			}

                        if (ds == null) {
                            // fall back to jdbc
                            cfgResource = "/repository.jdbc.cfg.xml";
                            configuration = new Configuration().configure(cfgResource);
                        }
                        
                        // support $user.home and $omar.home in omar repository cfg
                        String connUrl = configuration.getProperty("hibernate.connection.url");
                        if (connUrl != null && !"".equals(connUrl)) {
                            connUrl = substituteVariable(connUrl,
                                "$user.home", System.getProperty("user.home"));
                            connUrl = substituteVariable(connUrl,
                                "$omar.home", RegistryProperties.getInstance().getProperty("omar.home"));
                            configuration.setProperty("hibernate.connection.url", connUrl);
                        }

                        sessionFactory = configuration.buildSessionFactory();
                    } catch (HibernateException ex) {
                        throw new RuntimeException(ServerResourceBundle.getInstance().getString("message.buildingSessionFactory", new Object[]{ex.getMessage()}), ex);
                    }
                }
            }
        }
        return configuration;
    }
    
    protected ThreadLocal getThreadLocalSession() {
        return threadLocalSession;
    }
    
    protected boolean checkSchema(SessionFactory sessionFactory) throws HibernateException {
        // Tries a simple query that *should* work if database OK. Fails otherwise.
        SessionContext sc = null;
        try {
            sc = getSessionContext();
            Session s = sc.getSession();
            int howMany = ((Integer)s.iterate("select count(*) from org.freebxml.omar.server.repository.hibernate.RepositoryItemBean").next()).intValue();
            
            // got here, no exception  from query above, dutabase *should* be ok
            return true;
        } catch(HibernateException he) {
            // ops, query failed, database not ok
            return false;
        } finally {
            if(sc != null) {
                try {
                    sc.close();
                } catch (RegistryException e) {
                    log.error(e, e);
                }
            }
        }
    }

    protected void populateDB() throws HibernateException {
        // NO OP
    }
    
    public static void main(String [] args) throws Exception {
        //TODO: handle -cleandb -createdb params!!
        RepositoryHibernateUtil hibernateUtil = RepositoryHibernateUtil.getInstance();
        try {
            hibernateUtil.initDB(true);
            log.info(ServerResourceBundle.getInstance().getString("message.DatabaseCreated"));
        } catch (HibernateException e) {
            log.error(e);
            throw e;
        }
    }

    /** util method to replace variables in a string. */
    private static String substituteVariable(String value, String oldKeySubstring, String newKeySubstring) {
        if (value == null) {
            return null;
        }
        int oldKeySubstringSize = oldKeySubstring.length();
        while (true) {
            int index = value.indexOf(oldKeySubstring);
            if (index == -1) {
                break;
            }
            value = value.substring(0, index) + newKeySubstring +
                value.substring(index + oldKeySubstringSize);
        }
        return value;
    }
    
}
