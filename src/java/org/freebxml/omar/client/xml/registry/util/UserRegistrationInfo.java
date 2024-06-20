/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/UserRegistrationInfo.java,v 1.4 2006/09/21 10:08:58 vikram_blr Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;

import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;


/**
 * Wrapper around {@link javax.xml.registry.infomodel.User}. This allows
 * us to check the validity of the input. Usually one would do that in the
 * model itself (i.e. class User), but an extra layer inbetween gives us
 * more flexibility.
 *
 * @author Fabian Ritzmann
 */
public class UserRegistrationInfo {
    private final User user;
    private String alias = null;
    private char[] keyPassword = null;
    private char[] storePassword = null;
    private String p12File = null; //File in PKCS12 format to import from / export to the cert
    private boolean caIssuedCert = false;
    private String organization = null;
    private String organizationUnit = null;

    
    /**
     * @param u Underlying User implementation
     */
    public UserRegistrationInfo(User u) throws JAXRException {
        this.user = u;

        // hard coded for now:
        this.storePassword = ProviderProperties.getInstance()
                                               .getProperty("jaxr-ebxml.security.storepass")
                                               .toCharArray();
    }

    public User getUser() {
        return this.user;
    }

    /**
     * Method setAlias.
     * @param text
     */
    public void setAlias(String text) {
        this.alias = text.trim();
        if (this.p12File == null) {
            this.p12File = System.getProperty("java.io.tmpdir", ".") + "/" + alias + ".p12";
        }
    }

    /**
     * Method setKeyPassword.
     * @param text
     */
    public void setKeyPassword(char[] text) {
        // Don't trim text here. It's not a good idea to use whitespace
        // in a password, but if somebody does, it shouldn't be changed.
        this.keyPassword = text;
    }

    public void setStorePassword(char[] text) {
        this.storePassword = text;
    }
    
    /**
     * Sets the PKCS12 file to import / export a cert from.
     */
    public void setP12File(String text) {
        this.p12File = text.trim();
    }

    /**
     * Sets the Organization the user belongs to
     */
    public void setOrganization(String organization) {
        this.organization = organization.trim();
    }

    /**
     * Sets the Organization Unit the user belongs to
     */
    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit.trim();
    }

    /**
     * Returns tru if this is a CA issued cert, false if registry issued cert.
     */
    public void setCAIssuedCert(boolean caIssuedCert) {
        this.caIssuedCert = caIssuedCert;
    }        

    /**
     * Method getAlias.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Method getKeyPassword.
     */
    public char[] getKeyPassword() {
        return this.keyPassword;
    }

    /**
     * Method getStorePassword.
     */
    public char[] getStorePassword() {
        return this.storePassword;
    }

    /**
     * Gets the PKCS12 file to import / export a cert from.
     */
    public String getP12File() {
        return this.p12File;
    }    

    /**
     * Gets the Organization user belongs to
     */
    public String getOrganization() {
        return this.organization;
    }    

    /**
     * Gets the Organization Unit user belongs to
     */
    public String getOrganizationUnit() {
        return this.organizationUnit;
    }    

    /**
     * Returns tru if this is a CA issued cert, false if registry issued cert.
     */
    public boolean isCAIssuedCert() {
        return this.caIssuedCert;
    }        
}
