/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JavaUIResourceBundle.java,v 1.3 2006/07/29 05:53:37 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.freebxml.omar.common.AbstractResourceBundle;

/**
 * For loading the resource from ResourceBundle.properties
*/
public class JavaUIResourceBundle extends AbstractResourceBundle {
	public static final String BASE_NAME =
		"org.freebxml.omar.client.ui.swing.ResourceBundle";
    private static JavaUIResourceBundle instance;
	private static Locale locale;
    private ResourceBundle bundle;

    protected JavaUIResourceBundle() {
        // Load the resource bundle of default locale
        bundle = ResourceBundle.getBundle(BASE_NAME);
    }

    protected JavaUIResourceBundle(Locale locale) {
        // Load the resource bundle of specified locale
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public synchronized static JavaUIResourceBundle getInstance() {
        if (instance == null) {
            instance = new JavaUIResourceBundle();
            locale = Locale.getDefault();
        }

        return instance;
    }

    public synchronized static JavaUIResourceBundle getInstance(Locale locale) {
        if (instance == null) {
            instance = new JavaUIResourceBundle(locale);
        } else {
            if (JavaUIResourceBundle.locale != locale) {
                instance = new JavaUIResourceBundle(locale);
            }
	}
        return instance;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }
}
