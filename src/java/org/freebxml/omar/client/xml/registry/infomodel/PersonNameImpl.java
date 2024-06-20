/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/PersonNameImpl.java,v 1.12 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;


import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;


/**
 * Implements JAXR API interface named PersonName.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class PersonNameImpl implements PersonName {
    private String lastName = null;
    private String middleName = null;
    private String firstName = null;

    // This required attribute is used to communicate to the parent PersonImpl
    // that a change to firstName, middleName or lastName has occurred. This
    // ensures that the InternationalString returned by this class is the same
    // as the parent RegistryObject.Name value.
    private PersonImpl personImpl = null;

    public PersonNameImpl(LifeCycleManagerImpl lcm) {
    }

    // 	private String fullName = null;     // ?? Spec issue w/ format conversion
    public PersonNameImpl(LifeCycleManagerImpl lcm,
                          PersonImpl personImpl) {
        this(lcm);
        this.personImpl = personImpl;
    }

    public PersonNameImpl(LifeCycleManagerImpl lcm,
                          PersonImpl personImpl,
                          PersonNameType ebPersonName) {
        this(lcm, personImpl);
        firstName = ebPersonName.getFirstName();
        middleName = ebPersonName.getMiddleName();
        lastName = ebPersonName.getLastName();
    }

    /**
     * This method is called by the PersonImpl class in its setPersonName method
     * There should be no need to call this method directly.
     */
    protected void setPersonImpl(PersonImpl personImpl) throws InvalidRequestException{
        this.personImpl = personImpl;
    }

    public PersonImpl getPersonImpl() {
        return personImpl;
    }

    public String getLastName() throws JAXRException {
        if (lastName == null) {
            lastName = "";
        }

        return lastName;
    }

    public void setLastName(String par1) throws JAXRException {
        if (lastName == null || !lastName.equals(par1)) {
            lastName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    public String getFirstName() throws JAXRException {
        if (firstName == null) {
            firstName = "";
        }

        return firstName;
    }

    public void setFirstName(String par1) throws JAXRException {
        if (firstName == null || !firstName.equals(par1)) {
            firstName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    public String getMiddleName() throws JAXRException {
        if (middleName == null) {
            middleName = "";
        }

        return middleName;
    }

    public void setMiddleName(String par1) throws JAXRException {
        if (middleName == null || !middleName.equals(par1)) {
            middleName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    /*
     * This method returns a formatted Person's name:
     * LastName, FirstName MiddleName.  Other variants include:
     * LastName, FirstName
     * LastName, MiddleName
     * FirstName MiddleName
     * LastName | FirstName | MiddleName
     *
     * This form should also be acceptable in Asian locales where just the 
     * first and last names are typically needed.
     */
    public String getFormattedName() throws JAXRException {
        StringBuffer fullName = new StringBuffer();

        if (getLastName() != null && getLastName().length() > 0) {
            fullName.append(lastName);
            if ((getFirstName() != null && getFirstName().length() > 0) ||
                (getMiddleName() != null && getMiddleName().length() > 0)) {
                fullName.append(", ");
            }
        }
        if (getFirstName() != null && getFirstName().length() > 0) {
            fullName.append(firstName);
            if (getMiddleName() != null && getMiddleName().length() > 0) {
                fullName.append(" ");
            }
        }
        if (getMiddleName() != null && getMiddleName().length() > 0) {
            fullName.append(middleName);
        }

        return fullName.toString().trim();
    }

    public String getFullName() throws JAXRException {
        //Do not attempt formatting by combining first, middle, lastName as that has
        //isues with getFullName not matching due to extra spaces etc.
        //and causes failures in JAXR TCK
        return getLastName();
    }
    
    public void setFullName(String fullName) throws JAXRException {
        //Do not attempt formatting by parsing fullName as that has
        //isues with getFullName not matching due to extra spaces etc.
        //and causes failures in JAXR TCK
        setLastName(fullName);
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.PersonName ebPersonName = factory.createPersonName();

            setBindingObject(ebPersonName);

            return ebPersonName;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.PersonNameType ebPersonName)
        throws JAXRException {
        ebPersonName.setFirstName(getFirstName());
        ebPersonName.setMiddleName(getMiddleName());
        ebPersonName.setLastName(getLastName());
    }
    
    public String toString() {
        String str = super.toString();

        try {
            str += " lastName:" + getLastName() + " formattedName: " + getFormattedName();
        } catch (JAXRException e) {
        }

        return str;
    }
}
