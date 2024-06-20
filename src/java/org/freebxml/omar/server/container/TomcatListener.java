/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/container/TomcatListener.java,v 1.1 2005/11/18 01:22:29 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.container;

import java.lang.reflect.Constructor;
import java.util.ResourceBundle;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.server.container.GenericListener;
import org.freebxml.omar.server.container.Helper;

/**
 * Class Declaration for TomcatListener.
 *
 * @author Doug Bunting, Sun Microsystems
 * @see 
 */
public class TomcatListener extends GenericListener
    implements org.apache.catalina.LifecycleListener {
    // execution arm
    private Helper helper;
    private boolean helperCreateAttempted = false;

    // did we successfully start a database server?
    private boolean startedServer = false;

    // for logging
    private static Log log = LogFactory.getLog(TomcatListener.class);
    private static String rbName =
	TomcatListener.class.getPackage().getName() + ".ResourceBundle";
    private static ResourceBundle rb = ResourceBundle.getBundle(rbName);

    /**
     * class constructor
     */
    public TomcatListener() {
	super();
    }

    /**
     * Main entry point for this class.  Called whenever an application or
     * container life cycle event occurs.
     * <p>Choice between application and container events is based on how
     * the controlling Tomcat {@code <Listener/>} element is placed.  If
     * {@code <Listener/>} appears in server.xml file (normally directly
     * beneath the {@code <Server>} element), container life cycle events
     * are tracked.  Otherwise (if it is within a {@code <Context>}
     * element), application life cycle events are tracked.</p>
     *
     * @see org.apache.catalina.LifecycleListener#lifecycleEvent(LifecycleEvent)
     */
    public void lifecycleEvent(LifecycleEvent event) {
	String dataStr = "<null>";
	String lifecycleStr = "<null>";

	if (null == helper) {
	    if (!createHelper()) {
		// avoid NPEs later
		return;
	    }
	}

	if (log.isDebugEnabled()) {
	    Object data = event.getData();
	    if (null != data) {
		dataStr = data.toString();
	    }

	    Lifecycle lifecycle = event.getLifecycle();
	    if (null != lifecycle) {
		lifecycleStr = lifecycle.toString();
	    }
	}

	String type = event.getType();
	if (type.equals(Lifecycle.START_EVENT)) {
	    log.debug("start: getData() returned " + dataStr);
	    log.debug("start: getLifecycle() returned " + lifecycleStr);

	    helper.initialize(System.getProperty("catalina.base") +
			      System.getProperty("file.separator") + "conf",
			      this);
	    if (startupServer) {
		startedServer = helper.startupServer();
	    }
	} else if (type.equals(Lifecycle.STOP_EVENT)) {
	    log.debug("stop: getData() returned " + dataStr);
	    log.debug("stop: getLifecycle() returned " + lifecycleStr);

	    if (shutdownDatabase) {
		helper.shutdownDatabase();
	    }
	    if (startedServer && shutdownServer) {
		helper.shutdownServer();
	    }
	}
    }

    private synchronized boolean createHelper() {
	boolean retVal = (null != helper);

	// are we done already or have we tried (and failed) before?
	if (!helperCreateAttempted) {
	    helperCreateAttempted = true;

	    // attempt creation of specified Helper class
	    if (null == helperClass || 0 == helperClass.length()) {
		log.error(rb.getString("message.incorrectHelperConfig"));
	    } else {
		try {
		    helper = (Helper)Class.forName(helperClass).
			newInstance();
		    retVal = true;
		} catch (ClassNotFoundException e) {
		    // this likely results from a configuration error:
		    // unable to instantiate the helper class at all; make
		    // it clear reconfiguration will improve the situation
		    // ??? Add message for this case
		    log.warn(e);
		} catch (NoClassDefFoundError e) {
		    // if using the default configuration, this is the
		    // expected exception when derbynet.jar is not in the
		    // classpath; fallback also part of the default
		    // configuration
		    // ??? Add message for this case
		    log.warn(e);
		} catch (Throwable t) {
		    // as in both the above cases, fallback should work
		    // ??? Add message for this case
		    log.warn(t);
		}
	    }

	    if (!retVal) {
		// attempt creation of specified fallback Helper class
		if (null == helperFallbackClass ||
		    0 == helperFallbackClass.length()) {
		    log.error(rb.getString("message.noHelperFallback"));
		} else {
		    try {
			helper = (Helper)Class.forName(helperFallbackClass).
			    newInstance();
			retVal = true;
		    } catch (ClassNotFoundException e) {
			// this likely results from a configuration error:
			// unable to instantiate the fallback helper class
			// ??? Add message for this case
			log.error(e);
		    } catch (NoClassDefFoundError e) {
			// a bit bizarre: configured fallback class is not
			// generic and depends on something not in our
			// environment
			// ??? Add message for this case
			log.error(e);
		    } catch (Throwable t) {
			// ??? Add message for this case
			log.error(t);
		    }
		}
	    }
	}

	return retVal;
    }
}
