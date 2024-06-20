/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/LocalizedStringImpl.java,v 1.11 2007/05/04 18:42:37 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;

import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.LocalizedString;


//import org.oasis.ebxml.registry.bindings.rim.*;

/**
 * Implements JAXR API interface named LocalizedString.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class LocalizedStringImpl implements LocalizedString {
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private Locale locale = null;
    private String value = null;
    private String charsetName = null;
    private boolean modified = false;

    private LifeCycleManagerImpl lcm;

    public LocalizedStringImpl(LifeCycleManagerImpl lcm) {
        this.lcm = lcm;
    }

    public LocalizedStringImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.LocalizedStringType ebObj) {
        this(lcm);

        //Need to parse language and country from lang
        String xsdLang = ebObj.getLang();
        locale = xsdLang2Locale(xsdLang);

        value = ebObj.getValue();
        charsetName = ebObj.getCharset();
    }

    public boolean isModified() {
        return modified;
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    private Locale xsdLang2Locale(String xsdLang) {
        Locale locale = null;

        //Need to see if following is robust enough for all cases.
        // For now assuming 3 formats: xx, xx-yy, or xx-yy-zz where xx is
	// language, yy is country, and zz is variant
        String language = xsdLang.substring(0, 2);

        if (xsdLang.length() >= 5) {
            String country = xsdLang.substring(3, 5);
	    if (xsdLang.length() == 5) {
		locale = new Locale(language, country);
	    } else {
		String variant = xsdLang.substring(7);
		locale = new Locale(language, country, variant);
	    }
        } else {
            locale = new Locale(language);
        }

        return locale;
    }

    private String locale2xsdLang(Locale locale) {
        String xsdLang = locale.toString().replace('_','-');

        return xsdLang;
    }

    public String getCharsetName() throws JAXRException {
        String csName = null;

        if (charsetName == null || "".equals(charsetName.trim())) {
            csName = DEFAULT_CHARSET_NAME;
        } else {
            csName = charsetName;
        }

        return csName;
    }

    public Locale getLocale() throws JAXRException {
        Locale l = null;

        if (locale == null) {
            l = Locale.getDefault();
        } else {
            l = locale;
        }

        return l;
    }

    public String getValue() throws JAXRException {
        return value;
    }

    public void setCharsetName(String par1) throws JAXRException {
        if ((charsetName != null && !charsetName.equals(par1)) || (charsetName == null && par1 != null)) {
            charsetName = par1;
            setModified(true);
        }
    }

    public void setLocale(Locale par1) throws JAXRException {
        if ((locale != null && !locale.equals(par1)) || (locale == null && par1 != null)){
            locale = par1;
            setModified(true);
        }
    }

    public void setValue(String par1) throws JAXRException {
        if ((value != null && !value.equals(par1)) || (value == null && par1 != null)) {
            value = par1;
            setModified(true);
        }
    }

    public Object toBindingObject() throws JAXRException {
        org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

        try {
            org.oasis.ebxml.registry.bindings.rim.LocalizedString ebLS = bu.rimFac.createLocalizedString();
            ebLS.setLang(locale2xsdLang(getLocale()));
            ebLS.setValue(getValue());
            ebLS.setCharset(getCharsetName());

            return ebLS;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    public Object clone() {
        LocalizedStringImpl _clone = null;

        try {
            _clone = new LocalizedStringImpl(lcm);
            _clone.setCharsetName(getCharsetName());
            _clone.setLocale(getLocale());
            _clone.setValue(getValue());
            setModified(false);
        } catch (JAXRException e) {
            //Cannot happen.
            e.printStackTrace();
        }

        return _clone;
    }
    
}
