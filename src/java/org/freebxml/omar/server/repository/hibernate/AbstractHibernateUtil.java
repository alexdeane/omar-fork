/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/AbstractHibernateUtil.java,v 1.2 2006/10/09 19:33:11 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;

/**
 * A class to hold util methods for Hibernate, like session handling.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
abstract class AbstractHibernateUtil {
    private static final Log log = LogFactory.getLog(AbstractHibernateUtil.class);

    //private static final SessionFactory sessionFactory;
    //private static final ThreadLocal session = new ThreadLocal();
    protected abstract ThreadLocal getThreadLocalSession();
    protected abstract SessionFactory getSessionFactory();
    protected abstract boolean checkSchema(SessionFactory sessionFactory)
	    throws HibernateException;
    protected abstract void populateDB() throws HibernateException ;
    protected abstract Configuration getConfiguration();

/*    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            throw new RuntimeException("Exception building SessionFactory: " +
				       ex.getMessage(), ex);
        }
    }*/

    public void initDB(boolean forceCreate) throws HibernateException {
        //Check if  database exists. If not, create it.
        //TODO: Check all cfg database, use init method at 1st use, not static.
        if (forceCreate || !checkSchema(getSessionFactory())) {
            createDB(getSessionFactory());
            populateDB();
        }
    }


    public synchronized SessionContext getSessionContext()
	    throws HibernateException {
        boolean isNew = false;
        Session s = (Session) getThreadLocalSession().get();

        // Open a new Session, if this Thread has none yet
        if (s == null) {
            isNew = true;
            s = getSessionFactory().openSession();
            getThreadLocalSession().set(s);
        }
        if (!s.isConnected()) {
            s.reconnect();
        }
        SessionContext sessionContext = new  SessionContext(s, isNew);
        return sessionContext;
    }

    private void createDB(SessionFactory sf) throws HibernateException {
        log.info(ServerResourceBundle.getInstance().
		 getString("message.RebuildingDBSchema"));

        SchemaExport schemaManager = new SchemaExport(getConfiguration());

        // Remove the DB schema
        schemaManager.drop(true, true);

        // Export the DB schema
        schemaManager.create(true, true);

        // Above method will log error but throw no exception. Check
        // database again.
        if (!checkSchema(sf)) {
            //throw new HibernateException(ServerResourceBundle.getInstance().
	    //			       getString("message.RebuildDBSchemaFailed"));
            throw new HibernateException(ServerResourceBundle.getInstance().
				      getString("message.FailedToCreateDatabase"));
        }
    }
}
