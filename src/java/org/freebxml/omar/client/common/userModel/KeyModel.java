/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/userModel/KeyModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common.userModel;

import org.freebxml.omar.client.common.CommonResourceBundle;
import org.freebxml.omar.client.common.Model;
import org.freebxml.omar.client.xml.registry.util.UserRegistrationInfo;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


/**
 * A model for Digital Keys. Adds Model.validate to UserRegistrationInfo.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class KeyModel extends UserRegistrationInfo implements Model {
    private static final int MIN_ALIAS_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Creates a new instance of KeyModel.
     * Calls parent constructor with null argument.
     */
    public KeyModel() throws JAXRException {
        super(null);
    }

    /**
     * Creates a new instance of KeyModel.
     *
     * @param u User
     */
    public KeyModel(User u) throws JAXRException {
        super(u);
    }

    /** Implementation for Model.validate() */
    public void validate() throws JAXRException {
        if ((getAlias() == null) || (getAlias().length() < MIN_ALIAS_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keyAliasLength",
								   new String[] {String.valueOf(MIN_ALIAS_LENGTH)}));
        }

        if ((getKeyPassword() == null) || (getKeyPassword().length < MIN_PASSWORD_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keystorePasswordLength",
								   new String[] {String.valueOf(MIN_PASSWORD_LENGTH)}));
        }

        if ((getStorePassword() == null) || (getStorePassword().length < MIN_PASSWORD_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keyPasswordLength",
								   new String[] {String.valueOf(MIN_PASSWORD_LENGTH)}));
        }
    }
}
