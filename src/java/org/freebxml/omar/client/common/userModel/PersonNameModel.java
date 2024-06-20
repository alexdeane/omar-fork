/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/PersonNameModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.CommonResourceBundle;
import org.freebxml.omar.client.common.Model;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class PersonNameModel implements Model {
    private final User user;

    PersonNameModel(User name) throws JAXRException {
        this.user = name;
    }

    /**
     * Method setFirstName.
     * @param text
     */
    public void setFirstName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setFirstName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName(text, "", "");
            this.user.setPersonName(name);
        }
    }

    /**
     * Method setMiddleName.
     * @param text
     */
    public void setMiddleName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setMiddleName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName("", text, "");
            this.user.setPersonName(name);
        }
    }

    /**
     * Method setLastName.
     * @param text
     */
    public void setLastName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setLastName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName("", "", text);
            this.user.setPersonName(name);
        }
    }

    public void validate() throws JAXRException {
        String firstName = user.getPersonName().getFirstName();
        String middleName = user.getPersonName().getMiddleName();
        String lastName = user.getPersonName().getLastName();

        if (((firstName == null) || (firstName.length() == 0)) &&
                ((middleName == null) || (middleName.length() == 0)) &&
                ((lastName == null) || (lastName.length() == 0))) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.missingUserPersonName"));
        }
    }
}
