/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/UserModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.Model;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


/**
 * Wrapper around {@link javax.xml.registry.infomodel.User}. This allows
 * us to check the validity of the input. Usually one would do that in the
 * model itself (i.e. class User), but an extra layer inbetween gives us
 * more flexibility.
 *
 * @author Fabian Ritzmann
 */
public class UserModel implements Model {
    private final User user;
    private final PersonNameModel personName;
    private final EmailAddressModel emailAddress;
    private final PostalAddressModel postalAddress;
    private final TelephoneNumberModel phoneNumber;
    private final KeyModel key;

    /**
     * @param u Underlying User implementation
     */
    public UserModel(User u) throws JAXRException {
        this.user = u;
        this.key = new KeyModel(u);
        this.personName = new PersonNameModel(u);
        this.emailAddress = new EmailAddressModel(u);
        this.postalAddress = new PostalAddressModel(u);
        this.phoneNumber = new TelephoneNumberModel(u);

        // hard coded for now:
        key.setStorePassword(ProviderProperties.getInstance()
                                               .getProperty("jaxr-ebxml.security.storepass")
                                               .toCharArray());
    }

    public User getUser() {
        return user;
    }

    public KeyModel getUserRegistrationInfo() {
        return key;
    }

    /**
     * Method getPersonNameModel.
     */
    public PersonNameModel getPersonNameModel() {
        return this.personName;
    }

    /**
     * Method getEmailAddressModel.
     */
    public EmailAddressModel getEmailAddressModel() {
        return this.emailAddress;
    }

    /**
     * Method getPostalAddressModel.
     */
    public PostalAddressModel getPostalAddressModel() {
        return this.postalAddress;
    }

    /**
     * Method getTelephoneNumberModel.
     */
    public TelephoneNumberModel getTelephoneNumberModel() {
        return this.phoneNumber;
    }

    public void validate() throws JAXRException {
        this.personName.validate();
        this.emailAddress.validate();
        this.postalAddress.validate();
        this.phoneNumber.validate();
        this.key.validate();

        // TO DO: Review JAXR implementation dependency
        InternationalStringImpl roName = (InternationalStringImpl) this.user.getName();

        if ((roName == null) || (roName.getClosestValue() == null) ||
                (roName.getClosestValue().trim().length() == 0)) {
            String name = ((PersonNameImpl) (this.user.getPersonName())).getFormattedName();
            roName = (InternationalStringImpl) this.user.getLifeCycleManager()
                                                        .createInternationalString(name);
            this.user.setName(roName);
        }
    }
}
