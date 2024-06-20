/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/EmailAddressImpl.java,v 1.7 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.EmailAddressType;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;


/**
 * Implements JAXR API interface named EmailAddress.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class EmailAddressImpl implements EmailAddress {
    private BindingUtility bu = BindingUtility.getInstance();
    
    private String address = null;
    private String type = null;

    private EmailAddressImpl() {
    }

    public EmailAddressImpl(LifeCycleManagerImpl lcm) {
    }

    public EmailAddressImpl(LifeCycleManagerImpl lcm, EmailAddressType ebEmail)
        throws JAXRException {
        address = ebEmail.getAddress();
        type = ebEmail.getType();
    }

    public String getAddress() throws JAXRException {
        return address;
    }

    public void setAddress(String par1) throws JAXRException {
        address = par1;
    }

    public String getType() throws JAXRException {
        return type;
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
            org.oasis.ebxml.registry.bindings.rim.EmailAddress ebOrg = bu.rimFac.createEmailAddress();
            setBindingObject(ebOrg);

            return ebOrg;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.EmailAddressType ebEmailAddress)
        throws JAXRException {
        ebEmailAddress.setType(getType());
        ebEmailAddress.setAddress(getAddress());
    }
}
