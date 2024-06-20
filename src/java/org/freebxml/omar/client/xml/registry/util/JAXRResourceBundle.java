/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/JAXRResourceBundle.java,v 1.2 2006/07/29 05:53:37 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.registry.JAXRException;

import org.freebxml.omar.common.AbstractResourceBundle;

/**
 * Internationalization utilities
 *
 * This class expects to obtain messages from a ResourceBundle in the
 * same package, with a baseName of "messages".  Typically, this is
 * done using Java property files loaded via PropertyResourceBundle.
 *
 * @author Edwin Goei
 */
public class JAXRResourceBundle extends AbstractResourceBundle {
    private static final JAXRResourceBundle INSTANCE = new JAXRResourceBundle();
    private ResourceBundle bundle;
    public static final String BASE_NAME =
		"org.freebxml.omar.client.xml.registry.util.ResourceBundle";


    private JAXRResourceBundle() {
        // May want to add a setLocale() method in the future??
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static JAXRResourceBundle getInstance() {
        return INSTANCE;
    }

    protected Object handleGetObject(String key) {
        Object ret = null;

        try {
            ret = bundle.getObject(key);
        } catch (MissingResourceException x) {
	    // ??? The following line should probably be executed only if a
	    // ??? debug or verbose option is on somewhere.  Where?
            x.printStackTrace();
            ret = "[MissingResourceException] key=" + key;
        }

        return ret;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public JAXRException newJAXRException(String key) {
        return new JAXRException(getString(key));
    }

    public JAXRException newJAXRException(String key, Object[] params) {
        return new JAXRException(getString(key, params));
    }
}
