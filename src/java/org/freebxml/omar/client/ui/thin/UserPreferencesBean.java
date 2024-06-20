/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/UserPreferencesBean.java,v 1.5 2005/06/10 18:59:00 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

/**
 * A backing bean for user preferences
 *
 * @author  Diego Ballve / Digital Artefacts Europe
 */
public class UserPreferencesBean {
    
    /** The log. */
    private static final Log log = LogFactory.getLog(UserPreferencesBean.class);

    /**
     * Holds value of property contentLocale.
     */
    private Locale contentLocale;

    /**
     * Holds value of property uiLocale.
     */
    private Locale uiLocale;

    /**
     * Holds value of property charset.
     */
    private String charset;

    //cache
    private Collection allLocalesSelectItems;
    private Collection supportedLocalesSelectItems;
    
    /** Creates a new instance of LocaleBean */
    public UserPreferencesBean() {
        initLocales();
    }

    /** Sets the locales to 1st supported locale (directly or indirectly). */
    private void initLocales() {
        Iterator requestLocales = FacesContext.getCurrentInstance().getExternalContext().getRequestLocales();
        Collection supportedLocales = getSupportedUiLocales();
        //iterate though client preferred locales until find supported 
        while (requestLocales.hasNext()) {
            Locale loc = (Locale)requestLocales.next();
            // try direct match
            if (supportedLocales.contains(loc)) {
                init(loc);
                return;
            }
            // try to use language country, without variant
            if (loc.getVariant() != null && !"".equals(loc.getVariant())) {
                loc = new Locale(loc.getLanguage(), loc.getCountry());
                if (supportedLocales.contains(loc)) {
                    init(loc);
                    return;
                }
            }
            // try to use language without country and variant
            if (loc.getCountry() != null && !"".equals(loc.getCountry())) {
                loc = new Locale(loc.getLanguage());
                if (supportedLocales.contains(loc)) {
                    init(loc);
                    return;
                }
            }
        }
        // fall to default locale, from properties (or en_US, if not defined)
        init(getDefaultLocale());
    }
    
    private void init(Locale loc) {
        uiLocale = loc;
        contentLocale = loc;
        charset = "UTF-8";        
    }
    
    public int getNumSupportedUiLocales() {
        return getSupportedUiLocales().size();
    }
    
    /** Returns a Collection of available Locales. */
    public Collection getSupportedUiLocales() {
        String supportedLocales = ProviderProperties.getInstance().
            getProperty("omar.client.thinbrowser.supportedlocales", "en_US");
        StringTokenizer tkz = new StringTokenizer(supportedLocales, "|");
        ArrayList locales = new ArrayList();
        while (tkz.hasMoreTokens()) {
            Locale locale = parseLocale(tkz.nextToken());
            if (locale != null) {
                locales.add(locale);
            }
        }
        return locales;
    }

    /** returns the localized supported locales display names */
    public Collection getSupportedUiLocalesDisplayNames() {
        String supportedLocales = ProviderProperties.getInstance().
        getProperty("omar.client.thinbrowser.supportedlocales", "en_US");
        StringTokenizer tkz = new StringTokenizer(supportedLocales, "|");
        ArrayList locales = new ArrayList();
        while (tkz.hasMoreTokens()) {
            Locale locale = parseLocale(tkz.nextToken());
            if (locale != null && this.uiLocale != null) {
                locales.add(locale.getDisplayName(this.uiLocale) + " (" + locale + ")");
            }
        }
        return locales;
    }
    
    /** Returns a Collection of available locales as SelectItems. */
    public Collection getSupportedLocalesSelectItems() {
        if (supportedLocalesSelectItems == null) {
            ArrayList selectItems = new ArrayList();
            for (Iterator it = getSupportedUiLocales().iterator(); it.hasNext(); ) {
                Locale loc = (Locale)it.next();
                SelectItem item = new SelectItem(loc.toString(), loc.getDisplayName(loc));
                selectItems.add(item);
            }
            supportedLocalesSelectItems = selectItems;
        }
        return supportedLocalesSelectItems;
    }

