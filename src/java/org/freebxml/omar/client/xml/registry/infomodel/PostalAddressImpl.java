/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/PostalAddressImpl.java,v 1.8 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.Slot;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;


/**
 * Implements JAXR API interface named PostalAddress.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class PostalAddressImpl extends ExtensibleObjectImpl
    implements PostalAddress {
    private String street = null;
    private String streetNumber = null;
    private String city = null;
    private String stateOrProvince = null;
    private String postalCode = null;
    private String country = null;
    private String type = null;
    private ClassificationScheme postalScheme = null;

    private PostalAddressImpl() throws JAXRException {
        super(null);
    }

    public PostalAddressImpl(LifeCycleManagerImpl lcm)  throws JAXRException {
        super(lcm);
    }

    public PostalAddressImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.PostalAddressType address)  throws JAXRException {
        super(lcm);

        // Todo: Pass ExtensibleObject components to super class???
        if (address == null) {
            return;
        }

        city = address.getCity();
        country = address.getCountry();
        postalCode = address.getPostalCode();
        stateOrProvince = address.getStateOrProvince();
        street = address.getStreet();
        streetNumber = address.getStreetNumber();
    }

    public String getStreet() throws JAXRException {
        if (street == null) {
            street = "";
        }

        return street;
    }

    public void setStreet(String par1) throws JAXRException {
        street = par1;
    }

    public String getStreetNumber() throws JAXRException {
        if (streetNumber == null) {
            streetNumber = "";
        }

        return streetNumber;
    }

    public void setStreetNumber(String par1) throws JAXRException {
        streetNumber = par1;
    }

    public String getCity() throws JAXRException {
        if (city == null) {
            city = "";
        }

        return city;
    }

    public void setCity(String par1) throws JAXRException {
        city = par1;
    }

    public String getStateOrProvince() throws JAXRException {
        if (stateOrProvince == null) {
            stateOrProvince = "";
        }

        return stateOrProvince;
    }

    public void setStateOrProvince(String par1) throws JAXRException {
        stateOrProvince = par1;
    }

    public String getPostalCode() throws JAXRException {
        if (postalCode == null) {
            postalCode = "";
        }

        return postalCode;
    }

    public void setPostalCode(String par1) throws JAXRException {
        postalCode = par1;
    }

    public String getCountry() throws JAXRException {
        if (country == null) {
            country = "";
        }

        return country;
    }

    public void setCountry(String par1) throws JAXRException {
        country = par1;
    }

    public String getType() throws JAXRException {
        if (type == null) {
            type = "";
        }

        return type;
    }

    public void setType(String par1) throws JAXRException {
        type = par1;
    }

    public void setPostalScheme(ClassificationScheme par1)
        throws JAXRException {
        postalScheme = par1;
    }

    public ClassificationScheme getPostalScheme() throws JAXRException {
        return postalScheme;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Address ebAddress = factory.createAddress();

            setBindingObject(ebAddress);

            return ebAddress;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.PostalAddressType ebAddr)
        throws JAXRException {
        ebAddr.setStreet(street);
        ebAddr.setStreetNumber(streetNumber);
        ebAddr.setCity(city);
        ebAddr.setStateOrProvince(stateOrProvince);
        ebAddr.setPostalCode(postalCode);
        ebAddr.setCountry(country);

        //ebsetType(type);
    }

    public String toString() {
        String addrStr = "";

        try {
            addrStr = getStreetNumber() + " " + getStreet() + ", " + getCity() +
                " " + getStateOrProvince() + " " + getPostalCode() + ", " +
                getCountry();

            Collection slots = getSlots();

            Iterator slotsIter = slots.iterator();

            while (slotsIter.hasNext()) {
                Slot slot = (Slot) slotsIter.next();
                Collection values = slot.getValues();

                Iterator valuesIter = values.iterator();

                while (valuesIter.hasNext()) {
                    String value = (String) valuesIter.next();
                    addrStr += (" " + value);
                }

                if (slotsIter.hasNext()) {
                    addrStr += ",";
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
            addrStr = e.toString();
        }

        return addrStr;
    }
}
