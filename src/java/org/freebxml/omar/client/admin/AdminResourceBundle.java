/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminResourceBundle.java,v 1.4 2006/08/24 20:41:54 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.AbstractResourceBundle;
import org.freebxml.omar.client.admin.AdminShell;

/**
 * This class is used to load localized strings
 *
 * @author  Doug Bunting / Sun Microsystems
 *
 * Copied from common/CommonResourceBundle.java
 */
public class AdminResourceBundle extends AbstractResourceBundle {

    private static final String BASE_NAME =
	AdminShell.ADMIN_SHELL_RESOURCE_BASE + ".ResourceBundle";

    // ??? TODO: Add logging as necessary...
    private static final Log log = LogFactory.getLog(AdminResourceBundle.class);
    private static AdminResourceBundle instance = null;
    private static Locale locale = null;

    private ResourceBundle bundle = null;

    protected AdminResourceBundle() {
        // Load the resource bundle of default locale
        bundle = ResourceBundle.getBundle(BASE_NAME);
    }

    protected AdminResourceBundle(Locale locale) {
        // Load the resource bundle of specified locale
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static AdminResourceBundle getInstance() {
	Locale locale = Locale.getDefault();

	// Covers case when default locale has changed since we were last
	// called.
        return getInstance(locale);
    }

    public synchronized static AdminResourceBundle getInstance(Locale locale) {
        if (instance == null) {
            instance = new AdminResourceBundle(locale);
            AdminResourceBundle.locale = locale;
        } else {
            if (AdminResourceBundle.locale != locale) {
                instance = new AdminResourceBundle(locale);
                AdminResourceBundle.locale = locale;
            }
	}

        return instance;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    // Since this code is used only in the context of a CLI with a single
    // locale, calls to this variant of getBundle() are likely errors.  We
    // maintain the singleton invariant just in case...
    public ResourceBundle getBundle(Locale locale) {
        return getInstance(locale).getBundle();
    }
}