    /** Returns a Collection of all locales as SelectItems. */
    public Collection getAllLocalesSelectItems() {
        if (allLocalesSelectItems == null) {
            SortedMap selectItemsMap = new TreeMap();
            Locale loc[] = Locale.getAvailableLocales();
            for (int i = 0; i < loc.length; i++) {
                String name = loc[i].getDisplayName(loc[i]);
                SelectItem item = new SelectItem(loc[i].toString(), name);
                selectItemsMap.put(name.toLowerCase(), item);
            }
            Collection all = new ArrayList();
            all.addAll(selectItemsMap.values());
            allLocalesSelectItems = all;
        }
        return allLocalesSelectItems;
    }
    
    public Locale getDefaultLocale() {
        final String DEFAULT_LOCALE = "en_US";
        String defaultLocaleSt = ProviderProperties.getInstance().
            getProperty("omar.client.thinbrowser.defaultlocale", DEFAULT_LOCALE);
        Locale loc = parseLocale(defaultLocaleSt);
        if (loc != null) {
            return loc;
        } else {
            return parseLocale(DEFAULT_LOCALE);
        }
    }
    
    /** Returns a Locale, or null if failed to create one. */
    public static Locale parseLocale(String locale) {
        if (locale == null) {
            return null;
        }
	String[] components = locale.split("_");
	Locale newLocale = null;
	if (components.length == 1) {
	    newLocale = new Locale(components[0]);
	} else if (components.length == 2) {
	    newLocale = new Locale(components[0], components[1]);
	} else if (components.length == 3) {
	    newLocale = new Locale(components[0], components[1], components[2]);
	} else {
            if (log.isWarnEnabled()) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.InvalidLocale", new Object[]{locale}));
            }
            return null;
	}

	return newLocale;
    }    

    /** Event listener for UI Locale change. */
    public void changeUiLocaleCode(ValueChangeEvent event) {
        setUiLocaleCode((String)event.getNewValue());
    }
    
    /** Event listener for Content Locale change. */
    public void changeContentLocaleCode(ValueChangeEvent event) {
        setContentLocaleCode((String)event.getNewValue());
    }
    
    /** This method can be used to obtain the current language preferences
     * from the web browser */
    public String resetLocale() {
        initLocales();
        return "";
    }
    
    /**
     * Getter for property contentLocale.
     * @return Value of property contentLocale.
     */
    public Locale getContentLocale() {

        return this.contentLocale;
    }

    /**
     * Setter for property contentLocale.
     * @param contentLocale New value of property contentLocale.
     */
    public void setContentLocale(Locale contentLocale) {

        this.contentLocale = contentLocale;
    }

    /**
     * Getter for property uiLocale.
     * @return Value of property uiLocale.
     */
    public Locale getUiLocale() {

        return this.uiLocale;
    }

    /**
     * Setter for property uiLocale.
     * @param uiLocale New value of property uiLocale.
     */
    public void setUiLocale(Locale uiLocale) {

        this.uiLocale = uiLocale;
    }

    /**
     * Getter for property charset.
     * @return Value of property charset.
     */
    public String getCharset() {

        return this.charset;
    }

    /**
     * Setter for property charset.
     * @param charset New value of property charset.
     */
    public void setCharset(String charset) {

        this.charset = charset;
    }

    /**
     * Getter for property contentLocaleCode.
     * @return Value of property contentLocaleCode.
     */
    public String getContentLocaleCode() {

        return this.contentLocale.toString();
    }

    /**
     * Setter for property contentLocaleCode.
     * @param contentLocaleCode New value of property contentLocaleCode.
     */
    public void setContentLocaleCode(String contentLocaleCode) {

        this.contentLocale = parseLocale(contentLocaleCode);
    }

    /**
     * Getter for property uiLocaleCode.
     * @return Value of property uiLocaleCode.
     */
    public String getUiLocaleCode() {

        return this.uiLocale.toString();
    }

    /**
     * Setter for property uiLocaleCode.
     * @param uiLocaleCode New value of property uiLocaleCode.
     */
    public void setUiLocaleCode(String uiLocaleCode) {

        this.uiLocale = parseLocale(uiLocaleCode);
    }

    public boolean isNameBeforeAddress() {
        boolean nameBeforeAddress = true;
        String uiLocaleCode = getUiLocaleCode();
        if (uiLocaleCode.indexOf("zh") != -1 ||
            uiLocaleCode.indexOf("ko") != -1 ||
            uiLocaleCode.indexOf("ja") != -1) {
            nameBeforeAddress = false;
        }
        return nameBeforeAddress;
    }
     
}
