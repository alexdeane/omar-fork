/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/PostalAddressModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.RegistryMappedModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class PostalAddressModel extends RegistryMappedModel {
    public static final String[] ADDRESS_TYPES = {
            "Home", "Office", "Vacation", "Temporary"
        };
    private final User user;
    private final Map addresses = new HashMap();

    PostalAddressModel(User u) {
        super(ADDRESS_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setStreetNum.
     * @param addressType
     * @param text
     */
    public void setStreetNum(String streetNum) throws JAXRException {
        streetNum = streetNum.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setStreetNumber(streetNum);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress(streetNum,
                    "", "", "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setStreet.
     * @param addressType
     * @param text
     */
    public void setStreet(String street) throws JAXRException {
        street = street.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setStreet(street);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("",
                    street, "", "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setCity.
     * @param addressType
     * @param text
     */
    public void setCity(String city) throws JAXRException {
        city = city.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setCity(city);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    city, "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setState.
     * @param addressType
     * @param text
     */
    public void setState(String state) throws JAXRException {
        state = state.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setStateOrProvince(state);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", state, "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setPostalCode.
     * @param addressType
     * @param text
     */
    public void setPostalCode(String code) throws JAXRException {
        code = code.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setPostalCode(code);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", "", "", code, this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setCountry.
     * @param addressType
     * @param text
     */
    public void setCountry(String country) throws JAXRException {
        country = country.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = (PostalAddress) this.addresses.get(this.key);
            postalAddress.setCountry(country);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", "", country, "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    public PostalAddress getAddress() {
        PostalAddress address = (PostalAddress) this.addresses.get(this.key);

        return address;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection addressSet = this.addresses.values();
        PostalAddress address = null;
        Iterator i = addressSet.iterator();

        while (i.hasNext()) {
            address = (PostalAddress) i.next();

            String city = address.getCity();
            String country = address.getCountry();
            String code = address.getPostalCode();
            String state = address.getStateOrProvince();
            String street = address.getStreet();
            String number = address.getStreetNumber();

            // Takes all fields except the address type into account.
            if (((city == null) || (city.length() == 0)) &&
                    ((country == null) || (country.length() == 0)) &&
                    ((code == null) || (code.length() == 0)) &&
                    ((state == null) || (state.length() == 0)) &&
                    ((street == null) || (street.length() == 0)) &&
                    ((number == null) || (number.length() == 0))) {
                this.addresses.remove(address.getType());
            }
        }

        this.user.setPostalAddresses(this.addresses.values());
    }
}
