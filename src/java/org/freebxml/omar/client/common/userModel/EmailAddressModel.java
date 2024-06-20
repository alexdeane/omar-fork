/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/EmailAddressModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.RegistryMappedModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class EmailAddressModel extends RegistryMappedModel {
    public static final String[] EMAIL_TYPES = { "Office email", "Home email", };
    private final User user;
    private final Map addresses = new HashMap();

    EmailAddressModel(User u) {
        super(EMAIL_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setAddress.
     * @param emailType
     * @param text
     */
    public void setAddress(String address) throws JAXRException {
        address = address.trim();

        // Address already exists, just need to modify entry.
        if (this.addresses.containsKey(this.key)) {
            EmailAddress emailAddress = (EmailAddress) this.addresses.get(this.key);
            emailAddress.setAddress(address);
        }
        // Create new address and add to user.
        else {
            EmailAddress emailAddress = this.user.getLifeCycleManager()
                                                 .createEmailAddress(address,
                    this.key);
            this.addresses.put(this.key, emailAddress);
            this.user.setEmailAddresses(this.addresses.values());
        }
    }

    public EmailAddress getAddress() {
        EmailAddress address = (EmailAddress) this.addresses.get(this.key);

        return address;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection addressSet = this.addresses.values();
        EmailAddress address = null;
        Iterator i = addressSet.iterator();

        while (i.hasNext()) {
            address = (EmailAddress) i.next();

            String emailAddress = address.getAddress();

            if ((emailAddress == null) || (emailAddress.length() == 0)) {
                this.addresses.remove(address.getType());
            }
        }

        this.user.setEmailAddresses(this.addresses.values());
    }
}
