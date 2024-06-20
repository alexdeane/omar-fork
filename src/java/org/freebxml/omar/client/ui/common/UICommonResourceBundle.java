/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/common/UICommonResourceBundle.java,v 1.2 2006/07/29 05:53:37 dougb62 Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.ui.common;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.FacesContext;

import org.freebxml.omar.common.AbstractResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to load i18n and other resources from a resource bundle
 * file.
 *
 * @author  Paul Sterk
 */
public class UICommonResourceBundle extends AbstractResourceBundle {

    private ResourceBundle resourceBundle = null;
    private static UICommonResourceBundle instance = null;
    private static String messageBundleName = null;
    private static final Log log = LogFactory.getLog(UICommonResourceBundle.class);

    private UICommonResourceBundle() {
        initResources();
    }

    protected void initResources() {
        try {
            messageBundleName = FacesContext.getCurrentInstance()
                                            .getApplication()
                                            .getMessageBundle();
            if (messageBundleName == null) {
                messageBundleName = "org.freebxml.omar.client.ui.common.ResourceBundle";
            }
        }
        catch(Exception e) {
            messageBundleName = "org.freebxml.omar.client.ui.common.ResourceBundle";
        }
    }

    private ResourceBundle getLocalizedResourceBundle() {
        try {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            return ResourceBundle.getBundle(messageBundleName, locale);
        }
        catch(Exception e) {
            return  ResourceBundle.getBundle(messageBundleName);
        }
    }

    /**
     * Implement Singleton class, this method is only way to get this object.
     */
    public synchronized static UICommonResourceBundle getInstance() {
        if (instance == null) {
            instance = new UICommonResourceBundle();
        }
        return instance;
    }

    public ResourceBundle getBundle() {
        getInstance();
        return getLocalizedResourceBundle();
    }


    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(messageBundleName, locale);
    }

    /**
     * Gets an object for the given key from this resource bundle or one of
     * its parents.
     *  @param key
     *  The key to use to obtain the resource
     */
    protected Object handleGetObject(String key) {
        Object ret = null;

        try {
            ret = getBundle().getObject(key);
        } catch (MissingResourceException ex) {
            log.debug("Could not find resource with this key: "+key+
                " Please check your resource file");
        }
        if (ret == null || ret.equals("")) {
            // Try converting key to Camel Case
            // 1. remove empty spaces
            key = key.replaceAll(" ", "");
            // 2. convert first char to lower case
            key = key.substring(0,1).toLowerCase() + key.substring(1);
            try {
                ret = getBundle().getObject(key);
            } catch (MissingResourceException ex) {
		ret = null;
            }

	    // ??? Though it is not clear getObject() ever returns null,
	    // ??? leave this code outside exception handler above for now.
	    // ??? (no harm)
            if (ret == null) {
                log.warn(getString("missingMessage", new Object[]{key}));
                // TODO: refactor this into AbstractResourceBundle
                ret = "???"+key+"???";
            }
        }
        return ret;
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     * If this string is null, use the defaultString
     *
     * @param key
     *  The key to use to obtain the resource
     * @param defaultString
     *  The default string to use if the obtained resource is null
     */
    public String getString(String key, String defaultString) {
        String resourceString = getString(key);
        if (resourceString == null || resourceString.equals("")) {
            if (defaultString == null) {
                throw new NullPointerException(getString("excDefaultStringIsNull"));
            }
            resourceString = defaultString;
        }
        return resourceString;
    }

}
