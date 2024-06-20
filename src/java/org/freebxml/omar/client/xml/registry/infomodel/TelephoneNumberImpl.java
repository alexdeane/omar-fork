/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/TelephoneNumberImpl.java,v 1.9 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.TelephoneNumber;


/**
 * Implements JAXR API interface named TelephoneNumber.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class TelephoneNumberImpl implements TelephoneNumber {
    private String countryCode = null;
    private String areaCode = null;
    private String number = null;
    private String extension = null;
    private String url = null;
    private String type = null;
    private LifeCycleManagerImpl lcm = null;

    //not accessable
    private TelephoneNumberImpl() {
    }

    public TelephoneNumberImpl(LifeCycleManagerImpl lcm) {
        this.lcm = lcm;
    }

    public TelephoneNumberImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType tel) {
        this.lcm = lcm;
        areaCode = tel.getAreaCode();
        countryCode = tel.getCountryCode();
        extension = tel.getExtension();
        number = tel.getNumber();
        type = tel.getPhoneType();
    }

    public String getCountryCode() throws JAXRException {
        return countryCode;
    }

    public String getAreaCode() throws JAXRException {
        return areaCode;
    }

    public String getNumber() throws JAXRException {
        return number;
    }

    public String getExtension() throws JAXRException {
        return extension;
    }

    //TODO: Remove from JAXR 2.0
    public String getUrl() throws JAXRException {
        return url;
    }

    public String getType() throws JAXRException {
        return type;
    }

    public void setCountryCode(String par1) throws JAXRException {
        countryCode = par1;
    }

    public void setAreaCode(String par1) throws JAXRException {
        areaCode = par1;
    }

    public void setNumber(String par1) throws JAXRException {
        number = par1;
    }

    public void setExtension(String par1) throws JAXRException {
        extension = par1;
    }

    //TODO: Remove from JAXR 2.0
    public void setUrl(String par1) throws JAXRException {
        url = par1;
    }

    public void setType(String par1) throws JAXRException {
        type = par1;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.TelephoneNumber ebPhone = factory.createTelephoneNumber();

            setBindingObject(ebPhone);

            return ebPhone;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType ebTelephoneNumber)
        throws JAXRException {
        ebTelephoneNumber.setCountryCode(getCountryCode());
        ebTelephoneNumber.setAreaCode(getAreaCode());
        ebTelephoneNumber.setNumber(getNumber());
        ebTelephoneNumber.setExtension(getExtension());
        ebTelephoneNumber.setPhoneType(getType());
    }

    public String toString() {
        String str = "";

        try {
            int registryLevel = 1; //??Get from RegistryService later

            if (registryLevel == 0) {
                str = getNumber();
            } else {
                if (getCountryCode() != null) {
                    str += ("(" + getCountryCode() + ") ");
                }

                if (getAreaCode() != null) {
                    str += (getAreaCode() + "-");
                }

                if (getNumber() != null) {
                    str += getNumber();
                }

                if (getType() != null) {
                    str += (" (" + getType() + ")");
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
        }

        return str;
    }
}
