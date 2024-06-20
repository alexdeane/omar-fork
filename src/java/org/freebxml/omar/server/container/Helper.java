/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/container/Helper.java,v 1.1 2005/11/18 01:22:29 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.container;

import org.freebxml.omar.server.container.GenericListener;

/**
 * Interface for life cycle listener code specific to a type of database.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.1 $
 */
interface Helper {
    /**
     * Perform whatever initialization is necessary for this database type.
     * Should be called whether or not server will be started and shut down.
     *
     * @param configDirectory string identifying a directory in which
     * configuration files may be found.  Use {@code null} if container
     * provides a consistent current directory for such files.
     *
     * @param listener {@link GenericListener} from which {@link Helper}
     * may get parameters.
     */
    public void initialize(String configDirectory,
			   GenericListener listener);

    /**
     * Shut the database down smoothly.
     */
    public void shutdownDatabase();

    /**
     * Start database server, if relevant for this database type.
     *
     * @return boolean which is true if and only if a database server was
     * successfully started
     */
    public boolean startupServer();

    /**
     * Shut down database server, if any.
     */
    public void shutdownServer();
}
