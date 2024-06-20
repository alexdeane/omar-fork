/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/OutputExceptions.java,v 1.1 2006/06/15 01:43:56 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some utility functions for consistently logging and displaying errors or
 * warning messages.  Do not create an instance of this class as all
 * methods are static.
 *
 * Note: This module does no logging on its own behalf.  All control over
 * actions in this file (such as the logging level) is deferred to the
 * caller and the passed Log instance.
 *
 * @author Doug Bunting, Sun Microsystems
 */
public class OutputExceptions {
    /**
     * Constructor is private to force use of static methods.
     */
    private OutputExceptions() {
    }

    /**
     * Choose a string about Throwable which ignores generic class names
     *
     * @param t Throwable of interest
     *     t.getMessage() must be non-null when this method is used
     * @return String message about t
     */
    private static String goodMessage(Throwable t) {
	String msg;
	String className = t.getClass().getName();

	// TODO: may want to add to this list over time
	if ("java.lang.Error" == className ||
	    "java.lang.Exception" == className ||
	    "java.lang.Throwable" == className ||
	    "javax.xml.bind.JAXBException" == className ||
	    "javax.xml.registry.JAXRException" == className ||
	    "javax.xml.registry.RegistryException" == className ||
	    "javax.xml.rpc.JAXRPCException" == className) {
	    msg = t.getMessage();
	} else {
	    msg = t.toString();
	}
	return msg;
    }

    /**
     * Find a string which should provide most information about a Throwable.
     *
     * @param t Throwable of interest
     * @param forLog If true, return more information; going into log
     * @return String message describing original cause for t
     */
    private static String getCause(Throwable t, boolean forLog) {
	// Find all causes for this exception
	ArrayList causes = new ArrayList();
	while (null != t) {
	    causes.add(t);
	    t = t.getCause();
	}
	Collections.reverse(causes);

	// Work backwards through the causes to find the original
	// (hopefully most informative) detail string
	Iterator iter = causes.iterator();
	String msg = null;
	while (iter.hasNext() && null == msg) {
	    t = (Throwable)iter.next();
	    if (null != t.getMessage()) {
		// Detail string exists
		if (forLog) {
		    // Always get class information as well
		    msg = t.toString();
		} else {
		    msg = goodMessage(t);
		}
	    }
	}

	// No detail found, use class information about root cause
	if (null == msg) {
	    msg = ((Throwable)causes.get(0)).toString();
	}

	return msg;
    }

    /**
     * Log exceptions and display them to user in a consistent fashion.
     *
     * @param log Log where error or warning should be output
     * @param logMsg Context message to log with t
     * @param displayMsg Context message to display to user with t
     * @param t Throwable to log about and optionally display
     * @param display Output information about this Throwable on page?
     * @param warning If true, log warning rather than error
     */
    private static void display(Log log,
				String logMsg,
				String displayMsg,
				Throwable t,
				boolean display,
				boolean warning) {
	// When not given a Msg, should we provide context for cause?
	boolean needContext = (null != t.getMessage() && null != t.getCause());

	// Log a fair amount of information unconditionally
	if (null == logMsg && null != displayMsg) {
	    logMsg = displayMsg;
	}
	if (log.isDebugEnabled()) {
	    // Include full traceback in logging output
	    if (null == logMsg) {
		if (warning) {
		    log.warn(t.toString(), t);
		} else {
		    log.error(t.toString(), t);
		}
	    } else {
		if (warning) {
		    log.warn(logMsg, t);
		} else {
		    log.error(logMsg, t);
		}
	    }
	} else {
	    if (null == logMsg) {
		if (needContext) {
		    if (warning) {
			log.warn(t.toString());
		    } else {
			log.error(t.toString());
		    }
		}
	    } else {
		if (warning) {
		    log.warn(logMsg);
		} else {
		    log.error(logMsg);
		}
	    }
	    if (warning) {
		log.error(getCause(t, true));
	    } else {
		log.error(getCause(t, true));
	    }
	}

	// Conditionally display a subset of the above information to the user
	if (display) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    FacesMessage.Severity severity = (warning ?
					      FacesMessage.SEVERITY_ERROR :
					      FacesMessage.SEVERITY_WARN);

	    if (null == displayMsg && null != logMsg) {
		displayMsg = logMsg;
	    }
	    if (null == displayMsg) {
		if (needContext) {
		    context.addMessage(null, new FacesMessage(severity,
							      goodMessage(t),
							      null));
		}
	    } else {
		context.addMessage(null, new FacesMessage(severity,
							  displayMsg,
							  null));
	    }
	    context.addMessage(null, new FacesMessage(severity,
						      getCause(t, false),
						      null));
	}
    }

    /**
     * Output warning with different messages for log and display
     *
     * @param log Log where warning should be output
     * @param logMsg Context message to log with t
     * @param displayMsg Context message to display to user with t
     * @param t Throwable to log about and display
     */
    public static void warn(Log log,
			    String logMsg,
			    String displayMsg,
			    Throwable t) {
	display(log, logMsg, displayMsg, t, true, true);
    }

    /**
     * Output warning to log and display with same message
     *
     * @param log Log where warning should be output
     * @param msg Context message to log and display with t
     * @param t Throwable to log about and display
     */
    public static void warn(Log log, String msg, Throwable t) {
	display(log, msg, msg, t, true, true);
    }

    /**
     * Output warning to log and display
     *
     * @param log Log where warning should be output
     * @param t Throwable to log about and display
     */
    public static void warn(Log log, Throwable t) {
	display(log, null, null, t, true, true);
    }

    /**
     * Output error with different messages for log and display
     *
     * @param log Log where error should be output
     * @param logMsg Context message to log with t
     * @param displayMsg Context message to display to user with t
     * @param t Throwable to log about and display
     */
    public static void error(Log log,
			     String logMsg,
			     String displayMsg,
			     Throwable t) {
	display(log, logMsg, displayMsg, t, true, false);
    }

    /**
     * Output error to log and display with same message
     *
     * @param log Log where error should be output
     * @param msg Context message to log and display with t
     * @param t Throwable to log about and display
     */
    public static void error(Log log, String msg, Throwable t) {
	display(log, msg, msg, t, true, false);
    }

    /**
     * Output error to log and display
     *
     * @param log Log where error should be output
     * @param msg Context message to log and display with t
     */
    public static void error(Log log, Throwable t) {
	display(log, null, null, t, true, false);
    }

    /*
     * Last two do not display information to the user.  We do not provide
     * a direct mechanism to log errors without display since user should
     * hear about all errors.
     */

    /**
     * Warn about a Throwable with a message but do not display anything to
     * user
     *
     * @param log Log where warning should be output
     * @param msg Context message to log and display with t
     * @param t Throwable to log about and display
     */
    public static void logWarning(Log log, String msg, Throwable t) {
	display(log, msg, msg, t, false, true);
    }

    /**
     * Warn about a Throwable but do not display anything to user
     *
     * @param log Log where warning should be output
     * @param t Throwable to log about
     */
    public static void logWarning(Log log, Throwable t) {
	display(log, null, null, t, false, true);
    }
}
