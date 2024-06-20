/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/TelephoneNumberModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.RegistryMappedModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class TelephoneNumberModel extends RegistryMappedModel {
    public static final String[] PHONE_TYPES = {
            "Office Phone", "Home Phone", "Mobile Phone", "Beeper", "FAX"
        };
    private final User user;
    private final Map numbers = new HashMap();

    TelephoneNumberModel(User u) {
        super(PHONE_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setCountryCode.
     * @param phoneType
     * @param text
     */
    public void setCountryCode(String countryCode) throws JAXRException {
        countryCode = countryCode.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = (TelephoneNumber) this.numbers.get(this.key);
            phoneNumber.setCountryCode(countryCode);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setCountryCode(countryCode);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setAreaCode.
     * @param phoneType
     * @param text
     */
    public void setAreaCode(String areaCode) throws JAXRException {
        areaCode = areaCode.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = (TelephoneNumber) this.numbers.get(this.key);
            phoneNumber.setAreaCode(areaCode);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setAreaCode(areaCode);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setNumber.
     * @param phoneType
     * @param text
     */
    public void setNumber(String number) throws JAXRException {
        number = number.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = (TelephoneNumber) this.numbers.get(this.key);
            phoneNumber.setNumber(number);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setNumber(number);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setExtension.
     * @param phoneType
     * @param text
     */
    public void setExtension(String extension) throws JAXRException {
        extension = extension.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = (TelephoneNumber) this.numbers.get(this.key);
            phoneNumber.setExtension(extension);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setExtension(extension);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setURL.
     * @param phoneType
     * @param text
     */
    public void setURL(String url) throws JAXRException {
        url = url.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = (TelephoneNumber) this.numbers.get(this.key);
            phoneNumber.setUrl(url);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setUrl(url);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    public TelephoneNumber getNumber() {
        TelephoneNumber number = (TelephoneNumber) this.numbers.get(this.key);

        return number;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection numberSet = this.numbers.values();
        TelephoneNumber number = null;
        Iterator i = numberSet.iterator();

        while (i.hasNext()) {
            number = (TelephoneNumber) i.next();

            String areaCode = number.getAreaCode();
            String countryCode = number.getCountryCode();
            String extension = number.getExtension();
            String phoneNumber = number.getNumber();
            String url = number.getUrl();

            // Takes all fields except the address type into account.
            if (((areaCode == null) || (areaCode.length() == 0)) &&
                    ((countryCode == null) || (countryCode.length() == 0)) &&
                    ((extension == null) || (extension.length() == 0)) &&
                    ((phoneNumber == null) || (phoneNumber.length() == 0)) &&
                    ((url == null) || (url.length() == 0))) {
                this.numbers.remove(number.getType());
            }
        }

        this.user.setTelephoneNumbers(this.numbers.values());
    }
}
