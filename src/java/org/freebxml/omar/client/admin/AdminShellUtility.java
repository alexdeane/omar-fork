/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminShellUtility.java,v 1.5 2006/02/08 18:38:38 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.admin.AdminResourceBundle;

public class AdminShellUtility {
    private static final Log log = LogFactory.getLog(AdminShellUtility.class.getName());
    private static AdminShellUtility instance; //singleton instance

    protected AdminResourceBundle rb = AdminResourceBundle.getInstance();

    /** Creates a new instance of AdminShellFactory */
    protected AdminShellUtility() {
    }

    public synchronized static AdminShellUtility getInstance() {
        if (instance == null) {
            instance = new AdminShellUtility();
        }

        return instance;
    }

    public String normalizeArgs(String args) {
	/*
	 * Pattern is choice of:
	 *  - '"' (not preceded by '\') followed zero or more of:
	 *     - Not a '"' or
	 *     - '\"'
	 *  - '"' that is preceded by '\'
	 *  - Not a '"'
	 */
	Pattern p =
	    Pattern.compile("(?<!\\\\)\"(([^\"]|\\\\\")*)\"|(?<=\\\\)\"|[^\"]+");

	Matcher m = p.matcher(args);

	String matcherUseArgs = "";

	int prevEnd = 0;
	while (m.find()) {
	    prevEnd = m.end();

	    log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				   "debug.group",
				   new Object[] {m.group()}));
	    String quotedString = m.group(1);

	    if (quotedString != null) {
		matcherUseArgs += quotedString.replaceAll(" ", "\\\\ ");
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				       "debug.quoted",
				       new Object[] {quotedString.
						     replaceAll(" ", "\\\\ ")}));
	    } else {
		matcherUseArgs += m.group();
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				       "debug.unquoted",
				       new Object[] {m.group()}));
	    }
	}

	if (prevEnd < args.length()) {
	    String remainder = args.substring(prevEnd);
	    log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				   "debug.remainder",
				   new Object[] {remainder}));

	    if (remainder.matches(".*(?<!\\\\)\".*")) {
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                       "unbalancedQuotes"));
	    } else {
		matcherUseArgs += remainder;
	    }
	}

	log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       "debug.matcher",
			       new Object[] {matcherUseArgs}));

	String useArgs = args;

	String[] useArgsArray = useArgs.split("(?<=^|[^\\\\])\"", -1);

	useArgs = useArgsArray[0];

	if (log.isDebugEnabled()) {
	    log.debug(args);

	    for (int i = 0; i < useArgsArray.length; i++) {
		log.debug(useArgsArray[i] + ":");
	    }
	    log.debug("");
	}

	if ((useArgsArray.length > 1)) {
	    // An even number of quotes results in an odd-number array length
	    if (useArgsArray.length % 2 == 0) {
		log.error(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                       "unbalancedQuotes"));
		return null;
	    }

	    for (int i = 1; i < useArgsArray.length; i += 2) {
		useArgsArray[i] = useArgsArray[i].replaceAll(" ", "\\\\ ");
	    }

	    for (int i = 1; i < useArgsArray.length; i++) {
		useArgs += useArgsArray[i];
	    }
	}

	useArgs = useArgs.replaceAll("\\\\\"", "\"");
	useArgs = useArgs.replaceAll("\\\\ ", " ");

	if (log.isDebugEnabled()) {
	    log.debug(":" + useArgs + ":");
	}

	return useArgs;
    }
}
