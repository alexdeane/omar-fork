/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/CommonResourceBundle.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common;

import org.freebxml.omar.common.AbstractResourceBundle;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * For loading the resource from ResourceBundle.properties
*/
public class CommonResourceBundle extends AbstractResourceBundle {
    public static final String BASE_NAME = "org.freebxml.omar.client.common.ResourceBundle";
    private static CommonResourceBundle instance;
    private static Locale locale;
    private ResourceBundle bundle;

    protected CommonResourceBundle() {
        // Load the resource bundle of default locale
        bundle = ResourceBundle.getBundle(BASE_NAME);
    }

    protected CommonResourceBundle(Locale locale) {
        // Load the resource bundle of specified locale
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public synchronized static CommonResourceBundle getInstance() {
        if (instance == null) {
            instance = new CommonResourceBundle();
            locale = Locale.getDefault();
        }

        return instance;
    }

    public synchronized static CommonResourceBundle getInstance(Locale locale) {
        if (instance == null) {
            instance = new CommonResourceBundle(locale);
        } else {
            if (CommonResourceBundle.locale != locale) {
                instance = new CommonResourceBundle(locale);
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
