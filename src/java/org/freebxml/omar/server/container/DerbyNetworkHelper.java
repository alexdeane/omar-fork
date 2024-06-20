/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/container/DerbyNetworkHelper.java,v 1.1 2005/11/18 01:22:29 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.container;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.derby.drda.NetworkServerControl;

import org.freebxml.omar.server.container.DerbyHelper;

/**
 * {@link Helper} implementation providing Derby-specific support.  Extends
 * {@link DerbyHelper} to include starting and stopping the Derby Network
 * Server thread.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.1 $
 * @see DerbyHelper
 * @see Helper
 */
class DerbyNetworkHelper extends DerbyHelper {
    // for logging
    private static Log log = LogFactory.getLog(DerbyNetworkHelper.class);
    private static String rbName =
	DerbyNetworkHelper.class.getPackage().getName() + ".ResourceBundle";
    private static ResourceBundle rb = ResourceBundle.getBundle(rbName);

    // Derby control object
    private NetworkServerControl server = null;

    /**
     * class constructor
     */
    protected DerbyNetworkHelper() {
	super();
	try {
	    server = new NetworkServerControl();
	} catch (Exception e) {
	    // ??? Add message for this case
	    log.error(e);
	}
    }

    /**
     * {@inheritDoc}
     * In this case, we know from experimentation the exeptions Derby
     * raises in the normal situations.
     */
    protected void logDatabaseException(SQLException sqlException) {
	// likely an expected shutdown exception but have not done the full
	// set of experiments yet; know if this dB has not been used,
	// exception is "not found"; otherwise, "shutdown" is treated as an
	// exception
	// ??? Catch these exceptions and do not bother logging them
	// ??? log.debug(rb.getString("message.stoppedDatabase"));
	super.logDatabaseException(sqlException);
    }

    /**
     * {@inheritDoc}
     * In this case, start the Derby Network Server thread.
     */
    public boolean startupServer() {
	boolean retVal = false;

	if (null != server) {
	    try {
		log.info(rb.getString("message.startingDerbyServer"));
		server.start(null);
		log.debug(rb.getString("message.startedDerbyServer"));
		retVal = true;
	    } catch (Exception e) {
		log.error(rb.getString("message.failureStartingDerbyServer"),
			  e);
	    }
	}

	return retVal;
    }

    /**
     * {@inheritDoc}
     * In this case, stop the Derby Network Server thread.
     */
    public void shutdownServer() {
	if (null != server) {
	    try {
		log.info(rb.getString("message.stoppingDerbyServer"));
		server.shutdown();
		log.debug(rb.getString("message.stoppedDerbyServer"));
	    } catch (Exception e) {
		log.error(rb.getString("message.failureStoppingDerbyServer"),
			  e);
	    }
	}
    }
}
