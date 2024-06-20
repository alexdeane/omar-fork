/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/SessionContext.java,v 1.2 2006/08/24 20:41:52 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import javax.xml.registry.RegistryException;
import net.sf.hibernate.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;

/**
 * Provides context about a Session.
 * Specifically one can determine if the Session was
 * newly allocated or reused from past allocation.
 *
 * @author  Farrukh S. Najmi
 */
public class SessionContext {
    private boolean isNew = true;
    private Session session = null;
    
    private static final Log log = LogFactory.getLog(SessionContext.class);

    private SessionContext() {}
    
    public SessionContext(Session s, boolean isNew) {
        this.session = s;
        this.isNew = isNew;
    }
    
    public Session getSession() {
        return session;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    public void close() throws RegistryException {
        try {
            //Only close session if new
            if (isNew)  {
                if ((session != null) && (session.isOpen())) {
                    session.close();
                }
            }
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedCloseDatabaseSession");
            log.error(e, e);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        }        
    }
}
